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
package org.jboss.tattletale.core;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Archive
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class Archive implements Serializable, Comparable<Archive>
{
   /** SerialVersionUID */
   static final long serialVersionUID = 8349128019949046037L;

   /** Archive type */
   private final ArchiveType type;

   /** The name */
   private final String name;

   /** The version */
   private final int version;

   /** Manifest */
   private final List<String> manifest;

   /** Signing information */
   private final List<String> sign;

   /** Requires */
   private final SortedSet<String> requires;

   /** Provides */
   private final SortedMap<String, Long> provides;

   /** Profiles */
   private final SortedSet<String> profiles;

   /** Class dependencies */
   private final SortedMap<String, SortedSet<String>> classDependencies;

   /** Package dependencies */
   private final SortedMap<String, SortedSet<String>> packageDependencies;

   /** Blacklisted dependencies */
   private final SortedMap<String, SortedSet<String>> blacklistedDependencies;

   /** Locations */
   private final SortedSet<Location> locations;

   /** OSGi archive */
   private transient Boolean osgi;

   /** Module identifier */
   private String moduleIdentifier;

   /** Parent archive if it is a sub-archive */
   private Archive parentArchive = null;

   /**
    * Constructor
    * @param type                    The type
    * @param name                    The name
    * @param version                 The version number
    * @param manifest                The manifest
    * @param sign                    The signing information
    * @param requires                The requires
    * @param provides                The provides
    * @param classDependencies       The class dependencies
    * @param packageDependencies     The package dependencies
    * @param blacklistedDependencies The blacklisted dependencies
    * @param location                The location
    */
   protected Archive(ArchiveType type, String name, int version, List<String> manifest,
                     List<String> sign, SortedSet<String> requires, SortedMap<String, Long> provides,
                     SortedMap<String, SortedSet<String>> classDependencies,
                     SortedMap<String, SortedSet<String>> packageDependencies,
                     SortedMap<String, SortedSet<String>> blacklistedDependencies,
                     Location location)
   {
      this.type = type;
      this.name = name;
      this.version = version;
      this.manifest = manifest;
      this.sign = sign;
      this.requires = requires;
      this.provides = provides;
      this.classDependencies = classDependencies;
      this.packageDependencies = packageDependencies;
      this.blacklistedDependencies = blacklistedDependencies;
      profiles = new TreeSet<String>();
      locations = new TreeSet<Location>();
      osgi = null;
      moduleIdentifier = name;

      if (null != location)
      {
         locations.add(location);
      }
   }

   /**
    * Get the type
    * @return The value
    */
   public ArchiveType getType()
   {
      return type;
   }

   /**
    * Get the name
    * @return The value
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the version
    * @return The value
    */
   public int getVersion()
   {
      return version;
   }

   /**
    * Get the manifest
    * @return The value
    */
   public List<String> getManifest()
   {
      return manifest;
   }

   /**
    * Has manifest key
    * @param key The manifest key
    * @return True if the key is found; otherwise false
    */
   public boolean hasManifestKey(String key)
   {
      if (null != manifest)
      {
         for (String s : manifest)
         {
            if (s.startsWith(key))
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Get manifest value
    * @param key The manifest key
    * @return The value; <code>null</code> if not found
    */
   public String getManifestValue(String key)
   {
      if (null != manifest)
      {
         final StringBuilder value = new StringBuilder();
         boolean found = false;

         for (String s : manifest)
         {
            if (s.startsWith(key))
            {
               int idx = s.indexOf(':');
               value.append(s.substring(idx + 1).trim());
               found = true;
            }
            else if (found)
            {
               int idx = s.indexOf(':');
               if (idx != -1)
               {
                  return value.toString().trim();
               }
               else
               {
                  value.append(s.trim());
               }
            }
         }
      }

      return null;
   }

   /**
    * Get the signing information
    * @return The value
    */
   public List<String> getSign()
   {
      return sign;
   }

   /**
    * Get the requires
    * @return The value
    */
   public SortedSet<String> getRequires()
   {
      return requires;
   }

   /**
    * Get the provides
    * @return The value
    */
   public SortedMap<String, Long> getProvides()
   {
      return provides;
   }

   /**
    * Get the profiles
    * @return The value
    */
   public SortedSet<String> getProfiles()
   {
      return profiles;
   }

   /**
    * Add a profile
    * @param profile The profile
    */
   public void addProfile(String profile)
   {
      profiles.add(profile);
   }

   /**
    * Get the class dependencies
    * @return The value
    */
   public SortedMap<String, SortedSet<String>> getClassDependencies()
   {
      return classDependencies;
   }

   /**
    * Get the package dependencies
    * @return The value
    */
   public SortedMap<String, SortedSet<String>> getPackageDependencies()
   {
      return packageDependencies;
   }

   /**
    * Get the blacklisted dependencies
    * @return The value
    */
   public SortedMap<String, SortedSet<String>> getBlackListedDependencies()
   {
      return blacklistedDependencies;
   }

   /**
    * Get the locations
    * @return The value
    */
   public SortedSet<Location> getLocations()
   {
      return locations;
   }

   /**
    * Add a location
    * @param value The value
    */
   public void addLocation(Location value)
   {
      locations.add(value);
   }

   /**
    * Does the archives provide this class
    * @param clz The class name
    * @return True if the class is provided; otherwise false
    */
   public boolean doesProvide(String clz)
   {
      return provides.containsKey(clz);
   }

   /**
    * Is this an OSGi archive ?
    * @return True if OSGi; otherwise false
    */
   public boolean isOSGi()
   {
      if (null == osgi)
      {
         initOSGi();
      }

      return osgi;
   }

   /**
    * Simple setter
    * @param moduleIdentifier - the custom module identifier name.
    */
   public void setModuleIdentifier(String moduleIdentifier)
   {
      this.moduleIdentifier = moduleIdentifier;
   }

   /**
    * Simple getter
    *
    * @return - the module identifier string. The archive name by default unless the setter was previously called.
    */
   public String getModuleIdentifier()
   {
      return moduleIdentifier;
   }

   /**
    *  Init OSGi
    */
   private void initOSGi()
   {
      osgi = hasManifestKey("Bundle-SymbolicName");
   }

   /**
    * Comparable
    * @param a The other archive
    * @return The compareTo value
    */
   public int compareTo(Archive a)
   {
      return name.compareTo(a.getName());
   }

   /**
    * Equals
    * @param obj The other object
    * @return True if equals; otherwise false
    */
   public boolean equals(Object obj)
   {
      return obj instanceof Archive
              && name.equals(((Archive) obj).getName());
   }

   /**
    * Hash code
    * @return The hash code
    */
   public int hashCode()
   {
      return 7 + 31 * name.hashCode();
   }

   /**
    * String representation
    * @return The string
    */
   public String toString()
   {
      final StringBuilder sb = new StringBuilder();
      final String newline = System.getProperty("line.separator");

      sb.append(getClass().getName()).append('(').append(newline);
      sb.append("type=").append(type).append(newline);
      sb.append("name=").append(name).append(newline);
      sb.append("version=").append(version).append(newline);
      sb.append("manifest=").append(manifest).append(newline);
      sb.append("sign=").append(sign).append(newline);
      sb.append("requires=").append(requires).append(newline);
      sb.append("provides=").append(provides).append(newline);
      sb.append("profiles=").append(profiles).append(newline);
      sb.append("classdependencies=").append(classDependencies).append(newline);
      sb.append("packagedependencies=").append(packageDependencies).append(newline);
      sb.append("blacklisteddependencies=").append(blacklistedDependencies).append(newline);
      sb.append("locations=").append(locations).append(newline).append(')');

      return sb.toString();
   }

   /**
    * Simple getter.
    * @return - the parent archive.
    */
   public Archive getParentArchive()
   {
      return parentArchive;
   }

   /**
    * Simple setter.
    * @param parentArchive - the parent archive.
    */
   public void setParentArchive(Archive parentArchive)
   {
      this.parentArchive = parentArchive;
   }
}
