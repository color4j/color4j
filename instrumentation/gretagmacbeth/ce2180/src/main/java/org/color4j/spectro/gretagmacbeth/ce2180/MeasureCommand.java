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

package org.color4j.spectro.gretagmacbeth.ce2180;

import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
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

        command = "M";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        //response = response.substring( 0, response.indexOf( "\r\n" ) );

        // Now for the data
        StringTokenizer sTok = new StringTokenizer( response, "\n" );

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

        settings.setAperture( new MediumAperture() );

        settings.setLensPosition( new MediumAreaView() );

        settings.setLightFilter( new UVIncludedLightFilter() );

        // Actual creation of status
        CE2180Status newStatus = CE2180Status.create( status );

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

        m_Logger.log( Level.FINER, "Parsing data" );
        m_Logger.log( Level.FINER, "Parsing " + sTok.countTokens() + " lines" );

        while( sTok.hasMoreTokens() )
        {
            String line = sTok.nextToken();

            m_Logger.log( Level.FINER, "Parsing line" );

            StringTokenizer dataTok = new StringTokenizer( line, "," );

            m_Logger.log( Level.FINER, "Found " + dataTok.countTokens() + " data tokens" );

            while( dataTok.hasMoreTokens() )
            {
                String tempnumber = dataTok.nextToken().trim();

                try
                {
                    double data = Double.parseDouble( tempnumber );

                    data = data / 100.00;

                    m_Logger.log( Level.FINER, currentWavelength + " -> " + data );
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

        CE2180Reading reading = new CE2180Reading( newStatus, settings, values );

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