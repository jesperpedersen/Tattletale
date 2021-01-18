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
import org.jboss.tattletale.profiles.Profile;

/**
 * Depends On report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class DependsOnReport extends CLSReport
{
   /** NAME */
   private static final String NAME = "Depends On";

   /** DIRECTORY */
   private static final String DIRECTORY = "dependson";

   /** Constructor */
   public DependsOnReport()
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
      bw.write("    <th>Depends On</th>" + Dump.newLine());
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
         for (String require : getRequires(archive))
         {
            boolean found = false;

            for (Archive a : archives)
            {
               if (a.doesProvide(require) && (null == getCLS() || getCLS().isVisible(archive, a)))
               {
                  result.add(a.getName());
                  found = true;
                  break;
               }
            }

            if (!found)
            {
               for (Profile profile : getKnown())
               {
                  if (profile.doesProvide(require))
                  {
                     found = true;
                     break;
                  }
               }
            }

            if (!found)
            {
               result.add(require);
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
               if (r.endsWith(".jar") || r.endsWith(".war") || r.endsWith(".rar") || r.endsWith(".ear"))
               {
                  hrefs.add(hrefToReport(r));
               }
               else
               {
                  if (!isFiltered(archive.getName(), r))
                  {
                     hrefs.add("<i>" + r + "</i>");
                     status = ReportStatus.YELLOW;
                  }
                  else
                  {
                     hrefs.add("<i style=\"text-decoration: line-through;\">" + r + "</i>");
                  }
               }
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
         final List<Archive> subArchives = nestableArchive.getSubArchives();
         for (Archive sa : subArchives)
         {
            requires.addAll(getRequires(sa));
         }

         requires.addAll(nestableArchive.getRequires());
      }
      else
      {
         requires.addAll(archive.getRequires());
      }
      return requires;
   }
}
