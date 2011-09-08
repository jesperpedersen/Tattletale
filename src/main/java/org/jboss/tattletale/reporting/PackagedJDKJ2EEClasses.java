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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.ArchiveTypes;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.reporting.profiles.CommonProfile;
import org.jboss.tattletale.reporting.profiles.JavaEE5;
import org.jboss.tattletale.reporting.profiles.SunJava6;

/**
 * @author bmaxwell
 */
public class PackagedJDKJ2EEClasses extends AbstractReport
{
   /** NAME */
   private static final String NAME = "Packaged JDK / J2EE Classes";

   /** DIRECTORY */
   private static final String DIRECTORY = "packaged-jdk-j2ee-classes";

   /* This is a set which will create a summary of the problematic archives as the report analyzes the archives */
   private Set<Archive> summarySet = new TreeSet<Archive>();

   private Set<ProblematicArchive> problemSet = new TreeSet<ProblematicArchive>();

   /**
    * Default Constructor
    */
   public PackagedJDKJ2EEClasses()
   {
      super(DIRECTORY, ReportSeverity.WARNING, NAME, DIRECTORY);
   }         
   
   /**
    * This writes out the locations that the archive containing jdk/j2ee classes is located at
    * @param bw - BufferedWriter
    * @param locations - a set of the locations to write to the report
    * @throws IOException
    */
   private void writeLocations(BufferedWriter bw, SortedSet<Location> locations) throws IOException
   {
      bw.write("<h5>Location: ");
      for (Location l : locations)
      {
         bw.write(l.getFilename() + " ");
      }
      bw.write("</h5>" + Dump.newLine());
   }

   /**
    * This writes out the archive name in the report
    * @param bw - BufferedWriter
    * @param archive - archive
    * @throws IOException
    */
   private void writeArchiveName(BufferedWriter bw, Archive archive) throws IOException
   {
      bw.write("<h2>Archive: " + archive.getName() + " contains these classes which should not be packaged:</h2>"
            + Dump.newLine());
      writeLocations(bw, archive.getLocations());
   }

   @Override
   protected void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      // analyze the archive, so that writeSummary / writeDetailed can be in any order.
      analyze();
      writeSummary(bw);
      writeDetailed(bw);
   }

   private void writeDetailed(BufferedWriter bw) throws IOException
   {
      bw.write("<h1>Detailed analysis of problematic archives:</h1>");
      
      String[] profileProblemLevel = new String[]
      {"PROBLEM", "PROBLEM"};
      CommonProfile[] profiles = new CommonProfile[]
      {new SunJava6(), new JavaEE5()};

      boolean archiveNameWritten;

      for (ProblematicArchive problemmaticArchive : problemSet)
      {
         archiveNameWritten = false;
         Archive archive = problemmaticArchive.archive;
         Set<String> classes = archive.getProvides().keySet();
         int i = -1;
         for (CommonProfile profile : profiles)
         {
            i++;
            boolean profileNameWritten = false;

            for (String clz : classes)
            {
               if (profile.doesProvide(clz))
               {
                  // log the archive name once
                  if (!archiveNameWritten)
                  {
                     writeArchiveName(bw, archive);
                     archiveNameWritten = true;
                  }
                  if (!profileNameWritten)
                  {
                     bw.write("<h3>" + profileProblemLevel[i] + " - '" + profile.getName()
                           + "' already contains these classes:</h3>" + Dump.newLine());
                     bw.write("<ul>" + Dump.newLine());
                     profileNameWritten = true;
                  }

                  // log the class that is included by the jdk or
                  // container
                  bw.write("<li>" + clz + "</li>" + Dump.newLine());
               }
            }
            // close the profile block
            if (profileNameWritten)
               bw.write("</ul>" + Dump.newLine());
         }
      }
   }

   /**
    * This is a class to link archive/profiles for printing later
    *
    */
   class ProblematicArchive implements Comparable<ProblematicArchive>
   {
      public CommonProfile profile;

      public Archive archive;

      public ProblematicArchive(Archive archive, CommonProfile profile)
      {
         this.archive = archive;
         this.profile = profile;
      }

      @Override
      public int compareTo(ProblematicArchive other)
      {
         int val = this.archive.compareTo(other.archive);
         
         if(val != 0)
            return val;
         
         return this.profile.getProfileCode().compareTo(other.profile.getProfileCode());        
      }      
   }

   /**
    * analyze the archives and create a summarySet and problemsSet so we can print out the report
    */
   private void analyze()
   {
      SortedSet<String> envProvidedClassSet = new TreeSet<String>();
      envProvidedClassSet.addAll(new SunJava6().getClassSet());
      envProvidedClassSet.addAll(new JavaEE5().getClassSet());
      
      CommonProfile[] profiles = new CommonProfile[]
      {new SunJava6(), new JavaEE5()};

      for (Archive archive : archives)
      {
         if (archive.getType() == ArchiveTypes.JAR)
         {
            Set<String> classes = archive.getProvides().keySet();
            // loop through profiles, create a section for each profile
            
            for (CommonProfile profile : profiles)
            {               
               for (String clz : classes)
               {
                  if (profile.doesProvide(clz))
                  {
                     problemSet.add(new ProblematicArchive(archive, profile));

                     // track archives that contain classes they shouldn't in summary
                     summarySet.add(archive);

                     // break out and check the next profile, we will get the classes in the writing
                     break;
                  }
               }
            }
         }
      }
   }

   /**
    * This was the original method used to write the report, I've rewritten into analyze and writeSummary and writeDetailed which
    *   can be printed to the report in any order
    * @param bw
    * @throws IOException
    */
   private void writeDetail(BufferedWriter bw) throws IOException
   {
      SortedSet<String> envProvidedClassSet = new TreeSet<String>();
      envProvidedClassSet.addAll(new SunJava6().getClassSet());
      envProvidedClassSet.addAll(new JavaEE5().getClassSet());

      String[] profileProblemLevel = new String[]
      {"PROBLEM", "PROBLEM"};
      CommonProfile[] profiles = new CommonProfile[]
      {new SunJava6(), new JavaEE5()};

      boolean archiveNameWritten;
      for (Archive archive : archives)
      {
         archiveNameWritten = false;

         if (archive.getType() == ArchiveTypes.JAR)
         {
            Set<String> classes = archive.getProvides().keySet();
            // loop through profiles, create a section for each profile
            boolean profileNameWritten;
            int i = -1;
            for (CommonProfile profile : profiles)
            {
               i++;
               profileNameWritten = false;
               for (String clz : classes)
               {
                  if (profile.doesProvide(clz))
                  {
                     // track archives that contain classes they shouldn't in summary
                     summarySet.add(archive);

                     // log the archive name once
                     if (!archiveNameWritten)
                     {
                        writeArchiveName(bw, archive);
                        archiveNameWritten = true;
                     }
                     if (!profileNameWritten)
                     {
                        bw.write("<h2>" + profileProblemLevel[i] + " - '" + profile.getName()
                              + "' already contains these classes:</h2>" + Dump.newLine());
                        bw.write("<ul>" + Dump.newLine());
                        profileNameWritten = true;
                     }

                     // log the class that is included by the jdk or
                     // container
                     bw.write("<li>" + clz + "</li>" + Dump.newLine());
                  }
               }
               // close the profile block
               if (profileNameWritten)
                  bw.write("</ul>" + Dump.newLine());
            }
         }
      }
   }

   /**
    * This writes out a summary of the packaged archives which could cause problems
    * @param bw - BufferedWriter
    * @throws IOException
    */
   private void writeSummary(BufferedWriter bw) throws IOException
   {
      bw.write("<h1>Summary of problematic archives:</h1>");
      bw.write("<ul>");
      for (Archive a : summarySet)
      {
         for (Location l : a.getLocations())
         {
            bw.write("<li>" + l.getFilename() + "</li>");
         }
      }
      bw.write("</ul>");
   }

   @Override
   protected void writeHtmlBodyHeader(BufferedWriter bw) throws IOException
   {
      bw.write("<body>" + Dump.newLine());
      bw.write(Dump.newLine());
      bw.write("<h1>" + NAME + "</h1>" + Dump.newLine());
      bw.write("<h3>PROBLEM - indicates these classes will most likely cause ClassCastExceptions and should be removed</h3>"
            + Dump.newLine());           
      bw.write("<a href=\"../index.html\">Main</a>" + Dump.newLine() + "<br/>");      
      bw.write("<p>" + Dump.newLine());
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
