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

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.NestableArchive;
import org.jboss.tattletale.profiles.Profile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Depends On report
 *
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
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
    *
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
         String archiveName = archive.getName();
         int finalDot = archiveName.lastIndexOf(".");
         String extension = archiveName.substring(finalDot + 1);

         if (odd)
         {
            bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
         }
         else
         {
            bw.write("  <tr class=\"roweven\">" + Dump.newLine());
         }
         bw.write("    <td><a href=\"../" + extension + "/" + archiveName + ".html\">" +
               archiveName + "</a></td>" + Dump.newLine());
         bw.write("    <td>");

         SortedSet<String> result = new TreeSet<String>();

         for (String require : getRequires(archive))
         {
            boolean found = false;

            for (Archive a : archives)
            {
               if (a.doesProvide(require) && (getCLS() == null || getCLS().isVisible(archive, a)))
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

         if (result.size() == 0)
         {
            bw.write("&nbsp;");
         }
         else
         {
            StringBuffer list = new StringBuffer();
            for (String r : result)
            {
               if (r.endsWith(".jar") || r.endsWith(".war") || r.endsWith(".ear"))
               {
                  list.append("<a href=\"../" + extension + "/" + r + ".html\">" + r + "</a>");
               }
               else
               {
                  if (!isFiltered(archive.getName(), r))
                  {
                     list.append("<i>" + r + "</i>");
                     status = ReportStatus.YELLOW;
                  }
                  else
                  {
                     list.append("<i style=\"text-decoration: line-through;\">" + r + "</i>");
                  }
               }
               list.append(", ");
            }
            list.setLength(list.length() - 2);
            bw.write(list.toString());
         }

         bw.write("</td>" + Dump.newLine());
         bw.write("  </tr>" + Dump.newLine());

         odd = !odd;
      }

      bw.write("</table>" + Dump.newLine());
   }

   private SortedSet<String> getRequires(Archive archive)
   {
      SortedSet<String> requires = new TreeSet<String>();
      if (archive instanceof NestableArchive)
      {
         NestableArchive nestableArchive = (NestableArchive) archive;
         List<Archive> subArchives = nestableArchive.getSubArchives();
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
