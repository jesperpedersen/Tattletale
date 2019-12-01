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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.Location;

/**
 * OSGi report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class OSGiReport extends AbstractReport
{
   /** NAME */
   private static final String NAME = "OSGi";

   /** DIRECTORY */
   private static final String DIRECTORY = "osgi";

   /** subreportName */
   private String subreportName = "";

   /** Constructor */
   public OSGiReport()
   {
      super(DIRECTORY, ReportSeverity.INFO, NAME, DIRECTORY);
   }

   /**
    * Get the name of the report
    * @return The name
    * @see org.jboss.tattletale.reporting.Report#getName()
    */
   @Override
   public String getName()
   {
      return (subreportName.isEmpty()) ? NAME : NAME + " - " + subreportName;
   }

   /**
    * The name of the hyperlink to the index file
    * @return name of the hyperlink
    */
   @Override
   protected String getIndexLinkName()
   {
      return (subreportName.isEmpty()) ? INDEX_LINK_NAME : "Back";
   }

   /**
    * Generate the report(s)
    * @param outputDirectory The top-level output directory
    * @see org.jboss.tattletale.reporting.Report#generate(String)
    */
   @Override
   public void generate(String outputDirectory)
   {
      super.generate(outputDirectory);

      try
      {
         for (Archive archive : archives)
         {
            List<String> osgiInformation = getOSGIInfo(archive);

            File archiveOutput = writeArchiveOSGIHtml(archive, osgiInformation);

            writeArchiveOSGIManifest(archive, osgiInformation, archiveOutput);
         }
      }
      catch (IOException ioe)
      {
         System.err.println("OSGiReport: " + ioe.getMessage());
         ioe.printStackTrace(System.err);
      }
   }

   /**
    * Method writeArchiveOSGIManifest.
    * @param archive Archive
    * @param osgiInformation List<String>
    * @param archiveOutput File
    * @throws IOException
    */
   private void writeArchiveOSGIManifest(Archive archive, List<String> osgiInformation, File archiveOutput)
      throws IOException
   {
      final FileWriter mfw = new FileWriter(archiveOutput.getAbsolutePath() + File.separator + "MANIFEST.MF");
      final BufferedWriter mbw = new BufferedWriter(mfw, 8192);

      if (null != archive.getManifest())
      {
         for (String s : archive.getManifest())
         {
            mbw.write(s);
            mbw.write(Dump.newLine());
         }
      }

      if (!archive.isOSGi() && null != osgiInformation && osgiInformation.size() > 0)
      {
         mbw.write(Dump.newLine());
         mbw.write("### OSGi information" + Dump.newLine());
         for (String anOsgiInformation : osgiInformation)
         {
            if (anOsgiInformation.length() <= 69)
            {
               mbw.write(anOsgiInformation);
            }
            else
            {
               int count = 0;
               for (char c : anOsgiInformation.toCharArray())
               {
                  if (count <= 69)
                  {
                     mbw.write(c);
                     count++;
                  }
                  else
                  {
                     mbw.write(Dump.newLine());
                     mbw.write(' ');
                     mbw.write(c);
                     count = 2;
                  }
               }
            }
            mbw.write(Dump.newLine());
         }
      }

      mbw.flush();
      mbw.close();
   }

   /**
    * Method writeArchiveOSGIHtml.
    * @param archive Archive
    * @param osgiInformation List<String>
    * @return File
    * @throws IOException
    */
   private File writeArchiveOSGIHtml(Archive archive, List<String> osgiInformation) throws IOException
   {
      final File archiveOutput = new File(getOutputDirectory(), archive.getName());
      archiveOutput.mkdirs();
      subreportName = archive.getName();

      final FileWriter rfw = new FileWriter(archiveOutput.getAbsolutePath() + File.separator + "index.html");
      final BufferedWriter rbw = new BufferedWriter(rfw, 8192);
      writeHtmlHead(rbw, 2);
      writeHtmlBodyHeader(rbw);

      rbw.write("<table>" + Dump.newLine());

      rbw.write("  <tr>" + Dump.newLine());
      rbw.write("    <th>Field</th>" + Dump.newLine());
      rbw.write("    <th>Value</th>" + Dump.newLine());
      rbw.write("  </tr>" + Dump.newLine());

      rbw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      rbw.write("    <td>OSGi</td>" + Dump.newLine());
      if (archive.isOSGi())
      {
         rbw.write("    <td style=\"color: green;\">Yes</td>" + Dump.newLine());
      }
      else
      {
         rbw.write("    <td style=\"color: red;\">No</td>" + Dump.newLine());
      }
      rbw.write("  </tr>" + Dump.newLine());

      rbw.write("  <tr class=\"roweven\">" + Dump.newLine());
      rbw.write("    <td>Manifest</td>" + Dump.newLine());
      rbw.write("    <td><pre>");
      rbw.write(join(archive.getManifest(), "<br/>"));
      rbw.write("</pre></td>" + Dump.newLine());
      rbw.write("  </tr>" + Dump.newLine());

      if (!archive.isOSGi())
      {
         rbw.write("  <tr class=\"rowodd\">" + Dump.newLine());
         rbw.write("    <td>OSGi Manifest</td>" + Dump.newLine());
         rbw.write("    <td><pre>");
         rbw.write(join(osgiInformation, "<br/>"));
         rbw.write("</pre></td>" + Dump.newLine());
         rbw.write("  </tr>" + Dump.newLine());
      }

      rbw.write("</table>" + Dump.newLine());

      writeHtmlBodyFooter(rbw);
      writeHtmlFooter(rbw);
      rbw.flush();
      rbw.close();

      subreportName = "";
      return archiveOutput;
   }

   /**
    * Method getOSGIInfo.
    * @param archive Archive
    * @return List<String>
    */
   private List<String> getOSGIInfo(Archive archive)
   {
      List<String> osgiInformation = null;

      if (!archive.isOSGi())
      {
         osgiInformation = new ArrayList<String>();

         final SortedMap<String, Integer> exportPackages = new TreeMap<String, Integer>();

         for (String provide : archive.getProvides().keySet())
         {
            if (provide.lastIndexOf('.') != -1)
            {
               String packageName = provide.substring(0, provide.lastIndexOf('.'));
               Integer number = exportPackages.get(packageName);
               if (null == number)
               {
                  number = 0;
               }

               number += 1;
               exportPackages.put(packageName, number);
            }
         }

         final SortedMap<String, String> importPackages = new TreeMap<String, String>();

         for (String require : archive.getRequires())
         {
            if (require.lastIndexOf('.') != -1)
            {
               String packageName = require.substring(0, require.lastIndexOf('.'));

               if (null == importPackages.get(packageName))
               {
                  String version = null;

                  for (Archive a : archives)
                  {
                     if (a.doesProvide(require))
                     {
                        version = a.getLocations().first().getVersion();
                        if (null == version)
                        {
                           version = "0.0.0";
                        }
                        break;
                     }
                  }

                  importPackages.put(packageName, version);
               }
            }
         }

         osgiInformation.add("Bundle-ManifestVersion: 2");

         String bundleSymbolicName;
         if (exportPackages.size() > 0)
         {
            String bsn = null;
            Integer bsnv = null;

            for (Map.Entry<String, Integer> entry : exportPackages.entrySet())
            {
               String pkg = entry.getKey();
               Integer number = entry.getValue();

               if (null == bsn)
               {
                  bsn = pkg;
                  bsnv = number;
               }
               else
               {
                  if (number > bsnv)
                  {
                     bsn = pkg;
                     bsnv = number;
                  }
               }
            }

            bundleSymbolicName = bsn;
         }
         else
         {
            bundleSymbolicName = "UNKNOWN";
         }
         osgiInformation.add("Bundle-SymbolicName: " + bundleSymbolicName);

         final String bundleDescription = archive.getName().substring(0, archive.getName().lastIndexOf('.'));
         osgiInformation.add("Bundle-Description: " + bundleDescription);

         final String bName = archive.getName().substring(0, archive.getName().lastIndexOf('.'));
         final StringBuilder bundleName = new StringBuilder();
         for (char c : bName.toCharArray())
         {
            if ('\n' != c && '\r' != c && ' ' != c)
            {
               bundleName.append(c);
            }
         }
         osgiInformation.add("Bundle-Name: " + bundleName.toString());

         final Location location = archive.getLocations().first();
         final String bundleVersion = getOSGiVersion(location.getVersion());
         osgiInformation.add("Bundle-Version: " + bundleVersion);

         final StringBuilder exportPackage = new StringBuilder();
         for (String ep : exportPackages.keySet())
         {
            exportPackage.append(ep);

            SortedSet<String> epd = archive.getPackageDependencies().get(ep);
            if (null != epd && epd.size() > 0)
            {
               exportPackage.append(";uses:=\"").append(join(epd, ",")).append('\"');
            }
            exportPackage.append(',');
         }
         if (exportPackage.length() > 0)
         {
            exportPackage.setLength(exportPackage.length() - 1);
            osgiInformation.add("Export-Package: " + exportPackage.toString());
         }

         final StringBuilder importPackage = new StringBuilder();
         for (Map.Entry<String, String> entry : importPackages.entrySet())
         {
            importPackage.append(entry.getKey());

            String version = entry.getValue();
            if (null != version)
            {
               importPackage.append(";version=\"").append(getOSGiVersion(version)).append('\"');
            }

            importPackage.append(',');
         }
         if (importPackage.length() > 0)
         {
            importPackage.setLength(importPackage.length() - 1);
            osgiInformation.add("Import-Package: " + importPackage.toString());
         }
      }
      return osgiInformation;
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
      bw.write("    <th>OSGi</th>" + Dump.newLine());
      bw.write("    <th>Report</th>" + Dump.newLine());
      bw.write("    <th>Manifest</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      boolean odd = true;

      int osgiReady = 0;
      int osgiNotReady = 0;

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

         if (archive.isOSGi())
         {
            bw.write("    <td style=\"color: green;\">Yes</td>" + Dump.newLine());
            osgiReady++;
         }
         else
         {
            osgiNotReady++;

            if (!isFiltered(archiveName))
            {
               status = ReportStatus.RED;
               bw.write("    <td style=\"color: red;\">No</td>" + Dump.newLine());
            }
            else
            {
               bw.write("    <td style=\"color: red; text-decoration: line-through;\">No</td>" + Dump.newLine());
            }
         }
         bw.write("    <td><a href=\"" + archiveName + "/index.html\">Report</a></td>" + Dump.newLine());
         bw.write("    <td><a href=\"" + archiveName + "/MANIFEST.MF\">Manifest</a></td>" + Dump.newLine());
         bw.write("  </tr>" + Dump.newLine());

         odd = !odd;
      }

      bw.write("</table>" + Dump.newLine());

      bw.write(Dump.newLine());

      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr>" + Dump.newLine());
      bw.write("    <th>Status</th>" + Dump.newLine());
      bw.write("    <th>Archives</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("    <td>Ready</td>" + Dump.newLine());
      bw.write("    <td style=\"color: green;\">" + osgiReady + "</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"roweven\">" + Dump.newLine());
      bw.write("    <td>Not ready</td>" + Dump.newLine());
      bw.write("    <td style=\"color: red;\">" + osgiNotReady + "</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("</table>" + Dump.newLine());
   }

   /**
    * Get Bundle-Version
    * @param version The archive version
    * @return OSGi version
    */
   private String getOSGiVersion(String version)
   {
      if (version == null)
      {
         return "0.0.0";
      }

      if (!version.matches("\\d+(\\.\\d+(\\.\\d+(\\.[\\w\\-]+)?)?)?"))
      {
         return "0.0.0";
      }

      return version;
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
