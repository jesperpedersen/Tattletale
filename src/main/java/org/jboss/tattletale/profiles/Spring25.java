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
 * Spring 2.5
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Spring25 extends AbstractProfile
{
   /** Field CLASS_SET. (value is ""spring25.clz.gz"" */
   private static final String CLASS_SET = "spring25.clz.gz";

   /** Field PROFILE_NAME. (value is ""Spring 2.5"") */
   private static final String PROFILE_NAME = "Spring 2.5";

   /** Field PROFILE_CODE. (value is ""spring25"") */
   private static final String PROFILE_CODE = "spring25";

   /** Field PROFILE_LOCATION. (value is ""spring.jar"") */
   private static final String PROFILE_LOCATION = "spring.jar";

   /** Field ARCHIVE_TYPE. (value is ArchiveType.JAR) */
   private static final ArchiveType ARCHIVE_TYPE = ArchiveType.JAR;

   /** Field CLASSFILE_VERSION. (value is ClassFile.JAVA_4) */
   private static final int CLASSFILE_VERSION = ClassFile.JAVA_4;

   /** Constructor */
   public Spring25()
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
}
