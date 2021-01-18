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
package org.jboss.tattletale.maven;

import org.apache.maven.plugin.MojoFailureException;

import org.jboss.tattletale.Main;

/**
 * Implementation class for Tattletale Report Maven Mojo
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ReportMojo extends TattletaleMojo
{
   /** Class loader structure */
   private String classloaderStructure;

   /** Reports */
   private String[] reports;

   /** Profiles */
   private String[] profiles;

   /** Excludes */
   private String[] excludes;

   /** Blacklisted */
   private String[] blacklisted;

   /** Fail on info */
   private boolean failOnInfo;

   /** Fail on warning */
   private boolean failOnWarn;

   /** Fail on error */
   private boolean failOnError;

   /** Delete output directory */
   private boolean deleteOutputDirectory;

   /** Scan */
   private String scan;

   /** Title */
   private String title;

   /** Extract pattern */
   private String extractPattern;

   /** Bundle pattern */
   private String bundlePattern;

   /** Analyze components */
   private boolean analyzeComponents;

   /** Constructor */
   public ReportMojo()
   {
      classloaderStructure = null;
      profiles = null;
      excludes = null;
      blacklisted = null;
      failOnInfo = false;
      failOnWarn = false;
      failOnError = false;
      deleteOutputDirectory = true;
      reports = null;
      scan = null;
      title = null;
      extractPattern = null;
      bundlePattern = null;
      analyzeComponents = false;
   }

   /**
    * Get the class loader structure
    * @return The value
    */
   public String getClassloader()
   {
      return classloaderStructure;
   }

   /**
    * Set the class loader structure
    * @param classloaderStructure The value
    */
   public void setClassloader(String classloaderStructure)
   {
      this.classloaderStructure = classloaderStructure;
   }

   /**
    * Get the reports
    * @return The value
    */
   public String[] getReports()
   {
      return reports;
   }

   /**
    * Set the reports
    * @param reports The value
    */
   public void setReports(String[] reports)
   {
      this.reports = reports;
   }

   /**
    * Get the profiles
    * @return The value
    */
   public String[] getProfiles()
   {
      return profiles;
   }

   /**
    * Set the profiles
    * @param profiles The value
    */
   public void setProfiles(String[] profiles)
   {
      this.profiles = profiles;
   }

   /**
    * Get the excludes
    * @return The value
    */
   public String[] getExcludes()
   {
      return excludes;
   }

   /**
    * Set the excludes
    * @param excludes The value
    */
   public void setExcludes(String[] excludes)
   {
      this.excludes = excludes;
   }

   /**
    * Get the blacklisted
    * @return The value
    */
   public String[] getBlacklisted()
   {
      return blacklisted;
   }

   /**
    * Set the blacklisted
    * @param blacklisted The value
    */
   public void setBlacklisted(String[] blacklisted)
   {
      this.blacklisted = blacklisted;
   }

   /**
    * Get fail on info
    * @return The value
    */
   public boolean getFailOnInfo()
   {
      return failOnInfo;
   }

   /**
    * Set fail on info
    * @param failOnInfo The value
    */
   public void setFailOnInfo(boolean failOnInfo)
   {
      this.failOnInfo = failOnInfo;
   }

   /**
    * Get fail on warn
    * @return The value
    */
   public boolean getFailOnWarn()
   {
      return failOnWarn;
   }

   /**
    * Set fail on warn
    * @param failOnWarn The value
    */
   public void setFailOnWarn(boolean failOnWarn)
   {
      this.failOnWarn = failOnWarn;
   }

   /**
    * Get fail on error
    * @return The value
    */
   public boolean getFailOnError()
   {
      return failOnError;
   }

   /**
    * Set fail on error
    * @param failOnError The value
    */
   public void setFailOnError(boolean failOnError)
   {
      this.failOnError = failOnError;
   }

   /**
    * Get delete output directory
    * @return The value
    */
   public boolean getDeleteOutputDirectory()
   {
      return deleteOutputDirectory;
   }

   /**
    * Set delete output directory
    * @param deleteOutputDirectory The value
    */
   public void setDeleteOutputDirectory(boolean deleteOutputDirectory)
   {
      this.deleteOutputDirectory = deleteOutputDirectory;
   }

   /**
    * Get the scan
    * @return The value
    */
   public String getScan()
   {
      return scan;
   }

   /**
    * Set the scan
    * @param scan The value
    */
   public void setScan(String scan)
   {
      this.scan = scan;
   }

   /**
    * Get the title
    * @return The value
    */
   public String getTitle()
   {
      return title;
   }

   /**
    * Set the title
    * @param title The value
    */
   public void setTitle(String title)
   {
      this.title = title;
   }

   /**
    * Get the extractPattern
    * @return The value
    */
   public String getExtractPattern()
   {
      return extractPattern;
   }

   /**
    * Set the extractPattern
    * @param extractPattern The value
    */
   public void setExtractPattern(String extractPattern)
   {
      this.extractPattern = extractPattern;
   }

   /**
    * Get the bundlePattern
    * @return The value
    */
   public String getBundlePattern()
   {
      return bundlePattern;
   }

   /**
    * Set the bundlePattern
    * @param bundlePattern The value
    */
   public void setBundlePattern(String bundlePattern)
   {
      this.bundlePattern = bundlePattern;
   }

   /**
    * Get the analyzeComponents
    * @return The value
    */
   public boolean getAnalyzeComponents()
   {
      return analyzeComponents;
   }

   /**
    * Set the analyzeComponents
    * @param analyzeComponents The value
    */
   public void setAnalyzeComponents(boolean analyzeComponents)
   {
      this.analyzeComponents = analyzeComponents;
   }

   /**
    * Execute
    * @throws MojoFailureException   Thrown if there is an error
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   @Override
   public void execute() throws MojoFailureException
   {
      try
      {
         final Main main = new Main();

         main.setSource(getSource().getAbsolutePath());
         main.setDestination(getDestination().getAbsolutePath());

         if (null != getConfiguration())
         {
            main.setConfigurationFile(getConfiguration().getAbsolutePath());
         }

         if (null != getFilter())
         {
            main.setFilter(getFilter().getAbsolutePath());
         }

         main.setClassLoaderStructure(getClassloader());

         if (null != getReports())
         {
            final StringBuilder sb = new StringBuilder();
            for (String report : getReports())
            {
               sb.append(report).append(',');
            }
            sb.setLength(sb.length() - 1);
            main.setReports(sb.toString());
         }

         if (null != getProfiles())
         {
            final StringBuilder sb = new StringBuilder();
            for (String profile : getProfiles())
            {
               sb.append(profile).append(',');
            }
            sb.setLength(sb.length() - 1);
            main.setProfiles(sb.toString());
         }

         if (null != getExcludes())
         {
            final StringBuilder sb = new StringBuilder();
            for (String exclude : getExcludes())
            {
               sb.append(exclude).append(',');
            }
            sb.setLength(sb.length() - 1);
            main.setExcludes(sb.toString());
         }

         if (null != getBlacklisted())
         {
            final StringBuilder sb = new StringBuilder();
            for (String bl : getBlacklisted())
            {
               sb.append(bl).append(',');
            }
            sb.setLength(sb.length() - 1);
            main.setBlacklisted(sb.toString());
         }

         main.setFailOnInfo(getFailOnInfo());
         main.setFailOnWarn(getFailOnWarn());
         main.setFailOnError(getFailOnError());
         main.setDeleteOutputDirectory(getDeleteOutputDirectory());

         main.setScan(getScan());
         main.setTitle(getTitle());
         main.setExtractPattern(getExtractPattern());
         main.setBundlePattern(getBundlePattern());

         getLog().info("Scanning: " + getSource().getAbsolutePath());

         main.execute(analyzeComponents);
      }
      catch (Throwable t)
      {
         throw new MojoFailureException(t.getMessage());
      }
   }
}
