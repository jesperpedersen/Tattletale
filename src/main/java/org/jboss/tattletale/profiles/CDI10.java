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

import javassist.bytecode.ClassFile;

import org.jboss.tattletale.core.ArchiveType;

/**
 * Contexts and Dependency Injection (CDI) 1.0 profile
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class CDI10 extends AbstractProfile
{
   /** Field CLASS_SET. (value is ""cdi10.clz.gz"") */
   private static final String CLASS_SET = "cdi10.clz.gz";

   /** Field PROFILE_NAME. (value is ""CDI 1.0"") */
   private static final String PROFILE_NAME = "CDI 1.0";

   /** Field PROFILE_CODE. (value is ""cdi10"") */
   private static final String PROFILE_CODE = "cdi10";

   /** Field PROFILE_LOCATION. (value is ""cdi-api-1.0.jar"") */
   private static final String PROFILE_LOCATION = "cdi-api-1.0.jar";

   /** Field MODULE_IDENTIFIER. (value is ""javaee.api"") */
   private static final String MODULE_IDENTIFIER = "javaee.api";

   /** Field ARCHIVE_TYPE. (value is ArchiveType.JAR) */
   private static final ArchiveType ARCHIVE_TYPE = ArchiveType.JAR;

   /** Field CLASSFILE_VERSION. (value is ClassFile.JAVA_5) */
   private static final int CLASSFILE_VERSION = ClassFile.JAVA_5;

   /** Constructor */
   public CDI10()
   {
      super(CLASS_SET, ARCHIVE_TYPE, PROFILE_NAME, CLASSFILE_VERSION, PROFILE_LOCATION);
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
   protected String getProfileName()
   {
      return PROFILE_NAME;
   }

   /**
    * Method getModuleIdentifier.
    * @return String
    * @see org.jboss.tattletale.profiles.Profile#getModuleIdentifier()
    */
   @Override
   public String getModuleIdentifier()
   {
      return MODULE_IDENTIFIER;
   }
}
