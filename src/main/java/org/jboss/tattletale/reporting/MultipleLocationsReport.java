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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Multiple locations report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class MultipleLocationsReport extends AbstractReport
{
   /** NAME */
   private static final String NAME = "Multiple Locations";

   /** DIRECTORY */
   private static final String DIRECTORY = "multiplelocations";

   /** Constructor */
   public MultipleLocationsReport()
   {
      super(DIRECTORY, ReportSeverity.WARNING, NAME, DIRECTORY);
   }

   /**
    * write out the report's content
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr>" + Dump.newLine());
      bw.write("    <th>Archive</th>" + Dump.newLine());
      bw.write("    <th>Location</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());
      recursivelyWriteContent(bw, archives);
      bw.write("</table>" + Dump.newLine());
   }

   /**
    * Method recursivelyWriteContent.
    * @param bw BufferedWriter
    * @param archives Collection<Archive>
    * @throws IOException
    */
   private void recursivelyWriteContent(BufferedWriter bw, Collection<Archive> archives) throws IOException
   {
      boolean odd = true;

      for (Archive a : archives)
      {
         if (a instanceof NestableArchive)
         {
            NestableArchive nestableArchive = (NestableArchive) a;
            recursivelyWriteContent(bw, nestableArchive.getSubArchives());
            continue;
         }

         SortedSet<Location> locations = a.getLocations();
         if (locations.size() > 1)
         {
            if (odd)
            {
               bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
            }
            else
            {
               bw.write("  <tr class=\"roweven\">" + Dump.newLine());
            }

            bw.write("    <td>" + hrefToArchiveReport(a) + "</td>" + Dump.newLine());

            if (!isFiltered(a.getName()))
            {
               status = ReportStatus.YELLOW;
               bw.write("    <td>");
            }
            else
            {
               bw.write("    <td style=\"text-decoration: line-through;\">");
            }
            List<String> files = new ArrayList<String>();
            for (Location location : locations)
            {
               files.add(location.getFilename());
            }
            bw.write(join(files, "<br/>"));
            bw.write("</td>" + Dump.newLine());

            bw.write("  </tr>" + Dump.newLine());

            odd = !odd;
         }
      }
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
