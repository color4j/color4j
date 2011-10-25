/*
 * Copyright (c) 2011 Niclas Hedhman.
 *
 * Licensed  under the  Apache License, Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.color4j.spectro.drivers.manual;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Description of the Class
 */
public class ReflectionReader
{
    private TreeMap m_Readings;

    /**
     * Constructor for the ReflectionReader object
     */
    public ReflectionReader()
    {
        m_Readings = new TreeMap();
    }

    /**
     * Description of the Method
     *
     * @param location Description of the Parameter
     *
     * @return Description of the Return Value
     *
     * @throws IOException           Description of the Exception
     * @throws MalformedURLException Description of the Exception
     */
    public SortedMap importReadings( String location )
        throws IOException, MalformedURLException
    {
        if( location == null || location.trim().equals( "" ) )
        {
            throw new IOException( "Location cannot be empty." );
        }

        m_Readings.clear();

        URL url = new URL( location );
        BufferedReader br = new BufferedReader( new InputStreamReader( url.openStream() ) );
        String str = null;
        while( ( str = br.readLine() ) != null )
        {
            StringTokenizer st = new StringTokenizer( str, "," );
            while( st.hasMoreTokens() )
            {
                String nm = st.nextToken();
                String value = st.nextToken();

                m_Readings.put( nm, value );
            }
        }
        return m_Readings;
    }

    /**
     * Gets the readings attribute of the ReflectionReader object
     *
     * @return The readings value
     */
    public SortedMap getReadings()
    {
        return m_Readings;
    }
}


