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
import java.util.jar.JarEntry;

/**
 * Class that would be used in order to obtain .jar files stored within a zipped up .war file.
 *
 * @author Navin Surtani
 */

public class Extractor
{

   /**
    * Static call that would take in a zipped up file and extract it.
    *
    * @param is               - The input stream
    * @param jarEntry         - The entry within the war/ear archive.
    * @return A list of Jar files.
    * @throws IOException     - based on the input stream.
    */
   public static File extract(InputStream is, JarEntry jarEntry) throws IOException
   {
      if (jarEntry == null)
      {
         throw new NullPointerException("The file parameter passed into Extractor is null");
      }

      String basePath = System.getProperty("java.io.tmpdir");
      File targetDir = new File(basePath + "/tt_tmp");

      if (targetDir.exists())
      {
         recursiveDelete(targetDir);
      }
      if (!targetDir.mkdirs())
      {
         throw new IOException("Could not create the target directories for: " + targetDir.getCanonicalPath());
      }
      File copy = new File(targetDir, jarEntry.getName());
      if (!copy.getParentFile().mkdirs())
      {
         throw new IOException("Could not create parent directory for the jar files.");
      }
      BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
      FileOutputStream fos = new FileOutputStream(copy);
      BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);

      byte[] buffer = new byte[4096];
      for (;;)
      {
         int nBytes = bufferedInputStream.read(buffer);
         if (nBytes <= 0) break;
         bufferedOutputStream.write(buffer, 0, nBytes);
      }
      bufferedOutputStream.flush();
      bufferedOutputStream.close();

      return copy;
   }

   private static void recursiveDelete(File file) throws IOException
   {
      if (file != null && file.exists())
      {
         File[] files = file.listFiles();
         if (files != null)
         {
            for (int i = 0; i < files.length; i++)
            {
               if (files[i].isDirectory())
               {
                  recursiveDelete(files[i]);
               }
               else if (!files[i].delete())
               {
                  throw new IOException("Could not delete the file of: " + files[i]);
               }
            }
         }

         if (!file.delete())
         {
            throw new IOException("Could not delete the file of: " + file);
         }
      }
   }
}
