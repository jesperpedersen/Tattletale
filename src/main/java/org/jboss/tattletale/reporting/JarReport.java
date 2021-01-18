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
import java.util.Map;

import javassist.bytecode.ClassFile;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.Location;

/**
 * JAR report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class JarReport extends ArchiveReport
{
   /** File name */
   private String filename;

   /** The level of depth from the main output directory that this jar report would sit */
   private int depth;

   /**
    * Constructor
    * @param archive The archive
    */
   public JarReport(Archive archive)
   {
      this(archive, 1);
   }

   /**
    * Constructor
    * @param archive The archive
    * @param depth   The level of depth at which this report would lie
    */
   public JarReport(Archive archive, int depth)
   {
      super(archive.getType().toString(), ReportSeverity.INFO, archive);

      setFilename(archive.getName() + ".html");
      this.depth = depth;
   }

   /**
    * write the header of a html file.
    * @param bw the buffered writer
    * @throws IOException if an error occurs
    */
   @Override
   public void writeHtmlHead(BufferedWriter bw) throws IOException
   {
      super.writeHtmlHead(bw, depth);
   }

   /**
    * returns a Jar report specific writer.
    * Jar reports do not use an index but write one html file per archive.
    * @return the BufferedWriter
    * @throws IOException if an error occurs
    */
   @Override
   protected BufferedWriter getBufferedWriter() throws IOException
   {
      return getBufferedWriter(getFilename());
   }

   /**
    * write out the report's content
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Name</td>" + Dump.newLine());
      bw.write("    <td>" + archive.getName() + "</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"roweven\">" + Dump.newLine());
      bw.write("    <td>Class Version</td>" + Dump.newLine());
      bw.write("    <td>");

      switch (archive.getVersion())
      {
         case ClassFile.JAVA_9:
            bw.write("Java 9");
            break;
         case ClassFile.JAVA_8:
            bw.write("Java 8");
            break;
         case ClassFile.JAVA_7:
            bw.write("Java 7");
            break;
         case ClassFile.JAVA_6:
            bw.write("Java 6");
            break;
         case ClassFile.JAVA_5:
            bw.write("Java 5");
            break;
         case ClassFile.JAVA_4:
            bw.write("J2SE 1.4");
            break;
         case ClassFile.JAVA_3:
            bw.write("J2SE 1.3");
            break;
         case ClassFile.JAVA_2:
            bw.write("J2SE 1.2");
            break;
         case ClassFile.JAVA_1:
            bw.write("JSE 1.0 / JSE 1.1");
            break;
      }

      bw.write("</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Locations</td>" + Dump.newLine());
      bw.write("    <td>" + Dump.newLine());

      bw.write("      <table>" + Dump.newLine());

      for (Location location : archive.getLocations())
      {
         bw.write("      <tr>" + Dump.newLine());

         bw.write("        <td>" + location.getFilename() + "</td>" + Dump.newLine());
         bw.write("        <td>");
         if (null != location.getVersion())
         {
            bw.write(location.getVersion());
         }
         else
         {
            bw.write("<i>Not listed</i>");
         }
         bw.write("</td>" + Dump.newLine());

         bw.write("      </tr>" + Dump.newLine());
      }

      bw.write("      </table>" + Dump.newLine());

      bw.write("    </td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"roweven\">" + Dump.newLine());
      bw.write("    <td>Profiles</td>" + Dump.newLine());
      bw.write("    <td>");

      bw.write(join(archive.getProfiles(), "<br/>"));

      bw.write("</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Manifest</td>" + Dump.newLine());
      bw.write("    <td>");

      bw.write(join(archive.getManifest(), "<br/>"));

      bw.write("</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"roweven\">" + Dump.newLine());
      bw.write("    <td>Signing information</td>" + Dump.newLine());
      bw.write("    <td>");

      bw.write(join(archive.getSign(), "<br/>"));

      bw.write("</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Requires</td>" + Dump.newLine());
      bw.write("    <td>");

      bw.write(join(archive.getRequires(), "<br/>"));

      bw.write("</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"roweven\">" + Dump.newLine());
      bw.write("    <td>Provides</td>" + Dump.newLine());
      bw.write("    <td>" + Dump.newLine());

      bw.write("      <table>" + Dump.newLine());

      for (Map.Entry<String, Long> entry : archive.getProvides().entrySet())
      {
         Long serialVersionUID = entry.getValue();

         bw.write("        <tr>" + Dump.newLine());
         bw.write("          <td>" + entry.getKey() + "</td>" + Dump.newLine());

         if (null != serialVersionUID)
         {
            bw.write("          <td>" + serialVersionUID + "</td>" + Dump.newLine());
         }
         else
         {
            bw.write("          <td>&nbsp;</td>" + Dump.newLine());
         }
         bw.write("        </tr>" + Dump.newLine());
      }
      bw.write("      </table>" + Dump.newLine());

      bw.write("    </td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("</table>" + Dump.newLine());
   }

   /**
    * write out the header of the report's content
    * @return String
    */
   private String getFilename()
   {
      return filename;
   }

   /**
    * Method setFilename.
    * @param filename String
    */
   private void setFilename(String filename)
   {
      this.filename = filename;
   }
}
