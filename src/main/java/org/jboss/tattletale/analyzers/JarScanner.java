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

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.JarArchive;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.profiles.Profile;

/**
 * Java archive scanner
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author Navin Surtani
 */
public class JarScanner extends AbstractScanner
{
   /**
    * Scan an archive
    * @param file The file
    * @return The archive
    * @see org.jboss.tattletale.analyzers.ArchiveScanner#scan(File)
    */
   public Archive scan(File file)
   {
      return scan(file, null, null, null);
   }

   /**
    * Scan an archive
    * @param file        The file
    * @param gProvides   The global provides map
    * @param known       The set of known archives
    * @param blacklisted The set of black listed packages
    * @return The archive
    * @see org.jboss.tattletale.analyzers.ArchiveScanner#scan(File, Map, List, Set)
    */
   public Archive scan(File file, Map<String, SortedSet<String>> gProvides, List<Profile> known,
                       Set<String> blacklisted)
   {
      Archive archive = null;
      JarFile jarFile = null;
      final String name = file.getName();
      try
      {
         final String canonicalPath = file.getCanonicalPath();
         jarFile = new JarFile(file);
         Integer classVersion = null;
         final SortedSet<String> requires = new TreeSet<String>();
         final SortedMap<String, Long> provides = new TreeMap<String, Long>();
         final SortedSet<String> profiles = new TreeSet<String>();
         final SortedMap<String, SortedSet<String>> classDependencies = new TreeMap<String, SortedSet<String>>();
         final SortedMap<String, SortedSet<String>> packageDependencies = new TreeMap<String, SortedSet<String>>();
         final SortedMap<String, SortedSet<String>> blacklistedDependencies = new TreeMap<String, SortedSet<String>>();
         List<String> lSign = null;

         for (JarEntry jarEntry  : Collections.list(jarFile.entries()))
         {
            String entryName = jarEntry.getName();
            InputStream entryStream = null;
            if (entryName.endsWith(".class"))
            {
               try
               {
                  entryStream = jarFile.getInputStream(jarEntry);
                  classVersion = scanClasses(entryStream, blacklisted, known, classVersion, provides, requires,
                                             profiles, classDependencies, packageDependencies, blacklistedDependencies);
               }
               catch (IOException ioe)
               {
                  ioe.printStackTrace();
               }
               finally
               {
                  if (null != entryStream)
                  {
                     entryStream.close();
                  }
               }
            }
            else if (entryName.contains("META-INF") && entryName.endsWith(".SF"))
            {
               InputStream is = null;
               try
               {
                  is = jarFile.getInputStream(jarEntry);

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
         }

         if (0 == provides.size())
         {
            return null;
         }

         String version = null;
         List<String> lManifest = null;
         final Manifest manifest = jarFile.getManifest();
         if (null != manifest)
         {
            version = versionFromManifest(manifest);
            lManifest = readManifest(manifest);
         }
         final Location location = new Location(canonicalPath, version);

         if (null == classVersion)
         {
            classVersion = 0;
         }

         archive = new JarArchive(name, classVersion, lManifest, lSign, requires, provides,
                                  classDependencies, packageDependencies, blacklistedDependencies, location);
         addProfilesToArchive(archive, profiles);

         for (String provide : provides.keySet())
         {
            if (null != gProvides)
            {
               SortedSet<String> ss = gProvides.get(provide);
               if (null == ss)
               {
                  ss = new TreeSet<String>();
               }

               ss.add(archive.getName());
               gProvides.put(provide, ss);
            }

            requires.remove(provide);
         }
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
         // Probably not a JAR archive
      }
      catch (Exception e)
      {
         System.err.println("Scan: " + e.getMessage());
         e.printStackTrace(System.err);
      }
      finally
      {
         try
         {
            if (null != jarFile)
            {
               jarFile.close();
            }
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
      return archive;
   }
}
