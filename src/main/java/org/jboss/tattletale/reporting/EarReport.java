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

package org.jboss.tattletale.reporting;

import java.io.BufferedWriter;
import java.io.IOException;

import org.jboss.tattletale.core.NestableArchive;

/**
 * This type of report is to .ear files as to {@link JarReport} is to .jar files.
 * @author Navin Surtani
 */
public class EarReport extends NestableReport
{
   /** File name */
   private String fileName;

   /**
    * Constructor
    * @param archive  The archive
    */
   public EarReport(NestableArchive archive)
   {
      super(archive.getType().toString(), ReportSeverity.INFO, archive);
      setFilename(archive.getName() + ".html");
   }

   /**
    * Method getBufferedWriter.
    * @return BufferedWriter
    * @throws IOException if an error occurs
    */
   @Override
   protected BufferedWriter getBufferedWriter() throws IOException
   {
      return getBufferedWriter(getFilename());
   }

   /**
    * Method getFilename.
    * @return String
    */
   private String getFilename()
   {
      return fileName;
   }

   /**
    * Method setFilename.
    * @param fileName String
    */
   private void setFilename(String fileName)
   {
      this.fileName = fileName;
   }
}
