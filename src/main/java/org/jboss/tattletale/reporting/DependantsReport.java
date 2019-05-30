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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Dependants report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class DependantsReport extends CLSReport
{
   /** NAME */
   private static final String NAME = "Dependants";

   /** DIRECTORY */
   private static final String DIRECTORY = "dependants";

   /** Constructor */
   public DependantsReport()
   {
      super(DIRECTORY, ReportSeverity.INFO, NAME, DIRECTORY);
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
      bw.write("    <th>Dependants</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      boolean odd = true;

      for (Archive archive : archives)
      {
         if (odd)
         {
            bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
         }
         else
         {
            bw.write("  <tr class=\"roweven\">" + Dump.newLine());
         }

         bw.write("    <td>" + hrefToArchiveReport(archive) + "</td>" + Dump.newLine());

         bw.write("    <td>");

         SortedSet<String> result = new TreeSet<String>();
         for (Archive a : archives)
         {
            for (String require : getRequires(archive))
            {
               if (archive.doesProvide(require) && (null == getCLS() || getCLS().isVisible(a, archive)))
               {
                  result.add(a.getName());
               }
            }
         }

         if (0 == result.size())
         {
            bw.write("&nbsp;");
         }
         else
         {
            List<String> hrefs = new ArrayList<String>();
            for (String r : result)
            {
               hrefs.add((r.endsWith(".jar")) ? hrefToReport(r) : "<i>" + r + "</i>");
            }
            bw.write(join(hrefs, ", "));
         }

         bw.write("</td>" + Dump.newLine());

         bw.write("  </tr>" + Dump.newLine());

         odd = !odd;
      }

      bw.write("</table>" + Dump.newLine());
   }

   /**
    * Method getRequires.
    * @param archive Archive
    * @return SortedSet<String>
    */
   private SortedSet<String> getRequires(Archive archive)
   {
      final SortedSet<String> requires = new TreeSet<String>();
      if (archive instanceof NestableArchive)
      {
         final NestableArchive nestableArchive = (NestableArchive) archive;
         requires.addAll(nestableArchive.getRequires());
         for (Archive sa : nestableArchive.getSubArchives())
         {
            requires.addAll(getRequires(sa));
         }
      }
      else
      {
         requires.addAll(archive.getRequires());
      }
      return requires;
   }
}
