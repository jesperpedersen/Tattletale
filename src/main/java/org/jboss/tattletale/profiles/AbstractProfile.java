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
package org.jboss.tattletale.profiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.jboss.tattletale.core.ArchiveType;
import org.jboss.tattletale.core.Location;

/**
 * Base profile class.
 *
 * @author Michele
 * @author Navin Surtani
 */
public abstract class AbstractProfile implements Profile
{
   /** The type of Profile */
   @SuppressWarnings("unused")
   private ArchiveType type;

   /** The version */
   @SuppressWarnings("unused")
   private int version;

   /** The name of the profile */
   private String name;

   /** Content of the class set file */
   protected final Set<String> classSet = new HashSet<String>();

   /** Set of locations */
   protected final Set<Location> locations = new HashSet<Location>();

   /** Set of sub-subProfiles */
   protected final Set<Profile> subProfiles = new HashSet<Profile>();

   /**
    * Constructor
    * @param classSet The .gz file with the classes
    * @param type     Archive type
    * @param name     Profile name
    * @param version  Profile's class version
    * @param location Profile's location
    */
   protected AbstractProfile(String classSet, ArchiveType type, String name, int version, String location)
   {
      this (type, name, version, location);
      loadProfile(classSet);
   }

   /**
    * Constructor
    *
    * @param type     Archive type
    * @param name     Profile name
    * @param version  Profile's class version
    * @param location Profile's location
    */
   protected AbstractProfile(ArchiveType type, String name, int version, String location)
   {
      this.type = type;
      this.name = name;
      this.version = version;
      addLocation(new Location(location, name));
   }

   /**
    * Checks whether or not the class is provided by the profile.
    * @param clz The class name
    * @return True if the class is provided; otherwise false
    * @see org.jboss.tattletale.profiles.Profile#doesProvide(String)
    */
   public boolean doesProvide(String clz)
   {
      if (classSet.contains(clz))
      {
         return true;
      }
      else
      {
         for (Profile subProfile : subProfiles)
         {
            if (subProfile.doesProvide(clz))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Simple getter that will return the name of the Profile.
    * @return the name of the profile.
    * @see org.jboss.tattletale.profiles.Profile#getName()
    */
   public String getName()
   {
      return name;
   }

   /**
    * Adds the location parameter to the private collection of Locations.
    * @param location - the location object.
    */

   public void addLocation(Location location)
   {
      locations.add(location);
   }

   /**
    * Adds the profile passed to the private collection of sub subProfiles.
    * @param profile - the profile object.
    */
   public void addSubProfile(Profile profile)
   {
      subProfiles.add(profile);
   }

   /**
    * Getter call to return the module identifier. Default implementation here is to return null ALWAYS. For
    * different implementations, this method call MUST be overridden.
    * @return null.
    * @see org.jboss.tattletale.profiles.Profile#getModuleIdentifier()
    */

   public String getModuleIdentifier()
   {
      // Default implementation.
      return null;
   }

   /**
    * Loads this profile's class list from the resources.
    * @param resourceFile File name
    */
   protected void loadProfile(String resourceFile)
   {
      InputStream is = null;
      try
      {
         is = this.getClass().getClassLoader().getResourceAsStream(resourceFile);

         final GZIPInputStream gis = new GZIPInputStream(is);
         final InputStreamReader isr = new InputStreamReader(gis);
         final BufferedReader br = new BufferedReader(isr);

         for (String line; (line = br.readLine()) != null;)
         {
            classSet.add(line);
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

   /**
    * Returns true if this profile is selected by the supplied configuration
    * information.
    * @param allProfiles All-Profiles flag
    * @param profileSet  Selected subProfiles as specified in the configuration
    * @return True if the Profile is to be included
    */
   public boolean included(boolean allProfiles, Set<String> profileSet)
   {
      return allProfiles || null != profileSet && (profileSet.contains(getProfileCode())
                                                   || profileSet.contains(getProfileName()));
   }

   /**
    * @return The code name of the profile
    */
   public abstract String getProfileCode();

   /**
    * @return The long name of the profile
    */
   protected abstract String getProfileName();
}
