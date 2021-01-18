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
import org.jboss.tattletale.core.ArchiveType;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Report type used when generating an {@link ArchiveReport} for a {@link org.jboss.tattletale.core.NestableArchive}.
 *
 * @author Navin Surtani
 */
public abstract class NestableReport extends ArchiveReport
{
   /** Field nestableArchive. */
   private final NestableArchive nestableArchive;

   /**
    * Constructor
    * @param id                  The report id
    * @param severity            The severity
    * @param nestableArchive     The nestable archive
    */
   protected NestableReport(String id, ReportSeverity severity, NestableArchive nestableArchive)
   {
      super(id, severity, nestableArchive);
      this.nestableArchive = nestableArchive;
   }

   /**
    * Method writeHtmlBodyContent.
    * @param bw BufferedWriter
    * @throws IOException if an error occurs
    */
   @Override
   public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Name</td>" + Dump.newLine());
      bw.write("    <td>" + nestableArchive.getName() + "</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"roweven\">" + Dump.newLine());
      bw.write("    <td>Class Version</td>" + Dump.newLine());
      bw.write("    <td>");

      switch (nestableArchive.getVersion())
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

      bw.write("    </td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Locations</td>" + Dump.newLine());
      bw.write("    <td>");

      bw.write("      <table>" + Dump.newLine());

      for (Location location : nestableArchive.getLocations())
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

      bw.write(join(nestableArchive.getProfiles(), "<br/>"));

      bw.write("    </td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Manifest</td>" + Dump.newLine());
      bw.write("    <td>");

      bw.write(join(nestableArchive.getManifest(), "<br/>"));

      bw.write("</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"roweven\">" + Dump.newLine());
      bw.write("    <td>Signing information</td>" + Dump.newLine());
      bw.write("    <td>");

      bw.write(join(nestableArchive.getSign(), "<br/>"));

      bw.write("</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Requires</td>" + Dump.newLine());
      bw.write("    <td>");

      bw.write(join(nestableArchive.getRequires(), "<br/>"));

      bw.write("</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      // Table of Provides.
      bw.write("  <tr class=\"roweven\">" + Dump.newLine());
      bw.write("    <td>Provides</td>" + Dump.newLine());
      bw.write("    <td>" + Dump.newLine());

      bw.write("      <table>" + Dump.newLine());

      for (Map.Entry<String, Long> entry : nestableArchive.getProvides().entrySet())
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

      // Sub-archives
      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Sub-Archives</td>" + Dump.newLine());
      bw.write("    <td>" + Dump.newLine());

      bw.write("      <table>" + Dump.newLine());

      // The base output path for all of the sub archives.
      final String outputPath = getOutputDirectory().getPath();

      for (Archive subArchive : nestableArchive.getSubArchives())
      {
         ArchiveReport report = null;
         int depth = 1;

         if (subArchive.getType() == ArchiveType.JAR)
         {
            if (null != subArchive.getParentArchive() && null != subArchive.getParentArchive().getParentArchive())
            {
               depth = 3;
            }
            else if (null != subArchive.getParentArchive())
            {
               depth = 2;
            }
            report = new JarReport(subArchive, depth);
         }
         else if (subArchive.getType() == ArchiveType.WAR)
         {
            NestableArchive nestedSubArchive = (NestableArchive) subArchive;

            if (null != subArchive.getParentArchive())
            {
               depth = 2;
            }
            report = new WarReport(nestedSubArchive, depth);
         }

         if (subArchive.getType() != ArchiveType.CLASS)
         {
            report.generate(outputPath);
            bw.write("        <tr>" + Dump.newLine());
            bw.write("          <td>" + hrefToArchiveReport(subArchive, true) + "</td>" + Dump.newLine());
            bw.write("        </tr>" + Dump.newLine());
         }
      }
      bw.write("      </table>" + Dump.newLine());
      bw.write("    </td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("</table>" + Dump.newLine());
   }
}
