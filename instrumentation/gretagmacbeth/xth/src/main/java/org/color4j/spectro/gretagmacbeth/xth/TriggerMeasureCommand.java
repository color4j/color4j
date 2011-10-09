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

package org.color4j.spectro.gretagmacbeth.xth;

import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;

/**
 * @author hywong
 */

public class TriggerMeasureCommand implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( TriggerMeasureCommand.class.getName() );

    public String getName()
    {
        return "Trigger Measure Command";
    }

    public String construct()
    {
        String command;

        command = "T";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        m_Logger.finer( "Interpreting > " + response );

        // Now for the data
        StringTokenizer sTok = new StringTokenizer( response, "\r\n" );

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

        if( status.charAt( 6 ) == '2' )
        {
            settings.setAperture( new MediumAperture() );
            settings.setLensPosition( new MediumAreaView() );
        }
        else if( status.charAt( 6 ) == '3' )
        {
            settings.setAperture( new SmallAperture() );
            settings.setLensPosition( new SmallAreaView() );
        }
        else
        {
            m_Logger.warning( "Invalid command generated" );
        }

        settings.setLightFilter( new UVIncludedLightFilter() );

        // Actual creation of status
        XTHStatus newStatus = XTHStatus.create( status );

        if( status.charAt( 0 ) == '9' )
        {
            newStatus.addError( "White calibration required" );
        }

        if( status.charAt( 1 ) == '9' )
        {
            newStatus.addError( "Black calibration required" );
        }

        if( newStatus.isFailure() )
        {
            return new SpectroEvent( this, newStatus );
        }

        // Now proccess the data
        Map values = new TreeMap( new DoubleCompare() );

        int currentWavelength = 360;
        int interval = 10;

        m_Logger.finer( "Parsing data" );
        m_Logger.finer( "Parsing " + sTok.countTokens() + " lines" );

        while( sTok.hasMoreTokens() )
        {
            if( currentWavelength > 750 )
            {
                break;
            }

            String line = sTok.nextToken();

            m_Logger.finer( "Parsing line" );

            StringTokenizer dataTok = new StringTokenizer( line, "," );

            m_Logger.finer( "Found " + dataTok.countTokens() + " data tokens" );

            while( dataTok.hasMoreTokens() )
            {
                try
                {
                    double data = Double.parseDouble( dataTok.nextToken().trim() );

                    data = data / 100.00;

                    m_Logger.finer( "XTH Trigger Measure Command : " + currentWavelength + " -> " + data );
                    values.put( new Double( currentWavelength ), new Double( data ) );
                }
                catch( NumberFormatException numEx )
                {
                    warning = true;
                }

                currentWavelength += interval;
            }
        }

        XTHReading reading = new XTHReading( newStatus, settings, values );

        if( error )
        {
            return null;
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
