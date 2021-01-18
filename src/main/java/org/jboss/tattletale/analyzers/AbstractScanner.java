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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.profiles.Profile;

/**
 * Abstract class that contains utility methods that other scanner extensions can use.
 *
 * @author Navin Surtani
 */
public abstract class AbstractScanner implements ArchiveScanner
{
   /**
    * Read the manifest
    * @param manifest The manifest
    * @return The manifest as strings
    */
   protected List<String> readManifest(Manifest manifest)
   {
      final List<String> result = new ArrayList<String>();

      try
      {
         final ByteArrayOutputStream baos = new ByteArrayOutputStream();
         manifest.write(baos);

         final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
         final InputStreamReader isr = new InputStreamReader(bais);
         final LineNumberReader lnr = new LineNumberReader(isr);

         for (String line; (line = lnr.readLine()) != null;)
         {
            result.add(line);
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
    * @param manifest - the manifest obtained from the JarFile
    * @return - the version as a String.
    */
   protected String versionFromManifest(Manifest manifest)
   {

      final Attributes mainAttributes = manifest.getMainAttributes();
      String version = mainAttributes.getValue("Specification-Version");
      if (null == version)
      {
         version = mainAttributes.getValue("Implementation-Version");
      }

      if (null == version)
      {
         version = mainAttributes.getValue("Version");
      }

      if (null == version && null != manifest.getEntries())
      {
         for (Attributes attributes : manifest.getEntries().values())
         {
            version = attributes.getValue("Specification-Version");

            if (null == version)
            {
               version = attributes.getValue("Implementation-Version");
            }

            if (null == version)
            {
               version = attributes.getValue("Version");
            }

            if (null != version)
            {
               break;
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
      for (String profile : profiles)
      {
         archive.addProfile(profile);
      }
   }

   /**
    * Static method called to scan class files within an input stream and populate the data structure parameters.
    * @param is - input stream
    * @param blacklisted The set of black listed packages
    * @param known       The set of known archives
    * @param classVersion - the version of the class file
    * @param provides - the map of provides
    * @param requires - the set of requires
    * @param profiles - the set of profiles
    * @param classDependencies - the map of class dependencies
    * @param packageDependencies - the map of package dependencies
    * @param blacklistedDependencies - the map of blacklisted dependencies
    * @return An {@link Integer} representing the class version.
    * @throws IOException - if the Javassist ClassPool cannot make the CtClass based on the input stream.
    */
   public Integer scanClasses(InputStream is, Set<String> blacklisted, List<Profile> known, Integer classVersion,
                              SortedMap<String, Long> provides, SortedSet<String> requires,
                              SortedSet<String> profiles, SortedMap<String, SortedSet<String>> classDependencies,
                              SortedMap<String, SortedSet<String>> packageDependencies,
                              SortedMap<String, SortedSet<String>> blacklistedDependencies)
      throws IOException
   {
      final ClassPool classPool = new ClassPool();
      final CtClass ctClz = classPool.makeClass(is);

      if (null == classVersion)
      {
         classVersion = ctClz.getClassFile2().getMajorVersion();
      }

      Long serialVersionUID = null;
      try
      {
         final CtField field = ctClz.getField("serialVersionUID");
         serialVersionUID = (Long) field.getConstantValue();
      }
      catch (NotFoundException nfe)
      {
         // Ignore - not serializable
      }

      provides.put(ctClz.getName(), serialVersionUID);

      final int pkgIdx = ctClz.getName().lastIndexOf('.');
      String pkg = null;

      if (pkgIdx != -1)
      {
         pkg = ctClz.getName().substring(0, pkgIdx);
      }

      for (Object c : ctClz.getRefClasses())
      {
         String clzName = (String) c;
         requires.add(clzName);

         SortedSet<String> cd = classDependencies.get(ctClz.getName());
         if (null == cd)
         {
            cd = new TreeSet<String>();
         }
         cd.add(clzName);
         classDependencies.put(ctClz.getName(), cd);

         int rPkgIdx = clzName.lastIndexOf('.');
         String rPkg = null;
         if (rPkgIdx != -1)
         {
            rPkg = clzName.substring(0, rPkgIdx);
         }

         boolean include = true;

         if (null != known)
         {
            for (Profile p : known)
            {
               if (p.doesProvide(clzName))
               {
                  profiles.add(p.getName());
                  include = false;
                  break;
               }
            }
         }

         if (null != pkg && null != rPkg && !pkg.equals(rPkg) && include)
         {
            SortedSet<String> pd = packageDependencies.get(pkg);
            if (null == pd)
            {
               pd = new TreeSet<String>();
            }
            pd.add(rPkg);
            packageDependencies.put(pkg, pd);
         }

         if (null != blacklisted)
         {
            boolean bl = false;

            for (String blp : blacklisted)
            {
               if (clzName.startsWith(blp))
               {
                  bl = true;
                  break;
               }
            }

            if (bl)
            {
               String key = pkg;

               if (null == key)
               {
                  key = "";
               }

               SortedSet<String> bld = blacklistedDependencies.get(key);
               if (null == bld)
               {
                  bld = new TreeSet<String>();
               }
               bld.add(rPkg);
               blacklistedDependencies.put(key, bld);
            }
         }
      }
      return classVersion;
   }

   /**
    * Method addDependencies.
    * @param allDependencies SortedMap&lt;String,SortedSet&lt;String&gt;&gt;
    * @param bundleDependencies SortedMap&lt;String,SortedSet&lt;String&gt;&gt;
    * @param comment String
    */
   protected void addDependencies(final SortedMap<String, SortedSet<String>> allDependencies,
                                SortedMap<String, SortedSet<String>> bundleDependencies, String comment)
   {
      if (bundleDependencies.isEmpty())
      {
         return;
      }
      if (null == comment)
      {
         comment = "Dependencies";
      }
      for (Map.Entry<String, SortedSet<String>> entry : bundleDependencies.entrySet())
      {
         String className = entry.getKey();
         if (!allDependencies.containsKey(className))
         {
            allDependencies.put(className, entry.getValue());
         }
         else
         {
            System.err.println(comment + " of class " + className + " already seen!");
         }
      }
   }
}
