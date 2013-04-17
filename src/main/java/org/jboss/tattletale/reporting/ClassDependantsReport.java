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
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class level Dependants report
 *
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 */
public class ClassDependantsReport extends CLSReport
{
   /** NAME */
   private static final String NAME = "Class Dependants";

   /** DIRECTORY */
   private static final String DIRECTORY = "classdependants";

   /** Constructor */
   public ClassDependantsReport()
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
      bw.write("    <th>Class</th>" + Dump.newLine());
      bw.write("    <th>Dependants</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      SortedMap<String, SortedSet<String>> result = new TreeMap<String, SortedSet<String>>();

      boolean odd = true;

      for (Archive archive : archives)
      {
         SortedMap<String, SortedSet<String>> classDependencies = getClassDependencies(archive);

         for (Map.Entry<String, SortedSet<String>> entry : classDependencies.entrySet())
         {
            String clz = entry.getKey();

            for (String dep : entry.getValue())
            {
               if (!dep.equals(clz))
               {
                  boolean include = true;

                  for (Profile profile : getKnown())
                  {
                     if (profile.doesProvide(dep))
                     {
                        include = false;
                        break;
                     }
                  }

                  if (include)
                  {
                     SortedSet<String> deps = result.get(dep);

                     if (deps == null)
                     {
                        deps = new TreeSet<String>();
                     }

                     deps.add(clz);

                     result.put(dep, deps);
                  }
               }
            }
         }
      }

      for (Map.Entry<String, SortedSet<String>> entry : result.entrySet())
      {
         String clz = entry.getKey();
         SortedSet<String> deps = entry.getValue();

         if (deps != null && deps.size() > 0)
         {
            if (odd)
            {
               bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
            }
            else
            {
               bw.write("  <tr class=\"roweven\">" + Dump.newLine());
            }
            bw.write("    <td>" + clz + "</a></td>" + Dump.newLine());
            bw.write("    <td>");

            StringBuffer list = new StringBuffer();
            for (String dep : deps)
            {
               list.append(dep).append(", ");
            }
            list.setLength(list.length() - 2);
            bw.write(list.toString());

            bw.write("</td>" + Dump.newLine());
            bw.write("  </tr>" + Dump.newLine());

            odd = !odd;
         }
      }

      bw.write("</table>" + Dump.newLine());
   }

   private SortedMap<String, SortedSet<String>> getClassDependencies(Archive archive)
   {
      SortedMap<String, SortedSet<String>> classDeps = new TreeMap<String, SortedSet<String>>();

      if (archive instanceof NestableArchive)
      {
         NestableArchive nestableArchive = (NestableArchive) archive;

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
