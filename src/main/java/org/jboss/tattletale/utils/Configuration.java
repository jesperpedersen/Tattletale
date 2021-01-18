/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.tattletale.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper class for handling configuration
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Configuration
{
   /** Configuration */
   private Properties config;

   /**
    * Constructor
    * @param config Properties to encapsulate
    */
   public Configuration(Properties config)
   {
      this.config = config;
   }

   /**
    * Retrieve the configuration properties
    * @return encapsulated Properties
    */
   public Properties getConfiguration()
   {
      return config;
   }

   /**
    * Load configuration from a file
    * @param fileName The file name
    */
   public void loadFromFile(String fileName)
   {
      final Properties properties = new Properties();

      FileInputStream fis = null;
      try
      {
         fis = new FileInputStream(fileName);
         properties.load(fis);
      }
      catch (IOException e)
      {
         System.err.println("Unable to open " + fileName);
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

      addConfig(properties);
   }

   /**
    * Load configuration values specified from either a system property,
    * a file or the classloader
    * @param key The configuration key
    */
   public void load(String key)
   {
      final Properties properties = new Properties();
      final String propertiesFile = System.getProperty(key);
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
         catch (IOException e)
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
                  //No op
               }
            }
         }
      }
      if (!loaded)
      {
         FileInputStream fis = null;
         try
         {
            fis = new FileInputStream(key);
            properties.load(fis);
            loaded = true;
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
      if (!loaded)
      {
         InputStream is = null;
         try
         {
            final ClassLoader cl = Configuration.class.getClassLoader();
            is = cl.getResourceAsStream(key);
            properties.load(is);
         }
         catch (IOException ioe)
         {
            // Properties file not found
         }
         finally
         {
            if (null != is)
            {
               try
               {
                  is.close();
               }
               catch (IOException ioe)
               {
                  // Nothing to do
               }
            }
         }
      }
      addConfig(properties);
   }

   /**
    * Method addConfig.
    * @param cfg Properties
    */
   private void addConfig(Properties cfg)
   {
      if (null == config)
      {
         config = cfg;
         return;
      }

      for (String name : cfg.stringPropertyNames())
      {
         if (!config.containsKey(name))
         {
            config.setProperty(name, cfg.getProperty(name));
         }
      }
   }
}
