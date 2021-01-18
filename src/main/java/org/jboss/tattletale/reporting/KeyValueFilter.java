/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Represents a key/value filter
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class KeyValueFilter implements Filter
{
   /** KeyValue Filters */
   private final Map<String, SortedSet<String>> keyValueFilters;

   /** Constructor */
   public KeyValueFilter()
   {
      keyValueFilters = new HashMap<String, SortedSet<String>>();
   }

   /**
    * Is filtered
    * @return True if filtered; otherwise false
    * @see org.jboss.tattletale.reporting.Filter#isFiltered()
    */
   public boolean isFiltered()
   {
      throw new UnsupportedOperationException("isFiltered() not supported");
   }

   /**
    * Is filtered
    * @param archive The archive
    * @return True if filtered; otherwise false
    * @see org.jboss.tattletale.reporting.Filter#isFiltered(String)
    */
   public boolean isFiltered(String archive)
   {
      throw new UnsupportedOperationException("isFiltered(String) not supported");
   }

   /**
    * Is filtered
    * @param archive The archive
    * @param query   The query
    * @return True if filtered; otherwise false
    * @see org.jboss.tattletale.reporting.Filter#isFiltered(String, String)
    */
   public boolean isFiltered(String archive, String query)
   {
      final SortedSet<String> ss = keyValueFilters.get(archive);

      if (null != ss)
      {
         if (query.endsWith(".class"))
         {
            query = query.substring(0, query.indexOf(".class"));
         }

         if (query.endsWith(".jar"))
         {
            query = query.substring(0, query.indexOf(".jar"));
         }

         if (query.endsWith(".*"))
         {
            query = query.substring(0, query.indexOf(".*"));
         }

         query = query.replace('.', '/');

         for (String v : ss)
         {
            if (query.startsWith(v))
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Init the filter
    * @param filter The filter value
    * @see org.jboss.tattletale.reporting.Filter#init(String)
    */
   public void init(String filter)
   {
      if (null != filter)
      {
         for (String token : filter.split(";"))
         {
            int equal = token.indexOf('=');

            String key = token.substring(0, equal);
            String values = token.substring(equal + 1);

            SortedSet<String> vs = new TreeSet<String>(new SizeComparator());

            for (String value : values.split(","))
            {
               boolean includeAll = false;

               if (value.endsWith(".class"))
               {
                  value = value.substring(0, value.indexOf(".class"));
               }

               if (value.endsWith(".jar"))
               {
                  value = value.substring(0, value.indexOf(".jar"));
               }

               if (value.endsWith(".*"))
               {
                  value = value.substring(0, value.indexOf(".*"));
                  includeAll = true;
               }

               value = value.replace('.', '/');

               if (includeAll)
               {
                  value += '/';
               }

               vs.add(value);
            }

            keyValueFilters.put(key, vs);
         }
      }
   }
}
