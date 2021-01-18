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
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Blacklisted report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class BlackListedReport extends AbstractReport
{
   /** NAME */
   private static final String NAME = "Blacklisted";

   /** DIRECTORY */
   private static final String DIRECTORY = "blacklisted";

   /** Constructor */
   public BlackListedReport()
   {
      super(DIRECTORY, ReportSeverity.ERROR, NAME, DIRECTORY);
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
      bw.write("    <th>Usage</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      boolean odd = true;

      for (Archive archive : archives)
      {
         SortedMap<String, SortedSet<String>> blacklisted = getBlackListedDeps(archive);
         boolean filtered = isFiltered(archive.getName());

         if (blacklisted.size() > 0)
         {
            if (!filtered)
            {
               status = ReportStatus.RED;
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
            bw.write("    <td>" + Dump.newLine());

            bw.write("      <table>" + Dump.newLine());

            for (Map.Entry<String, SortedSet<String>> stringSortedSetEntry : blacklisted.entrySet())
            {
               bw.write("      <tr>" + Dump.newLine());

               bw.write("        <td>" + stringSortedSetEntry.getKey() + "</td>" + Dump.newLine());

               if (!filtered)
               {
                  bw.write("      <td>");
               }
               else
               {
                  bw.write("      <td style=\"text-decoration: line-through;\">");
               }
               bw.write(join(stringSortedSetEntry.getValue(), "<br/>"));
               bw.write("</td>" + Dump.newLine());

               bw.write("      </tr>" + Dump.newLine());
            }

            bw.write("      </table>" + Dump.newLine());

            bw.write("    </td>" + Dump.newLine());
            bw.write("  </tr>" + Dump.newLine());

            odd = !odd;
         }
      }
      bw.write("</table>" + Dump.newLine());
   }

   /**
    * Method getBlackListedDeps.
    * @param a Archive
    * @return SortedMap&lt;String,SortedSet&lt;String&gt;&gt;
    */
   private SortedMap<String, SortedSet<String>> getBlackListedDeps(Archive a)
   {
      final SortedMap<String, SortedSet<String>> deps = new TreeMap<String, SortedSet<String>>();
      if (a instanceof NestableArchive)
      {
         final NestableArchive na = (NestableArchive) a;

         for (Archive sa : na.getSubArchives())
         {
            deps.putAll(getBlackListedDeps(sa));
         }
      }
      else
      {
         deps.putAll(a.getBlackListedDependencies());
      }
      return deps;
   }

   /**
    * Create filter
    *
    * @return The filter
    */
   @Override
   protected Filter createFilter()
   {
      return new KeyFilter();
   }
}
