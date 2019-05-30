/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.tattletale.analyzers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.ClassesArchive;
import org.jboss.tattletale.core.EarArchive;
import org.jboss.tattletale.core.JarArchive;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.profiles.Profile;

/**
 * Scanner for .ear files.
 *
 * @author Navin Surtani
 */
public class EarScanner extends AbstractScanner
{
   /** Field extractPattern */
   private final String extractPattern;

   /** Field bundlePattern */
   private final Pattern bundlePattern;

   /** Field pattern */
   private final String pattern;

   /** Final placeholderClasses */
   private final boolean placeholderClasses;

   // NB! This is WebLogic proprietary stuff, not a part of JEE!
   /** Field CLASSES_DEFAULT_PATTERN. (value is ""APP-INF/classes"") */
   private static final String CLASSES_DEFAULT_PATTERN = "APP-INF/classes";

   /**
    * Constructor
    */
   public EarScanner()
   {
      extractPattern = ".*";
      bundlePattern = null;
      pattern = null;
      placeholderClasses = true;
   }

   /**
    * Constructor
    * @param extractPattern select matching entries
    */
   public EarScanner(String extractPattern)
   {
      this.extractPattern = extractPattern;
      bundlePattern = null;
      pattern = null;
      placeholderClasses = true;
   }

   /**
    * Constructor
    * @param extractPattern select matching entries
    * @param pattern bundle matching entries into JarArchives (ClassArchive is a placeholder)
    */
   public EarScanner(String extractPattern, String pattern)
   {
      this.extractPattern = extractPattern;
      placeholderClasses = false;
      if (null == pattern)
      {
         bundlePattern = Pattern.compile("(" + CLASSES_DEFAULT_PATTERN + ")/");
         this.pattern = null;
      }
      else
      {
         bundlePattern = Pattern.compile("(" + CLASSES_DEFAULT_PATTERN + "/(" + pattern + "))/");
         this.pattern = pattern;
      }
   }

   /**
    * Scan a .ear archive
    * @param ear The ear file to be scanned
    * @return The archive
    * @see org.jboss.tattletale.analyzers.ArchiveScanner#scan(File)
    */
   public Archive scan(File ear)
   {
      return this.scan(ear, null, null, null);
   }

   /**
    * Scan a .ear archive
    * @param ear         The ear file
    * @param gProvides   The global provides map
    * @param known       The set of known archives
    * @param blacklisted The set of black listed packages
    * @return The archive
    * @see org.jboss.tattletale.analyzers.ArchiveScanner#scan(File, Map, List, Set)
    */
   public Archive scan(File ear, Map<String, SortedSet<String>> gProvides, List<Profile> known,
                       Set<String> blacklisted)
   {
      if (null == ear || !ear.exists())
      {
         return null;
      }

      EarArchive earArchive = null;
      final List<Archive> subArchiveList = new ArrayList<Archive>();
      final ArchiveScanner jarScanner = new JarScanner();
      final ArchiveScanner warScanner = (null == bundlePattern) ? new WarScanner(extractPattern) :
          new WarScanner(extractPattern, pattern);
      JarFile earFile = null;
      final String name = ear.getName();
      try
      {
         final Extractor xt = new Extractor(ear, extractPattern);
         xt.extract();
         earFile = xt.getArchive();
         final File extractedDir = xt.getTarget();

         Integer classVersion = null;
         List<String> lSign = null;
         final Map<String, ClassScanner> classBundles = new HashMap<String, ClassScanner>();

         for (JarEntry earEntry : Collections.list(earFile.entries()))
         {
            String entryName = earEntry.getName();
            InputStream entryStream = null;

            if (entryName.endsWith(".class"))
            {
               Matcher match = bundlePattern.matcher(entryName);
               String bundleName = null;
               while (match.find())
               {
                  bundleName = match.group(1);
               }
               if (null == bundleName)
               {
                  bundleName = "unmatched_" + name;
               }

               ClassScanner cs = (!classBundles.isEmpty() && classBundles.containsKey(bundleName)) ?
                   classBundles.get(bundleName) : new ClassScanner(bundleName);

               try
               {
                  entryStream = earFile.getInputStream(earEntry);
                  classVersion = cs.scan(entryStream, known, blacklisted);
               }
               catch (IOException openException)
               {
                  openException.printStackTrace();
               }
               finally
               {
                  if (null != entryStream)
                  {
                     entryStream.close();
                  }
               }

               classBundles.put(bundleName, cs);
            }
            else if (entryName.contains("META-INF") && entryName.endsWith(".SF"))
            {
               InputStream is = null;
               try
               {
                  is = earFile.getInputStream(earEntry);

                  InputStreamReader isr = new InputStreamReader(is);
                  LineNumberReader lnr = new LineNumberReader(isr);

                  if (null == lSign)
                  {
                     lSign = new ArrayList<String>();
                  }

                  for (String line; (line = lnr.readLine()) != null;)
                  {
                     lSign.add(line);
                  }
               }
               catch (IOException ioe)
               {
                  // Ignore
               }
               finally
               {
                  try
                  {
                     if (null != is)
                     {
                        is.close();
                     }
                  }
                  catch (IOException ioe)
                  {
                     // Ignore
                  }
               }
            }
            else if (entryName.endsWith(".jar"))
            {
               File jarFile = new File(extractedDir.getCanonicalPath(), entryName);
               Archive jarArchive = jarScanner.scan(jarFile, gProvides, known, blacklisted);
               if (null != jarArchive)
               {
                  subArchiveList.add(jarArchive);
               }
            }
            else if (entryName.endsWith(".war") || entryName.endsWith(".rar"))
            {
               File warFile = new File(extractedDir.getCanonicalPath(), entryName);
               Archive warArchive = warScanner.scan(warFile, gProvides, known, blacklisted);
               if (null != warArchive)
               {
                  subArchiveList.add(warArchive);
               }
            }
         }

         String version = null;
         List<String> lManifest = null;
         final Manifest manifest = earFile.getManifest();

         if (null != manifest)
         {
            version = super.versionFromManifest(manifest);
            lManifest = super.readManifest(manifest);
         }

         final SortedSet<String> requires = new TreeSet<String>();
         final SortedMap<String, Long> provides = new TreeMap<String, Long>();
         final SortedSet<String> profiles = new TreeSet<String>();
         final SortedMap<String, SortedSet<String>> classDependencies = new TreeMap<String, SortedSet<String>>();
         final SortedMap<String, SortedSet<String>> packageDependencies = new TreeMap<String, SortedSet<String>>();
         final SortedMap<String, SortedSet<String>> blacklistedDependencies = new TreeMap<String, SortedSet<String>>();

         for (ClassScanner cs : classBundles.values())
         {
            final Location location = new Location(ear.getCanonicalPath() + cs.getLocation(), version);
            if (placeholderClasses)
            {
               // ClassesArchive is a placeholder that is excluded from analysis
               final ClassesArchive classesArchive = new ClassesArchive(cs.getName().replace(".jar", ""),
                                                                        cs.getClassVersion(), lManifest, lSign,
                                                                        cs.getRequires(), cs.getProvides(),
                                                                        cs.getClassDependencies(),
                                                                        cs.getPackageDependencies(),
                                                                        cs.getBlacklistedDependencies(), location);
               subArchiveList.add(classesArchive);
            }
            else
            {
               final JarArchive classesArchive = new JarArchive(cs.getName(), cs.getClassVersion(), lManifest, lSign,
                                                                cs.getRequires(), cs.getProvides(),
                                                                cs.getClassDependencies(),
                                                                cs.getPackageDependencies(),
                                                                cs.getBlacklistedDependencies(), location);
               subArchiveList.add(classesArchive);
            }

            requires.addAll(cs.getRequires());

            for (Map.Entry<String, Long> entry : cs.getProvides().entrySet())
            {
               String className = entry.getKey();
               if (null != gProvides)
               {
                  SortedSet<String> ss = gProvides.get(className);
                  if (null == ss)
                  {
                     ss = new TreeSet<String>();
                  }
                  if (placeholderClasses)
                  {
                     ss.add(name);
                  }
                  else
                  {
                     ss.add(cs.getName());
                  }
                  gProvides.put(className, ss);
               }
               if (!provides.containsKey(className))
               {
                  provides.put(className, entry.getValue());
                  requires.remove(className);
               }
               else
               {
                  System.err.println("Class " + className + " already seen!");
               }
            }

            profiles.addAll(cs.getProfiles());

            addDependencies(classDependencies, cs.getClassDependencies(), "Dependencies");
            addDependencies(packageDependencies, cs.getPackageDependencies(), "Package dependencies");
            addDependencies(blacklistedDependencies, cs.getBlacklistedDependencies(), "Blacklisted dependencies");
         }

         if (0 == provides.size() && 0 == subArchiveList.size())
         {
            return null;
         }

         // Obtain the class version from the first archive in the list of subarchives if it is null.
         if (subArchiveList.size() > 0 && null == classVersion)
         {
            classVersion = subArchiveList.get(0).getVersion();
         }
         if (null == classVersion)
         {
            classVersion = 0;
         }

         earArchive = new EarArchive(name, classVersion, lManifest, lSign, requires, provides,
                                     classDependencies, packageDependencies, blacklistedDependencies,
                                     new Location(ear.getCanonicalPath(), version), subArchiveList);
         super.addProfilesToArchive(earArchive, profiles);
      }
      catch (IOException ioe)
      {
         System.err.println("Scan: " + ioe.getMessage());
         ioe.printStackTrace(System.err);
      }
      finally
      {
         try
         {
            if (null != earFile)
            {
               earFile.close();
            }
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
      return earArchive;
   }
}
