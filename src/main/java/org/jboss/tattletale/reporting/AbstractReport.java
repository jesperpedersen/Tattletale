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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.tattletale.Version;
import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.NestableArchive;

/**
 * Represents a report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 * @author Navin Surtani
 */
public abstract class AbstractReport implements Report
{
   /** Report id */
   private String id;

   /** The severity */
   protected final ReportSeverity severity;

   /** The status */
   protected ReportStatus status;

   /** The actions */
   protected SortedSet<Archive> archives;

   /** name of the report */
   private String name = null;

   /** output directory of the report */
   private String directory = null;

   /** output directory */
   private File outputDirectory;

   /** Filter */
   private String filter;

   /** Filter implementation */
   private Filter filterImpl;

   /** Index filename */
   protected static final String INDEX_HTML = "index.html";

   /** Index hyperlink name */
   protected static final String INDEX_LINK_NAME = "Main";

   /** Archive report paths */
   private final Map<String, String> reportPaths = new HashMap<String, String>();

   /**
    * Constructor
    * @param id       The report id
    * @param severity The severity
    */
   protected AbstractReport(String id, ReportSeverity severity)
   {
      this.id = id;
      this.severity = severity;
      status = ReportStatus.GREEN;
      filter = null;
      filterImpl = null;
   }

   /**
    * Constructor
    * @param id        The report id
    * @param severity  The severity
    * @param name      The name of the report
    * @param directory The name of the output directory
    */
   protected AbstractReport(String id, ReportSeverity severity, String name, String directory)
   {
      this(id, severity);
      this.name = name;
      this.directory = directory;
   }

   /**
    * Get the report id
    * @return The value
    * @see org.jboss.tattletale.reporting.Report#getId()
    */
   public String getId()
   {
      return id;
   }

   /**
    * Get the severity
    * @return The value
    * @see org.jboss.tattletale.reporting.Report#getSeverity()
    */
   public ReportSeverity getSeverity()
   {
      return severity;
   }

   /**
    * Get the status
    * @return The value
    * @see org.jboss.tattletale.reporting.Report#getStatus()
    */
   public ReportStatus getStatus()
   {
      return status;
   }

   /**
    * Get the name of the directory
    * @return The directory
    * @see org.jboss.tattletale.reporting.Report#getDirectory()
    */
   public String getDirectory()
   {
      return directory;
   }

   /**
    * Get the name of the report
    * @return The name
    * @see org.jboss.tattletale.reporting.Report#getName()
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the filter for the report
    * @return The filter
    * @see org.jboss.tattletale.reporting.Report#getFilter()
    */
   public String getFilter()
   {
      return filter;
   }

   /**
    * Set the filter for the report
    * @param filter The value
    * @see org.jboss.tattletale.reporting.Report#setFilter(String)
    */
   public void setFilter(String filter)
   {
      this.filter = filter;
      filterImpl = createFilter();
      filterImpl.init(filter);
   }

   /**
    * the output directory
    * @return a file handle to the output directory
    * @see org.jboss.tattletale.reporting.Report#getOutputDirectory()
    */
   public File getOutputDirectory()
   {
      return outputDirectory;
   }

   /**
    * The name of the index file to be used. See {@link Report} for examples.
    * @return name of the index file that is to contain Report data.
    * @see org.jboss.tattletale.reporting.Report#getIndexName()
    */
   public String getIndexName()
   {
      return INDEX_HTML;
   }

   /**
    * Generate the report(s)
    * @param outputDirectory The top-level output directory
    * @see org.jboss.tattletale.reporting.Report#generate(String)
    */
   public void generate(String outputDirectory)
   {
      try
      {
         createOutputDir(outputDirectory);
         final BufferedWriter bw = getBufferedWriter();

         writeHtmlHead(bw);

         writeHtmlBodyHeader(bw);
         writeHtmlBodyContent(bw);
         writeHtmlBodyFooter(bw);

         writeHtmlFooter(bw);

         bw.flush();
         bw.close();
      }
      catch (IOException ioe)
      {
         System.err.println(getName() + " Report: " + ioe.getMessage());
         ioe.printStackTrace(System.err);
      }
   }

   /**
    * create the output directory
    * @param outputDirectory the name of the directory
    */
   protected void createOutputDir(String outputDirectory)
   {
      this.outputDirectory = new File(outputDirectory, getDirectory());
      this.outputDirectory.mkdirs();
   }

   /**
    * get a default writer for writing to an index html file.
    * @return a buffered writer
    * @throws IOException if an error occurs
    */
   protected BufferedWriter getBufferedWriter() throws IOException
   {
      return getBufferedWriter(INDEX_HTML);
   }

   /**
    * get a writer.
    * @param filename the filename to use
    * @return a buffered writer
    * @throws IOException if an error occurs
    */
   public BufferedWriter getBufferedWriter(String filename) throws IOException
   {
      final FileWriter fw = new FileWriter(getOutputDirectory().getAbsolutePath() + File.separator + filename);
      return new BufferedWriter(fw, 8192);
   }

   /**
    * Set the archives to be represented by this report
    * @param archives The archives represented by this report
    */
   public void setArchives(SortedSet<Archive> archives)
   {
      this.archives = archives;
   }

   /**
    * write the header of a html file.
    * @param bw the buffered writer
    * @throws IOException if an error occurs
    */
   public void writeHtmlHead(BufferedWriter bw) throws IOException
   {
      writeHtmlHead(bw, 1);
   }

   /**
    * write the header of a html file.
    * @param bw the buffered writer
    * @param depth the level of depth at which this report would lie
    * @throws IOException if an error occurs
    */
   public void writeHtmlHead(BufferedWriter bw, int depth) throws IOException
   {
      bw.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" " +
               "\"http://www.w3.org/TR/html4/loose.dtd\">" + Dump.newLine());
      bw.write("<html>" + Dump.newLine());
      bw.write("<head>" + Dump.newLine());
      bw.write("  <title>" + getName() + ": " + Version.FULL_VERSION + "</title>" + Dump.newLine());
      bw.write("  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>" + Dump.newLine());
      bw.write("  <link rel=\"stylesheet\" type=\"text/css\" href=\"");
      for (int i = 1; i <= depth; i++)
      {
         bw.write("../");
      }
      bw.write("style.css\"/>" + Dump.newLine());
      bw.write("</head>" + Dump.newLine());
   }

   /**
    * write out the header of the report's content
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlBodyHeader(BufferedWriter bw) throws IOException
   {
      bw.write("<body>" + Dump.newLine());
      bw.write(Dump.newLine());

      bw.write("<h1>" + getName() + "</h1>" + Dump.newLine());

      bw.write("<a href=\"../" + getIndexName() + "\">" + getIndexLinkName() + "</a>" + Dump.newLine());
      bw.write("<br style=\"clear:both;\"/>" + Dump.newLine());
   }

   /**
    * The name of the hyperlink to the index file
    * @return name of the hyperlink
    */
   protected String getIndexLinkName()
   {
      return INDEX_LINK_NAME;
   }

   /**
    * write out the report's content
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public abstract void writeHtmlBodyContent(BufferedWriter bw) throws IOException;

   /**
    * write out the footer of the report's content
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlBodyFooter(BufferedWriter bw) throws IOException
   {
      bw.write(Dump.newLine());
      bw.write("<br style=\"clear:both;\"/>" + Dump.newLine());
      bw.write("<hr/>" + Dump.newLine());
      bw.write("Generated by: <a href=\"http://www.jboss.org/tattletale\">" +
               Version.FULL_VERSION + "</a>" + Dump.newLine());
      bw.write(Dump.newLine());
      bw.write("</body>" + Dump.newLine());
   }

   /**
    * write out the footer of the html page.
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlFooter(BufferedWriter bw) throws IOException
   {
      bw.write("</html>" + Dump.newLine());
   }

   /**
    * Comparable
    * @param r The other report
    * @return The compareTo value
    */
   public int compareTo(Report r)
   {
      if (severity.ordinal() == r.getSeverity().ordinal())
      {
         return getName().compareTo(r.getName());
      }
      else if (severity.ordinal() < r.getSeverity().ordinal())
      {
         return -1;
      }
      else
      {
         return 1;
      }
   }

   /**
    * Equals
    * @param obj The other object
    * @return True if equals; otherwise false
    */
   public boolean equals(Object obj)
   {
      return obj instanceof Report
              && getName().equals(((AbstractReport) obj).getName());
   }

   /**
    * Hash code
    * @return The hash code
    */
   public int hashCode()
   {
      return 7 + 31 * getName().hashCode();
   }

   /**
    * Create filter
    * @return The filter
    */
   protected Filter createFilter()
   {
      return new KeyValueFilter();
   }

   /**
    * Is filtered
    * @return True if filtered; otherwise false
    */
   protected boolean isFiltered()
   {
      return null != filterImpl && filterImpl.isFiltered();
   }

   /**
    * Is filtered
    * @param archive The archive
    * @return True if filtered; otherwise false
    */
   protected boolean isFiltered(String archive)
   {
      return null != filterImpl && filterImpl.isFiltered(archive);
   }

   /**
    * Is filtered
    * @param archive The archive
    * @param query   The query
    * @return True if filtered; otherwise false
    */
   protected boolean isFiltered(String archive, String query)
   {
      return null != filterImpl && filterImpl.isFiltered(archive, query);
   }

   /**
    * Return path to archive report
    * @param archiveName ditto
    * @return a relative path
    */
   protected String pathToReport(String archiveName)
   {
      final SortedSet<Archive> archs = new TreeSet<Archive>(archives);
      final SortedSet<Archive> subarchs = new TreeSet<Archive>();

      while (!reportPaths.containsKey(archiveName))
      {
         for (Archive a : archs)
         {
            if (a.getName().equals(archiveName))
            {
               updateReportPaths(a);
               break;
            }
            if (a instanceof NestableArchive)
            {
               NestableArchive na = (NestableArchive) a;
               subarchs.addAll(na.getSubArchives());
            }
         }
         archs.clear();
         archs.addAll(subarchs);
         subarchs.clear();
      }
      return reportPaths.get(archiveName);
   }

   /**
    * Figure out a path to Archive report and save it
    * @param archive ditto
    */
   private void updateReportPaths(Archive archive)
   {
      final String archiveName = archive.getName();
      String extension = archive.getType().toString();
      for (Archive parent; (parent = archive.getParentArchive()) != null; archive = parent)
      {
         extension = String.format("%s/%s", parent.getType().toString(), extension);
      }
      reportPaths.put(archiveName, extension);
   }

   /**
    * Return href tag for Archive report
    * @param archive ditto
    * @return a href tag
    */
   protected String hrefToArchiveReport(Archive archive)
   {
      return hrefToArchiveReport(archive, false);
   }

   /**
    * Return href tag for Archive report
    * @param archive ditto
    * @param here current directory is top directory when true
    * @return a href tag
    */
   protected String hrefToArchiveReport(Archive archive, boolean here)
   {
      final String topDirectory = (here) ? "./" : "../";
      final String archiveName = archive.getName();
      if (!reportPaths.containsKey(archiveName))
      {
         updateReportPaths(archive);
      }
      return "<a href=\"" + topDirectory + reportPaths.get(archiveName) + "/" + archiveName + ".html\">" +
         archiveName + "</a>";
   }
   
   /**
    * Return href tag for archive report
    * @param archiveName ditto
    * @return a href tag
    */
   protected String hrefToReport(String archiveName)
   {
      return hrefToReport(archiveName, false);
   }

   /**
    * Return href tag for archive report
    * @param archiveName ditto
    * @param star add a note mark if true
    * @return a href tag
    */
   protected String hrefToReport(String archiveName, boolean star)
   {
      final String note = (star) ? " (*)" : "";
      return "<a href=\"../" + pathToReport(archiveName) + "/" + archiveName + ".html\">" +
         archiveName + note + "</a>";
   }

   /**
    * Method join.
    * @param input SortedSet&lt;String&gt;
    * @param joiner String
    * @return String
    */
   protected String join(SortedSet<String> input, String joiner)
   {
      if (null == input)
      {
         return "";
      }
      return join(new ArrayList<String>(input), joiner);
   }

   /**
    * Method join.
    * @param input List&lt;String&gt;
    * @param joiner String
    * @return String
    */
   protected String join(List<String> input, String joiner)
   {
      if (null == input || 0 == input.size())
      {
         return "";
      }
      if (null == joiner)
      {
         joiner = "";
      }
      final StringBuilder list = new StringBuilder();
      for (String m : input)
      {
         list.append(m).append(joiner);
      }
      list.setLength(list.length() - joiner.length());
      return list.toString();
   }
}
