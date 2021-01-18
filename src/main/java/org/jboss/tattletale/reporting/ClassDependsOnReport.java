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
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Class level Depends On report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ClassDependsOnReport extends CLSReport
{
   /** NAME */
   private static final String NAME = "Class Depends On";

   /** DIRECTORY */
   private static final String DIRECTORY = "classdependson";

   /** Constructor */
   public ClassDependsOnReport()
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
      bw.write("    <th>Class</th>" + Dump.newLine());
      bw.write("    <th>Depends On</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      final SortedMap<String, SortedSet<String>> result = new TreeMap<String, SortedSet<String>>();

      for (Archive archive : archives)
      {
         SortedMap<String, SortedSet<String>> classDependencies = getClassDependencies(archive);

         for (Map.Entry<String, SortedSet<String>> entry : classDependencies.entrySet())
         {
            String clz = entry.getKey();
            SortedSet<String> newDeps = new TreeSet<String>();

            for (String dep : entry.getValue())
            {
               if (!dep.equals(clz))
               {
                  newDeps.add(dep);
               }
            }

            result.put(clz, newDeps);
         }
      }

      boolean odd = true;

      for (Map.Entry<String, SortedSet<String>> entry : result.entrySet())
      {
         if (odd)
         {
            bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
         }
         else
         {
            bw.write("  <tr class=\"roweven\">" + Dump.newLine());
         }

         bw.write("    <td>" + entry.getKey() + "</td>" + Dump.newLine());

         bw.write("    <td>");
         SortedSet<String> deps = entry.getValue();
         if (null != deps && deps.size() > 0)
         {
            bw.write(join(deps, ", "));
         }
         else
         {
            bw.write("&nbsp;");
         }
         bw.write("</td>" + Dump.newLine());

         bw.write("  </tr>" + Dump.newLine());

         odd = !odd;
      }

      bw.write("</table>" + Dump.newLine());
   }

   /**
    * Method getClassDependencies.
    * @param archive Archive
    * @return SortedMap&lt;String,SortedSet&lt;String&gt;&gt;
    */
   private SortedMap<String, SortedSet<String>> getClassDependencies(Archive archive)
   {
      final SortedMap<String, SortedSet<String>> classDeps = new TreeMap<String, SortedSet<String>>();

      if (archive instanceof NestableArchive)
      {
         final NestableArchive nestableArchive = (NestableArchive) archive;

         for (Archive sa : nestableArchive.getSubArchives())
         {
            classDeps.putAll(getClassDependencies(sa));
         }

         classDeps.putAll(nestableArchive.getClassDependencies());
      }
      else
      {
         classDeps.putAll(archive.getClassDependencies());
      }
      return classDeps;
   }
}
