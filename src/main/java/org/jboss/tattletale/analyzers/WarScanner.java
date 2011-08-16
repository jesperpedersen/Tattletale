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

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.JarArchive;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.core.WarArchive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
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

/**
 * Class that would be used to scan .war files.
 *
 * @author Navin Surtani
 */

public class WarScanner extends JarScanner
{
   @Override
   public Archive scan(File file, Map<String, SortedSet<String>> gProvides, List<Archive> known,
                       Set<String> blacklisted)
   {
      WarArchive warArchive = null;
      JarFile jarFile = null;
      List<JarArchive> jarArchiveList= new ArrayList<JarArchive>();
      // This archive will be checked if it is a JarArchive. If it is then it will be added to the list.
      Archive scannedArchive = null;

      try
      {
         Integer classVersion = null;
         SortedSet<String> requires = new TreeSet<String>();
         SortedMap<String, Long> provides = new TreeMap<String, Long>();
         SortedSet<String> profiles = new TreeSet<String>();
         SortedMap<String, SortedSet<String>> classDependencies = new TreeMap<String, SortedSet<String>>();
         SortedMap<String, SortedSet<String>> packageDependencies = new TreeMap<String, SortedSet<String>>();
         SortedMap<String, SortedSet<String>> blacklistedDependencies = new TreeMap<String, SortedSet<String>>();
         List<String> lSign = null;
         jarFile = new JarFile(file);
         Enumeration<JarEntry> e = jarFile.entries();

         while (e.hasMoreElements())
         {
            JarEntry jarEntry = e.nextElement();
            String entryName = jarEntry.getName();
            InputStream is = null;

            if (entryName.endsWith(".class"))
            {
               try
               {
                  is = jarFile.getInputStream(jarEntry);
                  classVersion = ClassScanner.scan(is, blacklisted, known, classVersion, provides, requires, profiles,
                        classDependencies, packageDependencies, blacklistedDependencies);
               }
               catch (Exception openException)
               {
                  // Ignore
               }
               finally
               {
                  try
                  {
                     if (is != null)
                     {
                        is.close();
                     }
                  }
                  catch (Exception closeException)
                  {
                     // No op.
                  }
               }
            }
            else if (jarEntry.getName().contains("META-INF") && jarEntry.getName().endsWith(".SF"))
            {
               is = null;
               try
               {
                  is = jarFile.getInputStream(jarEntry);

                  InputStreamReader isr = new InputStreamReader(is);
                  LineNumberReader lnr = new LineNumberReader(isr);

                  if (lSign == null)
                  {
                     lSign = new ArrayList<String>();
                  }

                  String s = lnr.readLine();
                  while (s != null)
                  {
                     lSign.add(s);
                     s = lnr.readLine();
                  }
               }
               catch (Exception ie)
               {
                  // Ignore
               }
               finally
               {
                  try
                  {
                     if (is != null)
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
               // We have a JAR file so we are going to make the scan call on the JAR superclass and then return it.
               scannedArchive = scan(file, gProvides, known, blacklisted);
               if (scannedArchive instanceof JarArchive)
               {
                  jarArchiveList.add((JarArchive) scannedArchive);
               }
            }
         }
         if (provides.size() == 0)
         {
            return null;
         }

         String version = null;
         List<String> lManifest = null;
         Manifest manifest = jarFile.getManifest();

         if (manifest != null)
         {
            version = super.versionFromManifest(manifest);
            lManifest = super.readManifest(manifest);
         }

         Location location = new Location(file.getCanonicalPath(), version);

         warArchive = new WarArchive(file.getName(), classVersion, lManifest, lSign, requires, provides,
               classDependencies, packageDependencies, blacklistedDependencies, location, jarArchiveList);

         super.addProfilesToArchive(warArchive, profiles);
         Iterator<String> it = provides.keySet().iterator();
         while (it.hasNext())
         {
            String provide = it.next();

            if (gProvides != null)
            {
               SortedSet<String> ss = gProvides.get(provide);
               if (ss == null)
               {
                  ss = new TreeSet<String>();
               }
               ss.add(warArchive.getName());
               gProvides.put(provide, ss);
            }
            requires.remove(provide);
         }
      }
      catch (IOException ioe)
      {
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
            if (jarFile != null)
            {
               jarFile.close();
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
