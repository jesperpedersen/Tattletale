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
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.NestableArchive;
import org.jboss.tattletale.profiles.ExtendedProfile;
import org.jboss.tattletale.profiles.JBossAS7Profile;
import org.jboss.tattletale.profiles.Profile;

/**
 * Report type that makes use of the {@link org.jboss.tattletale.profiles.ExtendedProfile} to find which module
 * identifiers it needs for the scanned archives (eg: .war, .ear)
 *
 * @author Navin Surtani
 */
public class AS7Report extends CLSReport
{
   /** NAME **/
   private static final String NAME = "JBoss AS7";

   /** DIRECTORY */
   private static final String DIRECTORY = "jboss-as7";

   /** Constructor */
   public AS7Report()
   {
      super(NAME, ReportSeverity.INFO, NAME, DIRECTORY);
   }

   /**
    * Write the main html content.
    * @param bw the writer to use
    * @throws IOException - if there is an issue with the html writing
    */
   @Override
   public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      bw.write("<table>" + Dump.newLine());
      bw.write("  <tr>" + Dump.newLine());
      bw.write("    <th>Archive</th>" + Dump.newLine());
      bw.write("    <th>JBoss Deployment</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      boolean odd = true;
      for (Archive archive : archives)
      {
         Set<String> provides = getProvides(archive);
         Set<String> requires = getRequires(archive);
         requires.removeAll(provides);
         String archiveName = archive.getName();
         File deploymentXml = buildDeploymentXml(requires, archiveName);
         String path = "./" + archiveName + "/" + deploymentXml.getName();

         if (odd)
         {
            bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
         }
         else
         {
            bw.write("  <tr class=\"roweven\">" + Dump.newLine());
         }
         bw.write("    <td>" + hrefToArchiveReport(archive) + "</td>" + Dump.newLine());
         bw.write("    <td><a href=\"" + path + "\">jboss-deployment-structure.xml</a></td>"
                  + Dump.newLine());
         bw.write("  </tr>" + Dump.newLine());

         odd = !odd;
      }
      bw.write("</table>" + Dump.newLine());
   }

   /**
    * Method getProvides.
    * @param a Archive
    * @return Set<String>
    */
   private Set<String> getProvides(Archive a)
   {
      final Set<String> provides = new HashSet<String>();
      if (a instanceof NestableArchive)
      {
         final NestableArchive na = (NestableArchive) a;
         provides.addAll(na.getProvides().keySet());

         for (Archive sa : na.getSubArchives())
         {
            provides.addAll(getProvides(sa));
         }
      }
      else
      {
         provides.addAll(a.getProvides().keySet());
      }
      return provides;
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
    * Method buildDeploymentXml.
    * @param requires Set<String>
    * @param archiveName String
    * @return File
    * @throws IOException
    */
   private File buildDeploymentXml(Set<String> requires, String archiveName) throws IOException
   {
      final File deployedDir = new File(getOutputDirectory(), archiveName);
      deployedDir.mkdirs();
      final File outputXml = new File(deployedDir.getAbsolutePath() + File.separator
              + "jboss-deployment-structure.xml");
      final FileWriter fw = new FileWriter(outputXml);
      final BufferedWriter bw = new BufferedWriter(fw, 8192);

      bw.write("<?xml version=\"1.0\"?>" + Dump.newLine());
      bw.write("<jboss-deployment-structure>" + Dump.newLine());
      bw.write("  <deployment>" + Dump.newLine());
      bw.write("    <dependencies>" + Dump.newLine());

      final ExtendedProfile as7Profile = new JBossAS7Profile();
      final SortedSet<String> moduleIdentifiers = new TreeSet<String>();

      for (String requiredClass : requires)
      {
         String moduleIdentifier = as7Profile.getModuleIdentifier(requiredClass);
         if (null != moduleIdentifier)
         {
            moduleIdentifiers.add(moduleIdentifier);
         }
         else
         {
            for (Profile p : getKnown())
            {
               if (p.doesProvide(requiredClass))
               {
                  moduleIdentifier = p.getModuleIdentifier();
                  if (null != moduleIdentifier)
                  {
                     moduleIdentifiers.add(moduleIdentifier);
                  }
               }
            }
         }
      }

      for (String identifier : moduleIdentifiers)
      {
         bw.write("      <module name=\"" + identifier + "\"/>" + Dump.newLine());
      }
      bw.write("    </dependencies>" + Dump.newLine());
      bw.write("  </deployment>" + Dump.newLine());
      bw.write("</jboss-deployment-structure>" + Dump.newLine());
      bw.flush();
      bw.close();

      return outputXml;
   }
}
