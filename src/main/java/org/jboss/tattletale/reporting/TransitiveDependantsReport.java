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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.ArchiveType;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Transitive dependants report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class TransitiveDependantsReport extends CLSReport
{
   /** NAME */
   private static final String NAME = "Transitive Dependants";

   /** DIRECTORY */
   private static final String DIRECTORY = "transitivedependants";

   /** Constructor */
   public TransitiveDependantsReport()
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

      final SortedMap<String, SortedSet<String>> dependantsMap = new TreeMap<String, SortedSet<String>>();

      for (Archive archive : archives)
      {
         SortedSet<String> result = new TreeSet<String>();

         for (Archive a : archives)
         {
            if (a.getType() == ArchiveType.JAR)
            {
               for (String require : getRequires(a))
               {
                  if (archive.doesProvide(require) && (null == getCLS() || getCLS().isVisible(a, archive)))
                  {
                     result.add(a.getName());
                  }
               }
            }
         }

         dependantsMap.put(archive.getName(), result);
      }

      final SortedMap<String, SortedSet<String>> transitiveDependantsMap = new TreeMap<String, SortedSet<String>>();

      for (Map.Entry<String, SortedSet<String>> entry : dependantsMap.entrySet())
      {
         String archive = entry.getKey();
         SortedSet<String> result = new TreeSet<String>();

         for (String aValue : entry.getValue())
         {
            resolveDependants(aValue, archive, dependantsMap, result);
         }

         transitiveDependantsMap.put(archive, result);
      }

      boolean odd = true;

      for (Map.Entry<String, SortedSet<String>> entry : transitiveDependantsMap.entrySet())
      {
         String archive = entry.getKey();
         SortedSet<String> value = entry.getValue();

         if (odd)
         {
            bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
         }
         else
         {
            bw.write("  <tr class=\"roweven\">" + Dump.newLine());
         }

         bw.write("    <td>" + hrefToReport(archive) + "</td>" + Dump.newLine());

         bw.write("    <td>");
         if (0 == value.size())
         {
            bw.write("&nbsp;");
         }
         else
         {
            List<String> hrefs = new ArrayList<String>();
            for (String r : value)
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
    * @param a Archive
    * @return Set<String>
    */
   private Set<String> getRequires(Archive a)
   {
      final Set<String> requires = new HashSet<String>();
      if (a instanceof NestableArchive)
      {
         final NestableArchive na = (NestableArchive) a;
         requires.addAll(na.getRequires());

         for (Archive sa : na.getSubArchives())
         {
            requires.addAll(getRequires(sa));
         }
      }
      else
      {
         requires.addAll(a.getRequires());
      }
      return requires;
   }

   /**
    * Get dependants
    * @param scanArchive The scan archive
    * @param archive     The archive
    * @param map         The dependants map
    * @param result      The result
    */
   private void resolveDependants(String scanArchive, String archive,
                                  SortedMap<String, SortedSet<String>> map, SortedSet<String> result)
   {
      if (!archive.equals(scanArchive) && !result.contains(scanArchive))
      {
         result.add(scanArchive);

         for (String aValue : map.get(scanArchive))
         {
            resolveDependants(aValue, archive, map, result);
         }
      }
   }
}
