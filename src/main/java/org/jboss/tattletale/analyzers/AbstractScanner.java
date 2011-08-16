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

import org.jboss.tattletale.core.Archive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Abstract class that contains utility methods that other scanner extensions can use.
 *
 * @author Navin Surtani
 * */
public abstract class AbstractScanner implements ArchiveScanner
{
   /**
    * Read the manifest
    *
    * @param manifest The manifest
    * @return The manifest as strings
    */
   protected List<String> readManifest(Manifest manifest)
   {
      List<String> result = new ArrayList<String>();

      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         manifest.write(baos);

         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
         InputStreamReader isr = new InputStreamReader(bais);
         LineNumberReader lnr = new LineNumberReader(isr);

         String s = lnr.readLine();
         while (s != null)
         {
            result.add(s);
            s = lnr.readLine();
         }
      }
      catch (IOException ioe)
      {
         // Ignore
      }

      return result;
   }

   /**
    * Returns the version as a String once read from the manifest.
    *
    * @param manifest - the manifest obtained from the JarFile
    * @return - the version as a String.
    */
   protected String versionFromManifest(Manifest manifest)
   {

      Attributes mainAttributes = manifest.getMainAttributes();
      String version = mainAttributes.getValue("Specification-Version");
      if (version == null)
      {
         version = mainAttributes.getValue("Implementation-Version");
      }
      if (version == null)
      {
         version = mainAttributes.getValue("Version");
      }

      if (version == null && manifest.getEntries() != null)
      {
         Iterator ait = manifest.getEntries().values().iterator();
         while (version == null && ait.hasNext())
         {
            Attributes attributes = (Attributes) ait.next();

            version = attributes.getValue("Specification-Version");
            if (version == null)
            {
               version = attributes.getValue("Implementation-Version");
            }
            if (version == null)
            {
               version = attributes.getValue("Version");
            }
         }
      }
      return version;
   }

   /**
    * Method that will add a set of profiles (Strings) to the archive.
    *
    * @param archive - the archive
    * @param profiles - the set of Strings.
    */

   protected void addProfilesToArchive(Archive archive, SortedSet<String> profiles)
   {
      if (profiles.size() > 0)
      {
         for (String profile : profiles)
         {
            archive.addProfile(profile);
         }
      }
   }


}
