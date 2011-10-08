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

package org.color4j.spectro.gretagmacbeth.ce7000;

import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;

/**
 * @author chc
 */
public class MeasureCommand implements SpectroCommand
{
    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( MeasureCommand.class.getName() );
    }

    //protected CustomLightFilter m_CustomLightFilter;

    /**
     * Creates a new instance of MeasureCommand
     */
    public MeasureCommand()
    {
        //m_CustomLightFilter = new CustomLightFilter();
    }

    public String getName()
    {
        return "Measure Command";
    }

    /*
    public void setCustomLightfilter( CustomLightFilter filter )
    {
        m_CustomLightFilter = filter;
    }
    */

    public String construct()
    {
        String command;

        command = "M";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String resultchecksum = "";
        int checksum = 256;

        String response = new String( received );

        //response = response.substring( 0, response.indexOf( "\r\n" ) );

        // Now for the data
        StringTokenizer sTok = new StringTokenizer( response, "\n" );

        boolean warning = false;
        boolean error = false;

        // Create status first
        SpectroSettings settings = new SpectroSettings();

        String status = sTok.nextToken();

        if( status.charAt( 3 ) == '0' )
        {
            settings.setSpecular( true );
        }
        else if( status.charAt( 3 ) == '1' )
        {
            settings.setSpecular( false );
        }

        if( status.charAt( 4 ) == '1' )
        {
            settings.setLightFilter( new UVExcludedLightFilter() );
        }
        else if( status.charAt( 4 ) == '2' )
        {
            settings.setLightFilter( new UVD65LightFilter() );
        }

        if( status.charAt( 5 ) == '0' )
        {
            settings.setLensPosition( new LargeAreaView() );
        }
        else if( status.charAt( 5 ) == '1' )
        {
            settings.setLensPosition( new MediumAreaView() );
        }
        else if( status.charAt( 5 ) == '2' )
        {
            settings.setLensPosition( new SmallAreaView() );
        }
        else if( status.charAt( 5 ) == '3' )
        {
            settings.setLensPosition( new VerySmallAreaView() );
        }

        if( status.charAt( 6 ) == '0' )
        {
            settings.setAperture( new LargeAperture() );
        }
        else if( status.charAt( 6 ) == '1' )
        {
            settings.setAperture( new VerySmallAperture() );
        }
        else if( status.charAt( 6 ) == '2' )
        {
            settings.setAperture( new MediumAperture() );
        }
        else if( status.charAt( 6 ) == '3' )
        {
            settings.setAperture( new SmallAperture() );
        }

        // Actual creation of status
        CE7000Status newStatus = CE7000Status.create( status );

        if( status.charAt( 0 ) == '9' )
        {
            newStatus.addError( "MSG_WHITE_REQUIRED" );
        }

        if( status.charAt( 1 ) == '9' )
        {
            newStatus.addError( "MSG_BLACK_REQUIRED" );
        }

        if( newStatus.isFailure() )
        {
            return new SpectroEvent( this, newStatus );
        }

        // Now proccess the data
        Map values = new TreeMap( new DoubleCompare() );

        int currentWavelength = 360;
        int interval = 10;

        m_Logger.info( "Parsing data" );
        m_Logger.info( "Parsing " + sTok.countTokens() + " lines" );

        while( sTok.hasMoreTokens() )
        {
            String line = sTok.nextToken();

            m_Logger.info( "Parsing line" );

            StringTokenizer dataTok = new StringTokenizer( line, "," );

            m_Logger.info( "Found " + dataTok.countTokens() + " data tokens" );

            if( dataTok.countTokens() == 1 )
            {
                resultchecksum = resultchecksum + line;
            }

            while( dataTok.hasMoreTokens() )
            {

                if( currentWavelength >= 760 )
                {
                    break;
                }

                String tempnumber = dataTok.nextToken().trim();

                try
                {
                    double data = Double.parseDouble( tempnumber );

                    data = data / 100.00;

                    m_Logger.info( "CE7000 Measure Command : " + currentWavelength + " -> " + data );
                    values.put( new Double( currentWavelength ), new Double( data ) );
                }
                catch( NumberFormatException numEx )
                {
                    if( tempnumber.length() > 2 )
                    {
                        m_Logger.info( "Number format exception : Invalid values returned" );
                        error = true;
                    }
                }

                currentWavelength += interval;
            }
        }

        // Get the checksum char from the string
        char temp = resultchecksum.charAt( 0 );
        int resultchecksumint = (int) temp;

        String checksumstring = response.substring( 0, response.indexOf( resultchecksumint ) );

        // Now calculate checksum
        for( int i = 0; i < checksumstring.length(); i++ )
        {
            checksum = checksum - (int) checksumstring.charAt( i );
        }

        if( ( checksum & 255 ) < 32 )
        {
            m_Logger.info( "Adding 32" );
            checksum = ( checksum & 255 ) + 32;
        }
        else
        {
            m_Logger.info( "Not adding 32" );
            checksum = checksum & 255;
        }

        m_Logger.info( "Actual CHECKSUM : [" + checksum + "]" );

        m_Logger.info( "Result String CHECKSUM : [" + resultchecksumint + "]" );

        CE7000Reading reading = new CE7000Reading( newStatus, settings, values );

        if( checksum != resultchecksumint )
        {
            //newStatus.addError( "MSG_CHECKSUM_ERROR" );
            //return new SpectroEvent( this, newStatus );
            m_Logger.info( "CHECKSUM ERROR : [" + response + "][" + resultchecksum + "]" );
        }

        if( error )
        {
            newStatus.addError( "MSG_INVALID_DATA" );
            return new SpectroEvent( this, newStatus );
        }

        return new SpectroEvent( this, reading );
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
