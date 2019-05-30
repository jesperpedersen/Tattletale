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
package org.jboss.tattletale;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jboss.tattletale.analyzers.Analyzer;
import org.jboss.tattletale.analyzers.ArchiveScanner;
import org.jboss.tattletale.analyzers.DirectoryScanner;
import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.ArchiveType;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.core.NestableArchive;
import org.jboss.tattletale.profiles.AbstractProfile;
import org.jboss.tattletale.profiles.CDI10;
import org.jboss.tattletale.profiles.JBossAS7Profile;
import org.jboss.tattletale.profiles.JavaEE5;
import org.jboss.tattletale.profiles.JavaEE6;
import org.jboss.tattletale.profiles.Profile;
import org.jboss.tattletale.profiles.Seam22;
import org.jboss.tattletale.profiles.Spring25;
import org.jboss.tattletale.profiles.Spring30;
import org.jboss.tattletale.profiles.SunJava5;
import org.jboss.tattletale.profiles.SunJava6;
import org.jboss.tattletale.reporting.AS7Report;
import org.jboss.tattletale.reporting.AbstractReport;
import org.jboss.tattletale.reporting.BlackListedReport;
import org.jboss.tattletale.reporting.CircularDependencyReport;
import org.jboss.tattletale.reporting.ClassDependantsReport;
import org.jboss.tattletale.reporting.ClassDependsOnReport;
import org.jboss.tattletale.reporting.ClassLocationReport;
import org.jboss.tattletale.reporting.ClassMultipleJarsReport;
import org.jboss.tattletale.reporting.DependantsReport;
import org.jboss.tattletale.reporting.DependsOnReport;
import org.jboss.tattletale.reporting.Dump;
import org.jboss.tattletale.reporting.EarReport;
import org.jboss.tattletale.reporting.GraphvizReport;
import org.jboss.tattletale.reporting.InvalidVersionReport;
import org.jboss.tattletale.reporting.JarReport;
import org.jboss.tattletale.reporting.MultipleLocationsReport;
import org.jboss.tattletale.reporting.MultipleVersionsReport;
import org.jboss.tattletale.reporting.NoVersionReport;
import org.jboss.tattletale.reporting.OSGiReport;
import org.jboss.tattletale.reporting.PackageDependantsReport;
import org.jboss.tattletale.reporting.PackageDependsOnReport;
import org.jboss.tattletale.reporting.PackageMultipleJarsReport;
import org.jboss.tattletale.reporting.Report;
import org.jboss.tattletale.reporting.SealedReport;
import org.jboss.tattletale.reporting.SignedReport;
import org.jboss.tattletale.reporting.TransitiveDependantsReport;
import org.jboss.tattletale.reporting.TransitiveDependsOnReport;
import org.jboss.tattletale.reporting.UnusedReport;
import org.jboss.tattletale.reporting.WarReport;
import org.jboss.tattletale.utils.Configuration;

/**
 * Main
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jbalunas@jboss.org">Jay Balunas</a>
 * @author <a href="mailto:mike.moore@amentra.com">Mike Moore</a>
 * @author Navin Surtani
 */
public class Main
{
   /** Default classloader structure */
   private static final String DEFAULT_TT_CLS = "org.jboss.tattletale.reporting.classloader.NoopClassLoaderStructure";

   /** Default list of archive name extensions to scan for */
   private static final String DEFAULT_TT_SCAN = ".jar, .war, .ear";

   /** Default list of matching files to extract from nested (war or ear) archives */
   private static final String DEFAULT_TT_EXTRACT = ".*\\.(j|w|r)ar$";

   /** Source */
   private String source;

   /** Destination */
   private String destination;

   /** Configuration file */
   private String configurationFile;

   /** Filter */
   private String filter;

   /** Class loader structure */
   private String classloaderStructure;

   /** Profiles */
   private String profiles;

   /** Excludes */
   private String excludes;

   /** Blacklisted */
   private String blacklisted;

   /** Fail on info */
   private boolean failOnInfo;

   /** Fail on warning */
   private boolean failOnWarn;

   /** Fail on error */
   private boolean failOnError;

   /** Delete output directory */
   private boolean deleteOutputDirectory;

   /** Reports */
   private String reports;

   /** Scan */
   private String scan;

   /** Title */
   private String title;

   /** Extract pattern */
   private String extractPattern;

   /** Bundle depth */
   private String bundlePattern;

   /** Configuration **/
   private Properties configuration;

   /** A List of the Constructors used to create dependency reports */
   private final List<Class<? extends AbstractReport>> dependencyReports;

   /** A List of the Constructors used to create general reports */
   private final List<Class<? extends AbstractReport>> generalReports;

   /** A List of the Constructors used to create custom reports */
   private final List<Class<? extends Report>> customReports;

   private static final Map<String, String> reportMap = new HashMap<String, String>();
   static
   {
      reportMap.put("eliminatejars", "multipleversions");
      reportMap.put("unusedjar", "unused");
      reportMap.put("multiplejars", "multiplejarsclass");
      reportMap.put("sign", "signed");
   }

   /**
    * Constructor
    */
   public Main()
   {
      source = ".";
      destination = ".";
      configurationFile = null;
      filter = null;
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
      title = "";
      extractPattern = null;
      bundlePattern = null;
      configuration = null;

      dependencyReports = new ArrayList<Class<? extends AbstractReport>>();
      addDependencyReport(ClassDependsOnReport.class);
      addDependencyReport(ClassDependantsReport.class);
      addDependencyReport(DependsOnReport.class);
      addDependencyReport(DependantsReport.class);
      addDependencyReport(PackageDependantsReport.class);
      addDependencyReport(PackageDependsOnReport.class);
      addDependencyReport(TransitiveDependsOnReport.class);
      addDependencyReport(TransitiveDependantsReport.class);
      addDependencyReport(CircularDependencyReport.class);
      addDependencyReport(GraphvizReport.class);

      generalReports = new ArrayList<Class<? extends AbstractReport>>();
      addGeneralReport(AS7Report.class);
      addGeneralReport(ClassMultipleJarsReport.class);
      addGeneralReport(MultipleLocationsReport.class);
      addGeneralReport(PackageMultipleJarsReport.class);
      addGeneralReport(MultipleVersionsReport.class);
      addGeneralReport(NoVersionReport.class);
      addGeneralReport(ClassLocationReport.class);
      addGeneralReport(OSGiReport.class);
      addGeneralReport(SignedReport.class);
      addGeneralReport(SealedReport.class);
      addGeneralReport(InvalidVersionReport.class);
      addGeneralReport(BlackListedReport.class);
      addGeneralReport(UnusedReport.class);

      customReports = new ArrayList<Class<? extends Report>>();
   }

   /**
    * Set source
    * @param source The value
    */
   public void setSource(String source)
   {
      this.source = source;
   }

   /**
    * Set destination
    * @param destination The value
    */
   public void setDestination(String destination)
   {
      this.destination = destination;
   }

   /**
    * Set configuration
    * @param configurationFile The value
    */
   public void setConfigurationFile(String configurationFile)
   {
      this.configurationFile = configurationFile;
   }

   /**
    * Set filter
    * @param filter The value
    */
   public void setFilter(String filter)
   {
      this.filter = filter;
   }

   /**
    * Set class loader structure
    * @param classloaderStructure The value
    */
   public void setClassLoaderStructure(String classloaderStructure)
   {
      this.classloaderStructure = classloaderStructure;
   }

   /**
    * Set profiles
    * @param profiles The value
    */
   public void setProfiles(String profiles)
   {
      this.profiles = profiles;
   }

   /**
    * Set excludes
    * @param excludes The value
    */
   public void setExcludes(String excludes)
   {
      this.excludes = excludes;
   }

   /**
    * Add a dependency report to the list of those to be generated
    * @param clazz The class definition of the dependency report
    */
   public final void addDependencyReport(Class<? extends AbstractReport> clazz)
   {
      dependencyReports.add(clazz);
   }

   /**
    * Add a report to the list of those to be generated
    * @param clazz The class definition of the report
    */
   public final void addGeneralReport(Class<? extends AbstractReport> clazz)
   {
      generalReports.add(clazz);
   }

   /**
    * Add a report to the list of those to be generated
    * @param clazz The class definition of the custom report
    */
   public final void addCustomReport(Class<? extends Report> clazz)
   {
      customReports.add(clazz);
   }

   /**
    * Set blacklisted
    * @param blacklisted The value
    */
   public void setBlacklisted(String blacklisted)
   {
      this.blacklisted = blacklisted;
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
    * Set fail on warn
    * @param failOnWarn The value
    */
   public void setFailOnWarn(boolean failOnWarn)
   {
      this.failOnWarn = failOnWarn;
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
    * Set delete output directory
    * @param deleteOutputDirectory The value
    */
   public void setDeleteOutputDirectory(boolean deleteOutputDirectory)
   {
      this.deleteOutputDirectory = deleteOutputDirectory;
   }

   /**
    * Set the reports
    * @param reports The value
    */
   public void setReports(String reports)
   {
      this.reports = reports;
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
    * Set the title
    * @param title The value
    */
   public void setTitle(String title)
   {
      this.title = title;
   }

   /**
    * Set the extract pattern
    * @param extractPattern The value
    */
   public void setExtractPattern(String extractPattern)
   {
      this.extractPattern = extractPattern;
   }

   /**
    * Set the bundle pattern
    * @param bundlePattern The value
    */
   public void setBundlePattern(String bundlePattern)
   {
      this.bundlePattern = bundlePattern;
   }

   /**
    * Set the configuration
    * @param configuration The configuration
    */
   public void setConfiguration(Properties configuration)
   {
      this.configuration = configuration;
   }

   /**
    * Execute
    * @throws Exception Thrown if an error occurs
    */
   public void execute() throws Exception
   {
      execute(false);
   }

   /**
    * Execute
    * @param analyzeComponents analyze subarchives if true
    * @throws Exception Thrown if an error occurs
    */
   public void execute(boolean analyzeComponents) throws Exception
   {
      final Configuration cfg = new Configuration(configuration);

      if (null != configurationFile)
      {
         cfg.loadFromFile(configurationFile);
      }
      else
      {
         cfg.load("jboss-tattletale.properties");
      }

      configuration = cfg.getConfiguration();

      Properties filters = null;

      if (null != filter)
      {
         filters = loadFilters();
      }
      else
      {
         filters = loadDefaultFilters();
      }

      if (null == classloaderStructure)
      {
         classloaderStructure = configuration.getProperty("classloader");
      }

      if (null == classloaderStructure || classloaderStructure.trim().isEmpty())
      {
         classloaderStructure = DEFAULT_TT_CLS;
      }

      boolean allProfiles = false;
      Set<String> profileSet = null;

      if (null == profiles)
      {
         profiles = configuration.getProperty("profiles");
      }

      if (null != profiles)
      {
         profileSet = new HashSet<String>();

         for (String token : profiles.split("[\\s,]+"))
         {
            if ("*".equals(token))
            {
               allProfiles = true;
            }
            else
            {
               allProfiles = false;
               profileSet.add(token);
            }
         }
      }

      Set<String> blacklistedSet = null;

      if (null == blacklisted)
      {
         blacklisted = configuration.getProperty("blacklisted");
      }

      if (null != blacklisted)
      {
         blacklistedSet = new HashSet<String>();

         for (String token : blacklisted.split("[\\s,]+"))
         {
            if (token.endsWith(".*"))
            {
               token = token.substring(0, token.indexOf(".*"));
            }

            if (token.endsWith(".class"))
            {
               token = token.substring(0, token.indexOf(".class"));
            }

            blacklistedSet.add(token);
         }
      }

      Set<String> excludeSet = null;

      if (null == excludes)
      {
         excludes = configuration.getProperty("excludes");
      }

      if (null != excludes)
      {
         excludeSet = new HashSet<String>(parseExcludes(excludes));
      }

      boolean allReports = false;
      Set<String> reportSet = null;

      if (null == reports)
      {
         reports = configuration.getProperty("reports");
      }

      if (null == reports || reports.trim().equals("*"))
      {
         allReports = true;
      }

      if (!allReports && null != reports)
      {
         reportSet = new HashSet<String>();

         for (String token : reports.split("[\\s,]+"))
         {
            if (reportMap.containsKey(token))
            {
               System.err.println("Please adjust the configuration: " + token + " is now called "
                   + reportMap.get(token));
               token = reportMap.get(token);
            }
            reportSet.add(token);
         }
      }

      if (!allReports && null == reportSet)
      {
         allReports = true;
      }

      if (null == scan)
      {
         scan = configuration.getProperty("scan");
      }

      if (null == scan || scan.trim().isEmpty())
      {
         scan = DEFAULT_TT_SCAN;
      }

      if (null == extractPattern)
      {
         extractPattern = configuration.getProperty("extractPattern");
      }

      if (null == extractPattern || extractPattern.trim().isEmpty())
      {
         extractPattern = DEFAULT_TT_EXTRACT;
      }

      final String ac = configuration.getProperty("analyzeComponents");
      if (null != ac && ac.trim().equals("true"))
      {
         analyzeComponents = true;
      }

      if (null == bundlePattern && analyzeComponents)
      {
         bundlePattern = configuration.getProperty("bundlePattern");
      }

      if (null != bundlePattern && bundlePattern.trim().isEmpty())
      {
         bundlePattern = null;
      }

      if (null != bundlePattern && !analyzeComponents)
      {
         System.err.println("Ignoring bundlePattern: " + bundlePattern);
         bundlePattern = null;
      }

      DirectoryScanner.setArchives(scan);

      final Map<String, SortedSet<Location>> locationsMap = new HashMap<String, SortedSet<Location>>();
      final SortedSet<Archive> archives = new TreeSet<Archive>();
      final SortedMap<String, SortedSet<String>> gProvides = new TreeMap<String, SortedSet<String>>();

      // Load up selected profiles
      final List<Profile> known = new ArrayList<Profile>();

      final AbstractProfile[] profiles = new AbstractProfile[]{new SunJava5(), new SunJava6(),
                                                               new JavaEE5(), new JavaEE6(),
                                                               new CDI10(), new Seam22(),
                                                               new Spring25(), new Spring30(),
                                                               new JBossAS7Profile()};

      for (AbstractProfile p : profiles)
      {
         if (p.included(allProfiles, profileSet))
         {
            known.add(p);
         }
      }

      final List<File> fileList = new ArrayList<File>();
      final Analyzer analyzer = new Analyzer();

      for (String name : source.split("#"))
      {
         File file = new File(name);
         if (file.isDirectory())
         {
            fileList.addAll(DirectoryScanner.scan(file, excludeSet));
         }
         else
         {
            fileList.add(file);
         }
      }

      for (File file : fileList)
      {
         ArchiveScanner scanner = (analyzeComponents) ? analyzer.getScanner(file, extractPattern, bundlePattern) :
             analyzer.getScanner(file, extractPattern);

         if (null != scanner)
         {
            Archive archive = scanner.scan(file, gProvides, known, blacklistedSet);
            if (null != archive)
            {
               List<Archive> archs = new ArrayList<Archive>();
               if (analyzeComponents)
               {
                  addArchives(archs, archive);
               }
               else
               {
                  archs.add(archive);
               }
               for (Archive a : archs)
               {
                  SortedSet<Location> locations = locationsMap.get(a.getName());
                  if (null == locations)
                  {
                     locations = new TreeSet<Location>();
                  }
                  locations.addAll(a.getLocations());
                  locationsMap.put(a.getName(), locations);
                  archives.add(a);
               }
            }
         }
      }

      for (Archive a : archives)
      {
         SortedSet<Location> locations = locationsMap.get(a.getName());

         for (Location l : locations)
         {
            a.addLocation(l);
         }
      }

      // Write out report
      if (null != archives && archives.size() > 0)
      {
         final ReportSetBuilder reportSetBuilder = new ReportSetBuilder(destination, allReports, reportSet, filters);

         reportSetBuilder.addReportParameter("setCLS", classloaderStructure);
         reportSetBuilder.addReportParameter("setKnown", known);
         reportSetBuilder.addReportParameter("setArchives", archives);
         reportSetBuilder.addReportParameter("setConfig", configuration);
         reportSetBuilder.addReportParameter("setGlobalProvides", gProvides);

         loadCustomReports(configuration);
         outputReport(reportSetBuilder, archives);
      }
   }

   /**
    * Method for recursively adding subarchives in nested archives to an archive list
    * @param archiveList the archive list
    * @param archive an archive
    */
   private void addArchives(List<Archive> archiveList, Archive archive)
   {
      archive.setParentArchive(null);
      if (archive instanceof NestableArchive)
      {
         final NestableArchive na = (NestableArchive) archive;
         for (Archive a : na.getSubArchives())
         {
            addArchives(archiveList, a);
         }
      }
      else
      {
         archiveList.add(archive);
      }
   }

   /**
    * Method that loads the custom reports based on the configuration in the
    * jboss-tattletale.properties file.
    * @param config - the Properties configuration.
    */
   @SuppressWarnings("unchecked")
   private void loadCustomReports(Properties config)
   {
      final FileInputStream inputStream = null;
      try
      {
         int index = 1;
         String keyString = "customreport." + index;

         while (null != config.getProperty(keyString))
         {
            ClassLoader cl = Main.class.getClassLoader();
            String reportName = config.getProperty(keyString);
            Class<? extends Report> customReportClass =
                (Class<? extends Report>) Class.forName(reportName, true, cl);
            addCustomReport(customReportClass);
            index++;
            keyString = "customreport." + index;
         }
      }
      catch (Exception e)
      {
         System.err.println("Exception of type: " + e.getClass().toString()
                            + " thrown in loadCustomReports() in org.jboss.tattletale.Main");
      }
      finally
      {
         if (null != inputStream)
         {
            try
            {
               inputStream.close();
            }
            catch (IOException ioe)
            {
               // No op.
            }
         }
      }
   }

   /**
    * Load filters
    * @return The filters
    */
   private Properties loadFilters()
   {
      final Properties properties = new Properties();

      FileInputStream fis = null;
      try
      {
         fis = new FileInputStream(filter);
         properties.load(fis);
      }
      catch (IOException ioe)
      {
         System.err.println("Unable to open " + filter);
      }
      finally
      {
         if (null != fis)
         {
            try
            {
               fis.close();
            }
            catch (IOException ioe)
            {
               // Nothing to do
            }
         }
      }

      return properties;
   }

   /**
    * Load default filter values
    * @return The properties
    */
   private Properties loadDefaultFilters()
   {
      final Properties properties = new Properties();
      final String propertiesFile = System.getProperty("jboss-tattletale-filter.properties");
      boolean loaded = false;

      if (null != propertiesFile)
      {
         FileInputStream fis = null;
         try
         {
            fis = new FileInputStream(propertiesFile);
            properties.load(fis);
            loaded = true;
         }
         catch (IOException ioe)
         {
            System.err.println("Unable to open " + propertiesFile);
         }
         finally
         {
            if (null != fis)
            {
               try
               {
                  fis.close();
               }
               catch (IOException ioe)
               {
                  // Nothing to do
               }
            }
         }
      }
      if (!loaded)
      {
         FileInputStream fis = null;
         try
         {
            fis = new FileInputStream("jboss-tattletale-filter.properties");
            properties.load(fis);
         }
         catch (IOException ioe)
         {
            // Nothing to do
         }
         finally
         {
            if (null != fis)
            {
               try
               {
                  fis.close();
               }
               catch (IOException ioe)
               {
                  // Nothing to do
               }
            }
         }
      }

      return properties;
   }

   /**
    * Generate the basic reports to the output directory
    * @param reportSetBuilder Defines the output directory and which reports to build
    * @param archives         The archives
    * @throws Exception In case of fail on settings
    */
   private void outputReport(ReportSetBuilder reportSetBuilder, SortedSet<Archive> archives) throws Exception
   {
      reportSetBuilder.clear();
      for (Class<? extends AbstractReport> reportDef : dependencyReports)
      {
         reportSetBuilder.addReport(reportDef);
      }
      final SortedSet<Report> dependencyReportSet = reportSetBuilder.getReportSet();

      reportSetBuilder.clear();
      for (Class<? extends AbstractReport> reportDef : generalReports)
      {
         reportSetBuilder.addReport(reportDef);
      }
      final SortedSet<Report> generalReportSet = reportSetBuilder.getReportSet();

      reportSetBuilder.clear();
      for (Class<? extends Report> reportDef : customReports)
      {
         reportSetBuilder.addReport(reportDef);
      }
      final SortedSet<Report> customReportSet = reportSetBuilder.getReportSet();

      reportSetBuilder.clear();
      addJarReports(archives, reportSetBuilder);
      final SortedSet<Report> archiveReports = reportSetBuilder.getReportSet();

      final String outputDir = reportSetBuilder.getOutputDir();
      Dump.generateIndex(dependencyReportSet, generalReportSet, archiveReports, customReportSet, outputDir, title);
      Dump.generateCSS(outputDir);

      if (failOnInfo || failOnWarn || failOnError)
      {
         final FailureCheck failureCheck = new FailureCheck();
         failureCheck.processReports(dependencyReportSet);
         failureCheck.processReports(generalReportSet);
         failureCheck.processReports(customReportSet);
         failureCheck.processReports(archiveReports);

         if (null != failureCheck.errorReport())
         {
            throw new Exception(failureCheck.errorReport());
         }
      }
   }

   /**
    * Parse excludes
    * @param s The input string
    * @return The set of excludes
    */
   private Set<String> parseExcludes(String s)
   {
      final Set<String> result = new HashSet<String>();

      for (String token : s.split("[\\s,]+"))
      {
         if (token.startsWith("**"))
         {
            token = token.substring(2);
         }

         if (token.endsWith("**"))
         {
            token = token.substring(0, token.indexOf("**"));
         }

         result.add(token);
      }

      return result;
   }

   /**
    * The usage method
    */
   private static void usage()
   {
      System.out.println("Usage: Tattletale [-exclude=<excludes>] [-title=<title>] [-components[=<regex>]]"
                         + " <source>[#<source>]* [<output-directory>]");
      System.exit(0);
   }

   /**
    * Add the reports based on the archives that we have.
    * @param archives - the collection of Archives.
    * @param reportSetBuilder - the ReportSetBuilder to add a set of Reports to
    * (corresponding to ArchiveType of each archive).
    */

   private void addJarReports(Collection<Archive> archives, ReportSetBuilder reportSetBuilder)
   {
      for (Archive a : archives)
      {
         if (a.getType() == ArchiveType.WAR)
         {
            reportSetBuilder.addReport(new WarReport((NestableArchive) a));
            continue;
         }

         if (a.getType() == ArchiveType.EAR)
         {
            reportSetBuilder.addReport(new EarReport((NestableArchive) a));
            continue;
         }

         reportSetBuilder.addReport(new JarReport(a));
      }
   }

   /**
    * The main method
    * @param args The arguments
    */
   public static void main(String[] args)
   {
      try
      {
         final Main main = new Main();
         String source = "";
         String destination = ".";
         boolean analyzeComponents = false;
         for (String arg : args)
         {
            if (arg.startsWith("-components"))
            {
               analyzeComponents = true;
               int index = arg.indexOf('=');
               if (index > 0)
               {
                  main.setBundlePattern(arg.substring(index + 1));
               }
               continue;
            }
            if (arg.startsWith("-exclude="))
            {
               main.setExcludes(arg.substring(arg.indexOf('=') + 1));
               continue;
            }
            if (arg.startsWith("-title="))
            {
               main.setTitle(arg.substring(arg.indexOf('=') + 1));
               continue;
            }
            if (source.isEmpty())
            {
               source = arg;
               continue;
            }
            destination = arg;
         }
         if (source.isEmpty())
         {
            usage();
         }
         main.setSource(source);
         main.setDestination(destination);
         main.setFailOnInfo(false);
         main.setFailOnWarn(false);
         main.setFailOnError(false);
         main.setDeleteOutputDirectory(true);

         main.execute(analyzeComponents);
      }
      catch (Exception e)
      {
         System.err.println("Exception: " + e.getMessage());
         e.printStackTrace(System.err);
      }
   }

   /**
    * This helper class checks reports to determine whether they should fail,
    * according to the rules set.
    * @author Mike Moore
    */
   private class FailureCheck
   {
      /** Field foundError. */
      private boolean foundError = false;

      /** Field first. */
      private boolean first = true;

      /** Field sb. */
      private final StringBuilder sb = new StringBuilder();

      /**
       * Method errorReport().
       * @return the error report as a String if errors were found, null otherwise
       */
      String errorReport()
      {
         if (foundError)
         {
            return sb.toString();
         }
         return null;
      }

      /**
       * Checks a set of reports for failure conditions
       * @param reports The reports to check
       */
      void processReports(Set<Report> reports)
      {
         for (Report report : reports)
         {
            processReport(report);
         }
      }

      /**
       * Checks a single report for failure conditions
       * @param report The report to check
       */
      void processReport(Report report)
      {
         if ((Report.ReportStatus.YELLOW == report.getStatus() || Report.ReportStatus.RED == report.getStatus())
             && ((Report.ReportSeverity.INFO == report.getSeverity() && failOnInfo) ||
                 (Report.ReportSeverity.WARNING == report.getSeverity() && failOnWarn) ||
                 (Report.ReportSeverity.ERROR == report.getSeverity() && failOnError)))
         {
            appendReportInfo(report);
         }
      }

      /**
       * Record a report failure for the error report
       * @param report A report that meets the failure conditions
       */
      void appendReportInfo(Report report)
      {
         if (!first)
         {
            sb.append(System.getProperty("line.separator"));
         }

         sb.append(report.getId()).append('=').append(report.getStatus().toString());

         foundError = true;
         first = false;
      }
   }

   /**
    * This helper class generates reports from report definitions and gathers
    * report definitions into a SortedSet which can be used to build the index.
    * @author Mike Moore
    */
   private class ReportSetBuilder
   {
      /** Field allReports. */
      private final boolean allReports;

      /** Field outputDir. */
      private final String outputDir;

      /** Field filters. */
      private final Properties filters;

      /** Field reportSet. */
      private final Set<String> reportSet;

      /** Field returnReportSet. */
      private SortedSet<Report> returnReportSet = new TreeSet<Report>();

      /** Field reportParameters. */
      private final Map<String, Object> reportParameters = new HashMap<String, Object>();

      /**
       * Constructor
       *
       * @param destination Where the reports go
       * @param allReports  Should all reports be generated?
       * @param reportSet   The set of reports that should be generated
       * @param filters     The filters
       * @throws IOException
       */
      ReportSetBuilder(String destination, boolean allReports, Set<String> reportSet, Properties filters)
         throws IOException
      {
         outputDir = setupOutputDir(destination);
         this.allReports = allReports;
         this.reportSet = reportSet;
         this.filters = filters;
      }

      /**
       * Add a parameter which will be used to initialize the reports built
       * @param setMethodName The name of the method that will set the parameter on the report
       * @param parameter     The parameter to set
       */
      public void addReportParameter(String setMethodName, Object parameter)
      {
         reportParameters.put(setMethodName, parameter);
      }

      /**
       * Starts a new report set. This allows a single ReportSetBuilder to be
       * used to generate multiple report sets
       */
      void clear()
      {
         // start a new set, the old sets are still in use for indexing
         returnReportSet = new TreeSet<Report>();
      }

      /**
       * Generates the report from the definition, output goes to the output
       * directory.
       * @param report the definition of the report to generate
       */
      void addReport(Report report)
      {
         if (allReports || reportSet.contains(report.getId()))
         {
            if (null != filters && null != filters.getProperty(report.getId()))
            {
               report.setFilter(filters.getProperty(report.getId()));
            }
            report.generate(outputDir);
            returnReportSet.add(report);
         }
      }

      /**
       * Generates the report from the definition, output goes to the output
       * directory.
       * @param reportDef the class definition of the report to generate
       * @throws Exception
       */
      void addReport(Class<? extends Report> reportDef) throws Exception
      {
         // build report from empty constructor
         final Report report = reportDef.getConstructor(new Class[0]).newInstance();

         // populate required report parameters
         for (Method m : reportDef.getMethods())
         {
            if (reportParameters.containsKey(m.getName()))
            {
               m.invoke(report, reportParameters.get(m.getName()));
            }
         }
         addReport(report);
      }

      /**
       * Method getReportSet().
       * @return A Set of reports generated, useful for building an index
       */
      SortedSet<Report> getReportSet()
      {
         return returnReportSet;
      }

      /**
       * Method getOutputDir().
       * @return the String representation of the output directory
       */
      String getOutputDir()
      {
         return outputDir;
      }

      /**
       * Validate and create the outputDir if needed.
       * @param outputDir Where reports go
       * @return The verified output path for the reports
       * @throws IOException If the output directory cannot be created
       */
      private String setupOutputDir(String outputDir) throws IOException
      {
         // Verify ending slash
         outputDir = !outputDir.substring(outputDir.length() - 1).equals(File.separator)
            ? outputDir + File.separator : outputDir;
         // Verify output directory exists & create if it does not
         final File outputDirFile = new File(outputDir);

         if (outputDirFile.exists())
         {
            if (deleteOutputDirectory)
            {
               if (!outputDirFile.equals(new File(".")))
               {
                  recursiveDelete(outputDirFile);
               }
            }
            else
            {
               throw new IOException("Directory: " + outputDir + " exists");
            }
         }

         if (!outputDirFile.equals(new File(".")) && !outputDirFile.mkdirs())
         {
            throw new IOException("Cannot create directory: " + outputDir);
         }

         return outputDir;
      }

      /**
       * Recursive delete
       * @param f The file handler
       * @throws IOException Thrown if a file could not be deleted
       */
      private void recursiveDelete(File f) throws IOException
      {
         if (null != f && f.exists())
         {
            if (f.isDirectory())
            {
               for (File file : f.listFiles())
               {
                  recursiveDelete(file);
               }
            }

            if (!f.delete())
            {
               throw new IOException("Could not delete " + f);
            }
         }
      }
   }
}
