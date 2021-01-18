/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.tattletale.tools.as7;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Tools to generate a profile for JBoss Application Server 7
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Main
{
   /**
    * Constructor
    */
   private Main()
   {
   }

   /**
    * main
    * @param args The arguments
    */
   public static void main(String[] args)
   {
      if (null != args && 2 == args.length)
      {
         FileWriter fw = null;
         try
         {
            final File root = new File(args[0], "modules");
            final File outputFile = new File(args[1]);

            fw = new FileWriter(outputFile);

            for (File f : getFileListing(root))
            {
               String moduleId = "";
               String archiveName = f.getName();

               File moduleXml = new File(f.getParentFile(), "module.xml");
               if (moduleXml.exists())
               {
                  moduleId = ModuleXml.getModuleId(moduleXml);
               }

               JarFile jf = new JarFile(f);

               for (JarEntry je : Collections.list(jf.entries()))
               {
                  if (je.getName().endsWith(".class"))
                  {
                     String className = je.getName().replace('/', '.');
                     className = className.substring(0, className.indexOf(".class"));

                     fw.write(className + "," + archiveName + "," + moduleId + "\n");
                  }
               }

               jf.close();
            }
         }
         catch (Throwable t)
         {
            t.printStackTrace(System.out);
         }
         finally
         {
            if (null != fw)
            {
               try
               {
                  fw.close();
               }
               catch (IOException ioe)
               {
                  // Ignore
               }
            }
         }
      }
      else
      {
         System.out.println("Usage: Main <as7> <file>");
      }
   }

   /**
    * Recursively walk a directory tree and return a List of all
    * Files found; the List is sorted using File.compareTo().
    * @param aStartingDir is a valid directory, which can be read.
    * @return List<File>
    */
   private static List<File> getFileListing(File aStartingDir)
   {
      final List<File> result = getFileListingNoSort(aStartingDir);
      Collections.sort(result);
      return result;
   }

   /**
    * Method getFileListingNoSort.
    * @param aStartingDir File
    * @return List<File>
    */
   private static List<File> getFileListingNoSort(File aStartingDir)
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

            if (".jar".equals(extension))
            {
               result.add(file);
            }
         }
         else if (file.isDirectory())
         {
            List<File> deeperList = getFileListingNoSort(file);
            result.addAll(deeperList);
         }
      }

      return result;
   }
}
