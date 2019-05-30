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
import org.jboss.tattletale.core.JarArchive;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.core.WarArchive;
import org.jboss.tattletale.profiles.Profile;

/**
 * Scanner for .war (and .rar) files.
 *
 * @author Navin Surtani
 */
public class WarScanner extends AbstractScanner
{
   /** Field extractPattern */
   private final String extractPattern;

   /** Field bundlePattern */
   private final Pattern bundlePattern;

   /** Final placeholderClasses */
   private final boolean placeholderClasses;

   /** Field CLASSES_DEFAULT_PATTERN. (value is ""WEB-INF/classes"") */
   private static final String CLASSES_DEFAULT_PATTERN = "WEB-INF/classes";

   /**
    * Constructor
    */
   public WarScanner()
   {
      extractPattern = ".*";
      bundlePattern = Pattern.compile("(" + CLASSES_DEFAULT_PATTERN + ")/");
      placeholderClasses = true;
   }

   /**
    * Constructor
    * @param extractPattern select matching entries
    */
   public WarScanner(String extractPattern)
   {
      this.extractPattern = extractPattern;
      bundlePattern = Pattern.compile("(" + CLASSES_DEFAULT_PATTERN + ")/");
      placeholderClasses = true;
   }

   /**
    * Constructor
    * @param extractPattern select matching entries
    * @param pattern bundle matching entries into JarArchives (ClassArchive is a placeholder)
    */
   public WarScanner(String extractPattern, String pattern)
   {
      this.extractPattern = extractPattern;
      placeholderClasses = false;
      if (null == pattern)
      {
         bundlePattern = Pattern.compile("(" + CLASSES_DEFAULT_PATTERN + ")/");
      }
      else
      {
         bundlePattern = Pattern.compile("(" + CLASSES_DEFAULT_PATTERN + "/(" + pattern + "))/");
      }
   }

   /**
    * Scan a .war archive
    * @param war The war file to be scanned
    * @return The archive
    * @see org.jboss.tattletale.analyzers.ArchiveScanner#scan(File)
    */
   public Archive scan(File war)
   {
      return this.scan(war, null, null, null);
   }

   /**
    * Scan a .war archive
    * @param war         The war file
    * @param gProvides   The global provides map
    * @param known       The set of known archives
    * @param blacklisted The set of black listed packages
    * @return The archive
    * @see org.jboss.tattletale.analyzers.ArchiveScanner#scan(File, Map, List, Set)
    */
   public Archive scan(File war, Map<String, SortedSet<String>> gProvides, List<Profile> known,
                       Set<String> blacklisted)
   {
      if (null == war || !war.exists())
      {
         return null;
      }

      WarArchive warArchive = null;
      final List<Archive> subArchiveList = new ArrayList<Archive>();
      final ArchiveScanner jarScanner = new JarScanner();
      JarFile warFile = null;
      final String name = war.getName();
      Extractor xt = null;

      try
      {
         xt = new Extractor(war, extractPattern);
         xt.extract();
         warFile = xt.getArchive();
         final File extractedDir = xt.getTarget();

         Integer classVersion = null;
         List<String> lSign = null;
         final Map<String, ClassScanner> classBundles = new HashMap<String, ClassScanner>();

         for (JarEntry warEntry : Collections.list(warFile.entries()))
         {
            String entryName = warEntry.getName();
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
                  entryStream = warFile.getInputStream(warEntry);
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
                  is = warFile.getInputStream(warEntry);

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
         }

         String version = null;
         List<String> lManifest = null;
         final Manifest manifest = warFile.getManifest();

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
            final Location location = new Location(war.getCanonicalPath() + cs.getLocation(), version);
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

         warArchive = new WarArchive(name, classVersion, lManifest, lSign, requires, provides,
                                     classDependencies, packageDependencies, blacklistedDependencies,
                                     new Location(war.getCanonicalPath(), version), subArchiveList);
         super.addProfilesToArchive(warArchive, profiles);
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
            if (null != warFile)
            {
               warFile.close();
               xt.deleteTempTarget();
            }
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
      return warArchive;
   }
}
