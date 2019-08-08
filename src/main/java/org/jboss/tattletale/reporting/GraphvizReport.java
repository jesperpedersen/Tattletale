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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.ArchiveType;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Graphviz report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class GraphvizReport extends CLSReport
{
   /** NAME */
   private static final String NAME = "Graphical Dependencies";

   /** DIRECTORY */
   private static final String DIRECTORY = "graphviz";

   /** Enable dot */
   private boolean enableDot;

   /** Path to the dot application */
   private String graphvizDot;

   /** Path to the tred application */
   private String graphvizTred;

   /** dot application output format */
   private String convertDotToPic;

   /** Constructor */
   public GraphvizReport()
   {
      super(DIRECTORY, ReportSeverity.INFO, NAME, DIRECTORY);

      enableDot = true;
      graphvizDot = "dot";
      convertDotToPic = "svg";
   }

   /**
    * Set the configuration properties to use in generating the report
    * @param config The configuration properties
    */
   public void setConfig(Properties config)
   {
      enableDot = Boolean.parseBoolean(config.getProperty("enableDot", "true"));
      graphvizDot = config.getProperty("graphvizDot", "dot");
      graphvizTred = config.getProperty("graphvizTred", "tred");
      convertDotToPic = config.getProperty("convertDotToPic", "svg");
   }

   /**
    * write out the report's content
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      final boolean hasDot = testDot();

      bw.write("<a href=\"dependencies.dot\">All dependencies</a>");
      if (hasDot)
      {
         bw.write("&nbsp;");
         bw.write("(<a href=\"dependencies." + convertDotToPic + "\">." + convertDotToPic + "</a>)");
      }
      bw.write("&nbsp;|&nbsp;<a href=\"reduced.dot\">All dependencies (reduced transitively)</a>");
      if (hasDot)
      {
         bw.write("&nbsp;");
         bw.write("(<a href=\"reduced." + convertDotToPic + "\">." + convertDotToPic + "</a>)");
      }

      bw.write(Dump.newLine() + "<table>" + Dump.newLine());

      bw.write("  <tr>" + Dump.newLine());
      bw.write("    <th>Archive</th>" + Dump.newLine());
      bw.write("    <th>Archives</th>" + Dump.newLine());
      bw.write("    <th>Packages</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      final String alldotName = getOutputDirectory().getAbsolutePath() + File.separator + "dependencies.dot";
      final FileWriter alldotfw = new FileWriter(alldotName);
      final BufferedWriter alldotw = new BufferedWriter(alldotfw, 8192);

      alldotw.write("digraph dependencies {" + Dump.newLine());
      alldotw.write("  node [shape=box, fontsize=10.0];" + Dump.newLine());

      boolean odd = true;

      for (Archive archive : archives)
      {
         String archiveName = archive.getName();

         if (odd)
         {
            bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
         }
         else
         {
            bw.write("  <tr class=\"roweven\">" + Dump.newLine());
         }
         bw.write("    <td>" + hrefToArchiveReport(archive) + "</td>" + Dump.newLine());

         // Archive level dependencies
         bw.write("    <td>");

         SortedSet<String> result = new TreeSet<String>();

         for (String require : getRequires(archive))
         {
            for (Archive a : archives)
            {
               if (a.doesProvide(require) && (null == getCLS() || getCLS().isVisible(archive, a)))
               {
                  result.add(a.getName());
                  break;
               }
            }
         }

         if (0 == result.size())
         {
            bw.write("&nbsp;");
         }
         else
         {
            bw.write("<a href=\"" + archiveName + "/" + archiveName + ".dot\">.dot</a>");
            if (hasDot)
            {
               bw.write("&nbsp;");
               bw.write("<a href=\"" + archiveName + "/" + archiveName + "."
                        + convertDotToPic + "\">." + convertDotToPic + "</a>");
            }

            File doutput = new File(getOutputDirectory(), archiveName);
            doutput.mkdirs();

            String dotName = doutput.getAbsolutePath() + File.separator + archiveName + ".dot";

            FileWriter dotfw = new FileWriter(dotName);
            BufferedWriter dotw = new BufferedWriter(dotfw, 8192);

            dotw.write("digraph " + dotName(archiveName) + "_dependencies {" + Dump.newLine());
            dotw.write("  node [shape=box, fontsize=10.0];" + Dump.newLine());

            for (String aResult : result)
            {
               alldotw.write("  " + dotName(archiveName) + " -> " + dotName(aResult) + ";" + Dump.newLine());
               dotw.write("  " + dotName(archiveName) + " -> " + dotName(aResult) + ";" + Dump.newLine());
            }

            dotw.write("}" + Dump.newLine());

            dotw.flush();
            dotw.close();

            if (enableDot && hasDot)
            {
               generatePicture(dotName, doutput);
            }
         }

         bw.write("</td>" + Dump.newLine());
         // Package level dependencies
         bw.write("    <td>");

         if (0 == archive.getPackageDependencies().size())
         {
            bw.write("&nbsp;");
         }
         else
         {
            bw.write("<a href=\"" + archiveName + "/" + archiveName + "-package.dot\">.dot</a>");
            if (hasDot)
            {
               bw.write("&nbsp;");
               bw.write("<a href=\"" + archiveName + "/" + archiveName + "-package."
                        + convertDotToPic + "\">." + convertDotToPic + "</a>");
            }

            File doutput = new File(getOutputDirectory(), archiveName);
            doutput.mkdirs();

            String dotName = doutput.getAbsolutePath() + File.separator + archiveName + "-package.dot";

            FileWriter dotfw = new FileWriter(dotName);
            BufferedWriter dotw = new BufferedWriter(dotfw, 8192);

            dotw.write("digraph " + dotName(archiveName) + "_package_dependencies {" + Dump.newLine());
            dotw.write("  node [shape=box, fontsize=10.0];" + Dump.newLine());

            for (Map.Entry<String, SortedSet<String>> entry : archive.getPackageDependencies().entrySet())
            {
               String pkg = dotName(entry.getKey());

               for (String dep : entry.getValue())
               {
                  dotw.write("  " + pkg + " -> " + dotName(dep) + ";" + Dump.newLine());
               }
            }

            dotw.write("}" + Dump.newLine());

            dotw.flush();
            dotw.close();

            if (enableDot && hasDot)
            {
               generatePicture(dotName, doutput);
            }
         }

         bw.write("</td>" + Dump.newLine());
         bw.write("  </tr>" + Dump.newLine());

         odd = !odd;
      }

      alldotw.write("}" + Dump.newLine());

      alldotw.flush();
      alldotw.close();

      if (enableDot && hasDot)
      {
         generatePicture(alldotName, getOutputDirectory());
         final String reducedName = alldotName.replace("dependencies.", "reduced.");
         reduceGraph(alldotName, reducedName, getOutputDirectory());
         generatePicture(reducedName, getOutputDirectory());
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

         for (Archive sa : nestableArchive.getSubArchives())
         {
            requires.addAll(getRequires(sa));
         }
         requires.addAll(nestableArchive.getRequires());
      }
      else if (archive.getType() != ArchiveType.CLASS)
      {
         // CLASS is a placeholder -> no op
         requires.addAll(archive.getRequires());
      }
      return requires;
   }

   /**
    * The dot name for an archive
    * @param name The name
    * @return The dot name
    */
   private String dotName(String name)
   {
      final int idx = name.indexOf(".jar");
      if (idx != -1)
      {
         name = name.substring(0, idx);
      }

      return name.replace('-', '_').replace('.', '_');
   }

   /**
    * Test for the dot application
    * @return boolean
    */
   private boolean testDot()
   {
      try
      {
         final ProcessBuilder pb = new ProcessBuilder();
         pb.command(graphvizDot, "-V");

         final Process proc = pb.redirectErrorStream(true).start();

         proc.waitFor();

         return 0 == proc.exitValue();
      }
      catch (InterruptedException ie)
      {
         Thread.interrupted();
      }
      catch (IOException ioe)
      {
         // Ignore
      }

      return false;
   }

   /**
    * Generate picture
    * @param dotName   The .dot file name
    * @param directory The working directory
    * @return boolean
    */
   private boolean generatePicture(String dotName, File directory)
   {
      try
      {
         final ProcessBuilder pb = new ProcessBuilder();
         pb.command(graphvizDot, "-T" + convertDotToPic, dotName,
               "-o", dotName.replaceFirst("dot$", convertDotToPic)).directory(directory);

         final Process proc = pb.redirectErrorStream(true).start();

         final BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
         for (String line; (line = out.readLine()) != null;)
         {
            System.err.println(line);
         }
         out.close();

         /*
         final BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
         for (String line; (line = err.readLine()) != null;)
         {
            System.err.println(line);
         }
         */

         proc.waitFor();

         return 0 == proc.exitValue();

      }
      catch (InterruptedException ie)
      {
         Thread.interrupted();
      }
      catch (IOException ioe)
      {
         System.err.println(ioe.getMessage());
      }

      return false;
   }

   /**
    * Reduce graph
    * @param dotName     The .dot file name
    * @param reducedName The .dot file name for reduced graph
    * @param directory   The working directory
    * @return boolean
    */
   private boolean reduceGraph(String dotName, String reducedName, File directory)
   {
      try
      {
         final ProcessBuilder pb = new ProcessBuilder();
         pb.command(graphvizTred, dotName).directory(directory);

         final Process proc = pb.redirectErrorStream(false).start();

         final BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
         final FileWriter redfw = new FileWriter(reducedName);
         final BufferedWriter redw = new BufferedWriter(redfw, 8192);
         for (String line; (line = out.readLine()) != null; )
         {
            redw.write(line + Dump.newLine());
         }
         out.close();
         redw.flush();
         redw.close();

         final BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
         for (String line; (line = err.readLine()) != null;)
         {
            System.err.println(line);
         }

         proc.waitFor();

         return 0 == proc.exitValue();

      }
      catch (InterruptedException ie)
      {
         Thread.interrupted();
      }
      catch (IOException ioe)
      {
         System.err.println(ioe.getMessage());
      }

      return false;
   }
}
