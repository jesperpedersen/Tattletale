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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Class that would be used in order to extract .jar files from a .war file.
 * .war file can be renamed if necessary (so that .jar files have proper path names).
 *
 * @author Navin Surtani
 */
public class Extractor
{
   /** Field jf */
   private JarFile jf;
   /** Field target. */
   private File target;
   /** Field tempTarget. */
   private File tempTarget;
   /** Field extractPattern */
   private Pattern extractPattern = Pattern.compile(".*");
   /** Field basedir */
   private final String basedir = new File(System.getProperty("java.io.tmpdir")).getCanonicalPath();

   /**
    * Constructor for Extractor.
    * @param jar File
    * @param pattern String - extract only entries matched by pattern
    * @throws IOException if an error occurs in JarFile
    */
   public Extractor(File jar, String pattern) throws IOException
   {
      this(jar);

      try
      {
         extractPattern = Pattern.compile(pattern);
      }
      catch (PatternSyntaxException pse)
      {
         System.err.println("Incorrect extraction pattern: " + pattern);
      }
   }

   /**
    * Constructor for Extractor.
    * @param jar File
    * @throws IOException if an error occurs in JarFile
    */
   public Extractor(File jar) throws IOException
   {
      jf = new JarFile(jar);
      target = jar;

      if (!jar.isFile())
      {
         return;
      }

      String fileName = jar.getCanonicalPath();

      if (fileName.startsWith(basedir))
      {
         jf.close();
         final StringBuilder nb = new StringBuilder(fileName);
         nb.insert(fileName.lastIndexOf('.'), "." + UUID.randomUUID().toString());
         tempTarget = new File(nb.toString());
         if (!target.renameTo(tempTarget))
         {
            System.out.println("Rename of " + fileName + "failed!");
         }
         jf = new JarFile(tempTarget);
         return;
      }

      if (fileName.indexOf(':') != -1 &&
          System.getProperty("os.name").toLowerCase(Locale.US).contains("windows"))
      {
         fileName = fileName.substring(fileName.indexOf(':') + 1);
      }

      target = new File(basedir, fileName);
   }

   /**
    * Method getTarget.
    * @return File
    */
   public File getTarget()
   {
      return target;
   }

   /**
    * Method getArchive.
    * @return JarFile
    */
   public JarFile getArchive()
   {
      return jf;
   }

   /**
    * Extract a nested JAR type file
    * @throws IOException Thrown if an error occurs
    */
   public void extract() throws IOException
   {
      if (target.isDirectory() && !target.getCanonicalPath().startsWith(basedir))
      {
         return;
      }

      if (target.exists())
      {
         recursiveDelete(target);
      }

      if (!target.mkdirs())
      {
         throw new IOException("Could not create " + target);
      }

      for (JarEntry je : Collections.list(jf.entries()))
      {
         Matcher matcher = extractPattern.matcher(je.getName());
         if (!je.isDirectory() && !matcher.matches())
         {
            continue;
         }
         File copy = new File(target, je.getName());

         if (!je.isDirectory())
         {
            InputStream in = null;
            OutputStream out = null;

            // Make sure that the directory is _really_ there
            if (null != copy.getParentFile() && !copy.getParentFile().exists())
            {
               if (!copy.getParentFile().mkdirs())
               {
                  throw new IOException("Could not create " + copy.getParentFile());
               }
            }

            try
            {
               in = new BufferedInputStream(jf.getInputStream(je));
               out = new BufferedOutputStream(new FileOutputStream(copy));

               byte[] buffer = new byte[4096];
               for (;;)
               {
                  int nBytes = in.read(buffer);
                  if (nBytes <= 0)
                  {
                     break;
                  }
                  out.write(buffer, 0, nBytes);
               }
               out.flush();
            }
            finally
            {
               try
               {
                  if (null != out)
                  {
                     out.close();
                  }
               }
               catch (IOException ioe)
               {
                  // Ignore
               }

               try
               {
                  if (null != in)
                  {
                     in.close();
                  }
               }
               catch (IOException ioe)
               {
                  // Ignore
               }
            }
         }
         else
         {
            if (!copy.exists())
            {
               if (!copy.mkdirs())
               {
                  throw new IOException("Could not create " + copy);
               }
            }
            else
            {
               if (!copy.isDirectory())
               {
                  throw new IOException(copy + " is not a directory");
               }
            }
         }
      }
   }

   /**
    * Method deleteTempTarget.
    * @throws IOException if an error occurs
    */
   public void deleteTempTarget() throws IOException
   {
      if (null != tempTarget && !tempTarget.delete())
      {
         System.out.println("Delete of " + tempTarget.getCanonicalPath() + "failed!");
      }
   }

   /**
    * Method recursiveDelete.
    * @param file File
    * @throws IOException
    */
   private static void recursiveDelete(File file) throws IOException
   {
      if (null != file && file.exists())
      {
         if (file.isDirectory())
         {
            for (File f : file.listFiles())
            {
               recursiveDelete(f);
            }
         }

         if (!file.delete())
         {
            throw new IOException("Could not delete the file \"" + file + "\"");
         }
      }
   }
}
