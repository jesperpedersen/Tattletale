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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.profiles.Profile;

/**
 * ClassScanner scans individual class files and collects the scan results
 *
 * @author Navin Surtani
 * @author Gintas Grigelionis
 */
public class ClassScanner extends AbstractScanner
{
   /** Field location */
   private String location = null;

   /** Field validate */
   private boolean validate = false;

   /** Field classVersion. */
   private Integer classVersion = null;

   /** Field requires. */
   private final SortedSet<String> requires = new TreeSet<String>();

   /** Field provides. */
   private final SortedMap<String, Long> provides = new TreeMap<String, Long>();

   /** Field profiles. */
   private final SortedSet<String> profiles = new TreeSet<String>();

   /** Field classDependencies. */
   private final SortedMap<String, SortedSet<String>> classDependencies = new TreeMap<String, SortedSet<String>>();

   /** Field packageDependencies. */
   private final SortedMap<String, SortedSet<String>> packageDependencies = new TreeMap<String, SortedSet<String>>();

   /** Field blacklistedDependencies. */
   private final SortedMap<String, SortedSet<String>> blacklistedDependencies =
           new TreeMap<String, SortedSet<String>>();

   /**
    * Constructor
    * @param location Unique part of the class URL that identifies ClassScanner
    */
   public ClassScanner(String location)
   {
      this.location = location;
   }

   /**
    * Constructor
    * @param location Unique part of the class URL that identifies ClassScanner
    * @param validate Flag that tells ClassScanner to check whether the class file name provided to scan() matches name
    */
   public ClassScanner(String location, boolean validate)
   {
      this.location = location;
      this.validate = validate;
   }

   /**
    * Scan a class file
    * @param file The file
    * @return The archive (always null)
    * @throws IOException rethrown from scanClasses() or when file URL does not match location
    * @see org.jboss.tattletale.analyzers.ArchiveScanner#scan(File)
    */
   public Archive scan(File file) throws IOException
   {
      return scan(file, null, null, null);
   }

   /**
    * Scan a class file
    * @param file        The file
    * @param gProvides   The global provides map (ignored)
    * @param known       The set of known archives
    * @param blacklisted The set of black listed packages   
    * @return The archive (always null)
    * @throws IOException rethrown from scanClasses() or when file URL does not match location
    * @see org.jboss.tattletale.analyzers.ArchiveScanner#scan(File, Map, List, Set)
    */
   public Archive scan(File file, Map<String, SortedSet<String>> gProvides,
                       List<Profile> known, Set<String> blacklisted) throws IOException
   {
      final String filename = file.getName();
      if (validate && !filename.matches(".*" + location + ".*"))
      {
         throw new IOException("Class file name " + filename +
                               " does not match scanner location " + location);
      }
      final InputStream is;

      try
      {
         is = new FileInputStream(file);
         scan(is, known, blacklisted);
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   /**
    * Scan a class stream
    * @param stream The stream
    * @return class version
    * @throws IOException rethrown from scanClasses()
    */
   public int scan(InputStream stream) throws IOException
   {
      return scan(stream, null, null);
   }

   /**
    * Scan a class stream
    * @param stream      The stream
    * @param known       The set of known archives
    * @param blacklisted The set of black listed packages
    * @return class version
    * @throws IOException rethrown from scanClasses()
    */
   public int scan(InputStream stream, List<Profile> known, Set<String> blacklisted) throws IOException
   {
      classVersion = scanClasses(stream, blacklisted, known, classVersion, provides, requires,
                                 profiles, classDependencies, packageDependencies, blacklistedDependencies);
      for (String provide : provides.keySet())
      {
         requires.remove(provide);
      }
      return classVersion;
   }

   /**
    * Method getBlacklistedDependencies.
    * @return blacklisted dependencies
    */
   public SortedMap<String, SortedSet<String>> getBlacklistedDependencies()
   {
      return blacklistedDependencies;
   }

   /**
    * Method getClassDependencies.
    * @return class dependencies
    */
   public SortedMap<String, SortedSet<String>> getClassDependencies()
   {
      return classDependencies;
   }

   /**
    * Method getClassVersion.
    * @return class version
    */
   public int getClassVersion()
   {
      return classVersion;
   }

   /**
    * Method getLocation.
    * @return location
    */
   public String getLocation()
   {
      return "/" + location;
   }
   
   /**
    * Method getName.
    * @return name
    */
   public String getName()
   {
      return location.replaceAll("/", "_") + ".jar";
   }

   /**
    * Method getPackageDependencies.
    * @return package dependencies
    */
   public SortedMap<String, SortedSet<String>> getPackageDependencies()
   {
      return packageDependencies;
   }

   /**
    * Method getProfiles.
    * @return profiles
    */
   public SortedSet<String> getProfiles()
   {
      return profiles;
   }

   /**
    * Method getProvides.
    * @return provides
    */
   public SortedMap<String, Long> getProvides()
   {
      return provides;
   }

   /**
    * Method getRequires.
    * @return requires
    */
   public SortedSet<String> getRequires()
   {
      return requires;
   }
}
