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
 * Sun: Java 6 (JCE)
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class SunJava6JCE extends AbstractProfile
{
   /** Field CLASS_SET. (value is ""sunjdk6-jce.clz.gz"") */
   private static final String CLASS_SET = "sunjdk6-jce.clz.gz";

   /** Field PROFILE_NAME. (value is ""Sun Java 6 (JCE)"") */
   private static final String PROFILE_NAME = "Sun Java 6 (JCE)";

   /** Field PROFILE_CODE. (value is ""jce6"") */
   private static final String PROFILE_CODE = "jce6";

   /** Field PROFILE_LOCATION. (value is ""jce.jar"") */
   private static final String PROFILE_LOCATION = "jce.jar";

   /** Field ARCHIVE_TYPE. (value is ArchiveType.JAR) */
   private static final ArchiveType ARCHIVE_TYPE = ArchiveType.JAR;

   /** Field CLASSFILE_VERSION. (value type is ClassFile.JAVA_6) */
   private static final int CLASSFILE_VERSION = ClassFile.JAVA_6;

   /** Constructor */
   public SunJava6JCE()
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
