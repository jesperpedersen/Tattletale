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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Directory scanner
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class DirectoryScanner
{
   /** Archive types that should be scanned */
   private static final Set<String> ARCHIVES = new HashSet<String>();

   static
   {
      ARCHIVES.add(".jar");
      ARCHIVES.add(".war");
   }

   /** Constructor */
   private DirectoryScanner()
   {
   }

   /**
    * Set archives
    * @param scan The archives
    */
   public static void setArchives(String scan)
   {
      ARCHIVES.clear();

      if (null != scan)
      {
         for (String token : scan.split("[\\s,]+"))
         {
            if (token.length() > 0 && '*' == token.charAt(0))
            {
               token = token.substring(1);
            }

            ARCHIVES.add(token.toLowerCase(Locale.US));
         }
      }

      if (ARCHIVES.isEmpty())
      {
         ARCHIVES.add(".jar");
         ARCHIVES.add(".war");
      }
   }

   /**
    * Scan a directory for JAR files
    * @param file The root directory
    * @return The list of JAR files
    */
   public static List<File> scan(File file)
   {
      return scan(file, null);
   }

   /**
    * Scan a directory for JAR files
    * @param file     The root directory
    * @param excludes The set of excludes
    * @return The list of files
    */
   public static List<File> scan(File file, Set<String> excludes)
   {
      return getFileListing(file, excludes);
   }

   /**
    * Recursively walk a directory tree and return a List of all Files found;
    * the List is sorted using File.compareTo().
    * @param aStartingDir is a valid directory, which can be read.
    * @param excludes     The set of excludes
    * @return The list of the files without the specific exclusions
    */
   private static List<File> getFileListing(File aStartingDir, Set<String> excludes)
   {
      final List<File> result = getFileListingNoSort(aStartingDir, excludes);
      Collections.sort(result);
      return result;
   }

   /**
    * Method getFileListingNoSort.
    * @param aStartingDir File
    * @param excludes Set<String>
    * @return List<File>
    */
   private static List<File> getFileListingNoSort(File aStartingDir, Set<String> excludes)
   {
      final File[] filesAndDirs = aStartingDir.listFiles();

      if (filesAndDirs == null)
      {
         return Collections.emptyList();
      }

      final List<File> result = new ArrayList<File>();

      for (File file : filesAndDirs)
      {
         if (file.isFile())
         {
            String extension = null;

            if (file.getName().lastIndexOf('.') != -1)
            {
               extension = file.getName().substring(file.getName().lastIndexOf('.'));
            }

            if (null != extension && ARCHIVES.contains(extension))
            {
               boolean include = true;

               if (null != excludes)
               {
                  for (String exclude : excludes)
                  {
                     if (file.getName().equals(exclude) || file.getAbsolutePath().contains(exclude))
                     {
                        include = false;
                        break;
                     }
                  }
               }

               if (include)
               {
                  result.add(file);
               }
            }
         }
         else if (file.isDirectory())
         {
            List<File> deeperList = getFileListingNoSort(file, excludes);
            result.addAll(deeperList);
         }
      }

      return result;
   }
}
