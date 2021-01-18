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
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.ArchiveType;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Circular dependency report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class CircularDependencyReport extends CLSReport
{
   /** NAME */
   private static final String NAME = "Circular Dependency";

   /** DIRECTORY */
   private static final String DIRECTORY = "circulardependency";

   /** Constructor */
   public CircularDependencyReport()
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
      bw.write("    <th>Circular Dependencies</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      final SortedMap<String, SortedSet<String>> dependsOnMap = recursivelyBuildDependsOnFromArchive(archives);
      final SortedMap<String, SortedSet<String>> transitiveDependsOnMap = new TreeMap<String, SortedSet<String>>();
      for (Map.Entry<String, SortedSet<String>> entry : dependsOnMap.entrySet())
      {
         String archive = entry.getKey();
         SortedSet<String> result = new TreeSet<String>();

         for (String aValue : entry.getValue())
         {
            resolveDependsOn(aValue, archive, dependsOnMap, result);
         }

         transitiveDependsOnMap.put(archive, result);
      }

      boolean odd = true;

      for (Map.Entry<String, SortedSet<String>> entry : transitiveDependsOnMap.entrySet())
      {
         String archive = entry.getKey();
         SortedSet<String> value = entry.getValue();

         if (0 != value.size())
         {
            SortedSet<String> circular = new TreeSet<String>();

            for (String r : value)
            {
               SortedSet<String> td = transitiveDependsOnMap.get(r);
               if (null != td && td.contains(archive))
               {
                  circular.add(r);
               }
            }

            if (circular.size() > 0)
            {
               if (odd)
               {
                  bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
               }
               else
               {
                  bw.write("  <tr class=\"roweven\">" + Dump.newLine());
               }

               bw.write("    <td>" + hrefToReport(archive) + "</td>" + Dump.newLine());

               if (!isFiltered(archive))
               {
                  status = ReportStatus.RED;
                  bw.write("    <td>");
               }
               else
               {
                  bw.write("    <td style=\"text-decoration: line-through;\">");
               }
               List<String> hrefs = new ArrayList<String>();
               for (String r : value)
               {
                  hrefs.add(hrefToReport(r, circular.contains(r)));
               }
               bw.write(join(hrefs, ", "));
               bw.write("</td>" + Dump.newLine());

               bw.write("  </tr>" + Dump.newLine());

               odd = !odd;
            }
         }
      }

      bw.write("</table>" + Dump.newLine());
   }

   /**
    * Method recursivelyBuildDependsOnFromArchive.
    * @param archives Collection<Archive>
    * @return SortedMap&lt;String,SortedSet&lt;String&gt;&gt;
    */
   private SortedMap<String, SortedSet<String>> recursivelyBuildDependsOnFromArchive(Collection<Archive> archives)
   {
      final SortedMap<String, SortedSet<String>> dependsOnMap = new TreeMap<String, SortedSet<String>>();
      for (Archive archive : archives)
      {
         if (archive instanceof NestableArchive)
         {
            NestableArchive nestableArchive = (NestableArchive) archive;
            SortedMap<String, SortedSet<String>> subMap = recursivelyBuildDependsOnFromArchive(nestableArchive
                  .getSubArchives());
            dependsOnMap.putAll(subMap);
         }
         else
         {
            SortedSet<String> result = dependsOnMap.get(archive.getName());
            if (null == result)
            {
               result = new TreeSet<String>();
            }

            for (String require : archive.getRequires())
            {
               for (Archive a : archives)
               {
                  if (a.getType() == ArchiveType.JAR)
                  {
                     if (a.doesProvide(require) && (null == getCLS() || getCLS().isVisible(archive, a)))
                     {
                        result.add(a.getName());
                        break;
                     }
                  }
               }
            }

            dependsOnMap.put(archive.getName(), result);
         }
      }
      return dependsOnMap;
   }

   /**
    * Get depends on
    * @param scanArchive The scan archive
    * @param archive     The archive
    * @param map         The depends on map
    * @param result      The result
    */
   private void resolveDependsOn(String scanArchive, String archive,
                                 SortedMap<String, SortedSet<String>> map, SortedSet<String> result)
   {
      if (!archive.equals(scanArchive) && !result.contains(scanArchive))
      {
         result.add(scanArchive);

         final SortedSet<String> value = map.get(scanArchive);
         if (null != value)
         {
            for (String aValue : value)
            {
               resolveDependsOn(aValue, archive, map, result);
            }
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
