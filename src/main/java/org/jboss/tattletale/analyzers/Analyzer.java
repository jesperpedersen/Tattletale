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

package org.jboss.tattletale.analyzers;

import java.io.File;

/**
 * Class that returns an appropriate implementation of {@link ArchiveScanner}
 *
 * @author Navin Surtani
 */
public class Analyzer
{
   /** Field placeholder */
   private boolean placeholder = false;

   /**
    * Returns the appropriate scanner implementation based on the extension of file that is passed as a parameter.
    * @param file - the .jar, .war file etc.
    * @return the implementation of {@link ArchiveScanner}
    */
   public ArchiveScanner getScanner(File file)
   {
      placeholder = true;
      return this.getScanner(file, ".*", null);
   }

   /**
    * Returns the appropriate scanner implementation based on the extension of file that is passed as a parameter.
    * @param file - the .jar, .war file etc.
    * @param extractPattern - extract only matching entries
    * @return the implementation of {@link ArchiveScanner}
    */
   public ArchiveScanner getScanner(File file, String extractPattern)
   {
      placeholder = true;
      return this.getScanner(file, extractPattern, null);
   }

   /**
    * Returns the appropriate scanner implementation based on the extension of file that is passed as a parameter.
    * @param file - the .jar, .war file etc.
    * @param extractPattern - extract only matching entries
    * @param bundlePattern - bundle classes with matching name into separate ClassesArchives
    * @return the implementation of {@link ArchiveScanner}
    */
   public ArchiveScanner getScanner(File file, String extractPattern, String bundlePattern)
   {
      final String fileName = file.getName();

      if (fileName.endsWith(".jar"))
      {
         return new JarScanner();
      }
      else if (fileName.endsWith(".war") || fileName.endsWith(".rar"))
      {
         return (placeholder) ? new WarScanner(extractPattern) : new WarScanner(extractPattern, bundlePattern);
      }
      else if (fileName.endsWith(".ear"))
      {
         return (placeholder) ? new EarScanner(extractPattern) : new EarScanner(extractPattern, bundlePattern);
      }

      return null;
   }
}
