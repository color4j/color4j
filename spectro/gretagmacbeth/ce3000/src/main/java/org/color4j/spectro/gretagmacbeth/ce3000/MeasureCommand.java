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

/*
 * MeasureCommand.java
 *
 * Created on July 11, 2002, 4:27 PM
 */

package org.color4j.spectro.gretagmacbeth.ce3000;

import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroReading;
import org.color4j.spectro.spi.SpectroSettings;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author hywong
 */

public class MeasureCommand
    implements SpectroCommand
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( MeasureCommand.class.getName() );
    }

    /**
     * Creates a new instance of MeasureCommand
     */
    public MeasureCommand()
    {

    }

    public String getName()
    {
        return "Measure Command";
    }

    public String construct()
    {
        String command;

        command = "m";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        // Now for the data
        StringTokenizer sTok = new StringTokenizer( response, "\n" );

        // Create status first
        String status = sTok.nextToken();
        SpectroSettings settings = createSettings( status );

        // Actual creation of status
        SpectroStatus newStatus = new CE3000Status( status.getBytes() );

        // Now proccess the data
        Map values = parseData( sTok );

        SpectroReading reading = new CE3000Reading( newStatus, settings, values );

        return new SpectroEvent( this, reading );
    }

    private Map parseData( StringTokenizer sTok )
    {
        Map values = new TreeMap( new DoubleCompare() );

        int currentWavelength = 360;
        int interval = 20;

        m_Logger.finer( "Parsing data" );
        m_Logger.finer( "Parsing " + sTok.countTokens() + " lines" );

        while( sTok.hasMoreTokens() )
        {
            String line = sTok.nextToken();

            m_Logger.finer( "Parsing line" );

            StringTokenizer dataTok = new StringTokenizer( line, "," );

            m_Logger.finer( "Found " + dataTok.countTokens() + " data tokens" );

            while( dataTok.hasMoreTokens() )
            {
                String tempnumber = dataTok.nextToken().trim();

                try
                {
                    double data = Double.parseDouble( tempnumber );

                    data = data / 100.00;

                    m_Logger.finer( currentWavelength + " -> " + data );
                    values.put( new Double( currentWavelength ), new Double( data ) );
                }
                catch( NumberFormatException numEx )
                {
                    if( tempnumber.length() > 2 )
                    {
                        m_Logger.info( "Number format exception : Invalid values returned" );
                    }
                }

                currentWavelength += interval;
            }
        }

        return values;
    }

    private SpectroSettings createSettings( String status )
    {
        SpectroSettings settings = new SpectroSettings();

        if( status.charAt( 2 ) == 'I' )
        {
            settings.setSpecular( true );
        }
        else if( status.charAt( 2 ) == 'E' )
        {
            settings.setSpecular( false );
        }
        else
        {
            m_Logger.finer( "no specular set!" );
        }

        if( status.charAt( 3 ) == 'I' )
        {
            settings.setLightFilter( new UVIncludedLightFilter() );
        }
        else if( status.charAt( 3 ) == 'O' )
        {
            settings.setLightFilter( new UVExcludedLightFilter() );
        }
        else
        {
            m_Logger.finer( "no light filter set!" );
        }

        if( status.charAt( 4 ) == 'S' )
        {
            //KH : status doesn't know about aperture size
            settings.setAperture( new SmallAperture() );
            settings.setLensPosition( new SmallAreaView() );
        }
        else if( status.charAt( 4 ) == 'L' )
        {
            //KH : status doesn't know about aperture size
            settings.setAperture( new LargeAperture() );
            settings.setLensPosition( new LargeAreaView() );
        }
        else
        {
            m_Logger.finer( "no aperture set!" );
        }

        return settings;
    }

    class DoubleCompare implements Comparator
    {
        /**
         * Description of the Method
         *
         * @param o1 Description of the Parameter
         * @param o2 Description of the Parameter
         *
         * @return Description of the Return Value
         *
         * @throws ClassCastException Description of the Exception
         */
        public int compare( Object o1, Object o2 )
            throws ClassCastException
        {
            Double d1 = (Double) o1;
            Double d2 = (Double) o2;

            return (int) ( d1.doubleValue() - d2.doubleValue() );
        }
    }
}