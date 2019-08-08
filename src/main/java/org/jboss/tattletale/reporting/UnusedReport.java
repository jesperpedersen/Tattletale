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

import org.jboss.tattletale.core.Archive;

/**
 * A report that shows unused JAR archives
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class UnusedReport extends AbstractReport
{
   /** NAME */
   private static final String NAME = "Unused";

   /** DIRECTORY */
   private static final String DIRECTORY = "unused";

   /** Constructor */
   public UnusedReport()
   {
      super(DIRECTORY, ReportSeverity.WARNING, NAME, DIRECTORY);
   }

   /**
    * Write out the report's content
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr>" + Dump.newLine());
      bw.write("    <th>Archive</th>" + Dump.newLine());
      bw.write("    <th>Used</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      boolean odd = true;
      int used = 0;
      int unused = 0;

      for (Archive archive : archives)
      {
         boolean archiveStatus = false;
         String archiveName = archive.getName();

         for (Archive a : archives)
         {
            if (!archiveName.equals(a.getName()))
            {
               for (String require : a.getRequires())
               {
                  if (archive.getProvides().containsKey(require))
                  {
                     archiveStatus = true;
                     break;
                  }
               }
            }
            if (archiveStatus)
            {
               break;
            }
         }

         if (odd)
         {
            bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
         }
         else
         {
            bw.write("  <tr class=\"roweven\">" + Dump.newLine());
         }

         bw.write("    <td>" + hrefToArchiveReport(archive) + "</td>" + Dump.newLine());

         if (archiveStatus)
         {
            used++;
            bw.write("    <td style=\"color: green;\">Yes</td>" + Dump.newLine());
         }
         else
         {
            unused++;
            if (!isFiltered(archiveName))
            {
               status = ReportStatus.YELLOW;
               bw.write("    <td style=\"color: red;\">No</td>" + Dump.newLine());
            }
            else
            {
               bw.write("    <td style=\"color: red; text-decoration: line-through;\">No</td>" + Dump.newLine());
            }
         }

         bw.write("  </tr>" + Dump.newLine());

         odd = !odd;
      }

      bw.write("</table>" + Dump.newLine());

      bw.write(Dump.newLine());

      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr>" + Dump.newLine());
      bw.write("    <th>Status</th>" + Dump.newLine());
      bw.write("    <th>Archives</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Used</td>" + Dump.newLine());
      bw.write("    <td style=\"color: green;\">" + used + "</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"roweven\">" + Dump.newLine());
      bw.write("    <td>Unused</td>" + Dump.newLine());
      bw.write("    <td style=\"color: red;\">" + unused + "</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("</table>" + Dump.newLine());
   }

   /**
    * Create filter
    * @return The filter
    */
   @Override
   protected Filter createFilter()
   {
      return new KeyFilter();
   }
}
