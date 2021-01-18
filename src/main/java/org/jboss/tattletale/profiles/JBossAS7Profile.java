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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javassist.bytecode.ClassFile;

import org.jboss.tattletale.core.ArchiveType;

/**
 * Profile for JBoss AS 7.
 *
 * @author Navin Surtani
 */
public class JBossAS7Profile extends AbstractProfile implements ExtendedProfile
{
   /** Field CLASS_SET. (value is ""jbossas7.clz.gz"") */
   private static final String CLASS_SET = "jbossas7.clz.gz";

   /** Field PROFILE_NAME. (value is ""JBoss AS 7"") */
   private static final String PROFILE_NAME = "JBoss AS 7";

   /** Field PROFILE_CODE. (value is ""as7"") */
   private static final String PROFILE_CODE = "as7";

   /** Field PROFILE_LOCATION. (value is ""jboss-modules.jar"") */
   private static final String PROFILE_LOCATION = "jboss-modules.jar";

   /** Field ARCHIVE_TYPE. (value is ArchiveType.JAR) */
   private static final ArchiveType ARCHIVE_TYPE = ArchiveType.JAR;

   /** Field CLASSFILE_VERSION. (value is ClassFile.JAVA_6) */
   private static final int CLASSFILE_VERSION = ClassFile.JAVA_6;

   /** Constructor */
   public JBossAS7Profile()
   {
      super(ARCHIVE_TYPE, PROFILE_NAME, CLASSFILE_VERSION, PROFILE_LOCATION);
      loadProfile(CLASS_SET);
   }

   /**
    * Implementation from {@link ExtendedProfile}.
    * @param clz  - the class name
    * @return     - the module identifier
    * @see org.jboss.tattletale.profiles.ExtendedProfile#getModuleIdentifier(String)
    */

   public String getModuleIdentifier(String clz)
   {
      for (Profile p : subProfiles)
      {
         if (p.doesProvide(clz))
         {
            return p.getModuleIdentifier();
         }
      }
      return null;
   }

   /**
    * Makes the call on the superclass.
    * @param clz The class name
    * @return - whether or not the class name is provided or not.
    * @see org.jboss.tattletale.profiles.Profile#doesProvide(String)
    */
   public boolean doesProvide(String clz)
   {
      return super.doesProvide(clz);
   }

   /**
    * The name of the Profile
    * @return  - the name of the profile
    * @see org.jboss.tattletale.profiles.Profile#getName()
    */

   public String getName()
   {
      return PROFILE_NAME;
   }

   /**
    * Method getProfileCode.
    * @return String
    */
   @Override
   public String getProfileCode()
   {
      return PROFILE_CODE;
   }

   /**
    * Method getProfileName.
    * @return String
    */
   @Override
   public String getProfileName()
   {
      return PROFILE_NAME;
   }

   /**
    * Method loadProfile.
    * @param classSet String
    */
   @Override
   protected void loadProfile(String classSet)
   {
      InputStream inputStream = null;
      try
      {
         inputStream = this.getClass().getClassLoader().getResourceAsStream(classSet);
         final GZIPInputStream gis = new GZIPInputStream(inputStream);
         final InputStreamReader isr = new InputStreamReader(gis);
         final BufferedReader br = new BufferedReader(isr);
         final Map <String, ProfileArchive> profileMapping = new HashMap<String, ProfileArchive>();

         for (String line; (line = br.readLine()) != null;)
         {
            String className = "";
            String archiveName = "";
            String moduleIdentifier = "";
            for (String value : line.split(","))
            {
               if (className.isEmpty())
               {
                  className = value;
                  continue;
               }
               if (archiveName.isEmpty())
               {
                  archiveName = value;
                  continue;
               }
               moduleIdentifier = value;
            }

            ProfileArchive profileArchive = profileMapping.get(archiveName);

            if (null == profileArchive)
            {
               profileArchive = new ProfileArchive(archiveName, moduleIdentifier);
               profileMapping.put(archiveName, profileArchive);
            }

            profileArchive.addClass(className);
         }

         subProfiles.addAll(profileMapping.values());
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
      finally
      {
         try
         {
            if (null != inputStream)
            {
               inputStream.close();
            }
         }
         catch (IOException ioe)
         {
            // No op.
         }
      }
   }
}
