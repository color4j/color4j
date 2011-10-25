/*
 * Copyright (c) 2000-2011 Niclas Hedhman.
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

package org.color4j.imports.mtf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.color4j.colorimetry.ReflectanceImpl;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.imports.AbstractTextFileReflectanceImporter;
import org.color4j.imports.ImportException;

/**
 */
public class ImporterMTF extends AbstractTextFileReflectanceImporter
{
    private int m_StartWavelength = 300;
    private int m_EndWavelength = 700;

    private static final Double ZERO = 0.0;

    public ImporterMTF()
    {
        super( "Minolta Text File", "mtf" );
    }

    public int getStartWavelength()
    {
        return m_StartWavelength;
    }

    public int getEndWavelength()
    {
        return m_EndWavelength;
    }

    /**
     * The start of the wavelength corresponds to the "nm" column.
     * E.g, 300, 400, 500...
     *
     * @param start The starting wavelength.
     */
    public void setStartWavelength( int start )
    {
        m_StartWavelength = start;
    }

    /**
     * The end of the wavelength corresponds to the "nm" column.
     * E.g, 700, 800...
     *
     * @param end the ending wavelength
     */
    public void setEndWavelength( int end )
    {
        m_EndWavelength = end;
    }

    public Reflectance[] doImport( File file, Map<String, String> attributes )
        throws IOException, ImportException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( file );
            Reflectance[] reflectances = doImport( fis, attributes );
            if( reflectances != null && reflectances.length == 1 )
            {
                // ML - Use the File name as the reflectance name since the
                // file doesn't contain the "full" reflectance name.
                String fileName = file.getName();
                int start = fileName.lastIndexOf( "/" );
                int end = fileName.lastIndexOf( "." );
                String name = null;
                if( start >= 0 )
                {
                    name = fileName.substring( start + 1, end );
                }
                else
                {
                    name = fileName.substring( 0, end );
                }
                reflectances[ 0 ].setName( name );
            }
            return reflectances;
        }
        finally
        {
            if( fis != null )
            {
                fis.close();
            }
        }
    }

    /**
     * @param spectrumMap
     *
     * @return
     */
    private Reflectance createReflectance( SortedMap spectrumMap )
    {
        Reflectance reflectance = ReflectanceImpl.create( null );
        reflectance.setSpectrumMap( spectrumMap );
        return reflectance;
    }

    /**
     * @param spectrumMap
     * @param nm
     * @param str
     */
    private void parseWavelength( SortedMap spectrumMap, String nm, String str )
    {
        // ML - Skip the first 4 characters.
        str = str.substring( 4 );
        for( int i = 0; i <= 90; i += 10 )
        {
            // ML - Read 6 characters.
            String value = null;
            if( str.length() >= 6 )
            {
                value = str.substring( 0, 6 ).trim();
            }
            else
            {
                value = str.substring( 0, str.length() ).trim();
            }
            //m_Logger.debug( "Value parsed : " + value );
            // ML - Remove the characters that was read
            // but depending which nm, an extra character may be skipped.
            int length = str.trim().length();
            if( length > 0 && i != 0 && i != 10 && i != 40 && i != 60 && i != 90 )
            {
                str = str.substring( 7 );
            }
            else if( length > 0 && i < 90 )
            {
                if( length >= 6 )
                {
                    str = str.substring( 6 );
                }
                else
                {
                    str = str.substring( length );
                }
            }
            //m_Logger.debug( "Line for next input : " + str );
            if( value.equals( "" ) )
            {
                // ML - No values, skip
                continue;
            }
            Double reflValue = new Double( Double.parseDouble( value ) / 100.0 );
            // ML - If the value is 0, check the previous key, if no value is
            // present, it is still the start, don't store it. This would allow
            // future files that has values starting from 360 to be stored.
            if( reflValue.equals( ZERO ) )
            {
                Integer previousKey = new Integer( Integer.parseInt( nm ) - i );
                if( !spectrumMap.containsKey( previousKey ) )
                {
                    continue;
                }
            }

            Integer key = new Integer( Integer.parseInt( nm ) + i );
            //m_Logger.debug( "" + key + " = " + value );
            spectrumMap.put( key, reflValue );
        }
    }

    public Reflectance[] doImport( InputStream stream, Map<String, String> attributes )
        throws IOException, ImportException
    {
        InputStreamReader isr = null;
        BufferedReader br = null;
        try
        {
            isr = new InputStreamReader( stream );
            br = new BufferedReader( isr );
            Collection wavelengths = new HashSet();
            for( int i = m_StartWavelength; i <= m_EndWavelength; i += 100 )
            {
                wavelengths.add( "" + i );
            }
            SortedMap spectrumMap = new TreeMap();
            for( String str = br.readLine(); str != null; str = br.readLine() )
            {
                str = str.trim();
                if( str.length() >= 3 )
                {
                    //m_Logger.debug( "Reading line " + str );
                    String nm = str.substring( 0, 3 );
                    if( wavelengths.contains( nm ) )
                    {
                        //m_Logger.debug( "Parsing wavelength : " + nm );
                        parseWavelength( spectrumMap, nm, str.substring( 3 ) );
                    }
                }
            }
            if( spectrumMap.size() > 0 )
            {
                return new Reflectance[]{ createReflectance( spectrumMap ) };
            }
            throw new ImportException( "No spectrum data is found in file!" );
        }
        finally
        {
            if( isr != null )
            {
                isr.close();
            }
            if( br != null )
            {
                br.close();
            }
        }
    }
}
