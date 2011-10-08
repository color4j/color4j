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
 * ExecMeasurement.java
 *
 * Created on July 19, 2002, 4:06 PM
 */

package org.color4j.spectro.gretagmacbeth.spectrolino;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;

/**
 * @author chc
 */
public class ExecMeasurementCommand implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( SpectrolinoStatus.class.getName() );

    public String getName()
    {
        return "Exec Measurement Command";
    }

    public String construct()
    {
        String command;

        command = "; 32 ";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );
        m_Logger.info( "ExecMeasurementCommand result = " + response );

        boolean error = false;
        SpectrolinoStatus status = SpectrolinoStatus.create( " 250 0" );

        // Both result of the command and measurement result contain in this string
        // So we need to seperate them out first
        StringTokenizer token = new StringTokenizer( response, "\r\n" );

        // Make sure only 3
        if( token.countTokens() == 3 )
        {
            SpectroSettings settings = new SpectroSettings();
            settings.setAperture( new SmallAperture() );
            settings.setLensPosition( new SmallAreaView() );
            settings.setLightFilter( new D65LightFilter() );

            String temp1 = token.nextToken();
            String temp2 = token.nextToken();
            String temp3 = token.nextToken();

            m_Logger.info( "Line 1=" + temp1 );
            m_Logger.info( "Line 2=" + temp2 );
            m_Logger.info( "Line 3=" + temp3 );

            SpectrolinoStatus tempstatus = SpectrolinoStatus.create( temp1 );
            SpectrolinoStatus tempstatus2 = SpectrolinoStatus.create( temp3 );

            if( tempstatus.isFailure() || tempstatus2.isFailure() )
            {
                Iterator list = tempstatus.getErrors().iterator();

                while( list.hasNext() )
                {
                    if( list.next().equals( "MSG_UNKNOWN_STRING" ) )
                    {
                        status.addError( "MSG_INVALID_RETURN" );
                        return new SpectroEvent( this, tempstatus );
                    }
                }
            }
            // Get the reading out if the status is success
            else if( tempstatus.isSuccess() && tempstatus2.isSuccess() )
            {
                // Check whether is a reading string
                if( Pattern.matches( ": 190 .*", temp2 ) )
                {
                    temp2 = temp2.replaceFirst( ": 190 [0-9] [0-9]", "" );

                    StringTokenizer result = new StringTokenizer( temp2, " " );

                    Map values = new TreeMap( new DoubleCompare() );

                    int currentWavelength = 380;
                    int interval = 10;

                    m_Logger.info( "Parsing data" );
                    m_Logger.info( "Parsing " + result.countTokens() + " lines" );

                    // Now get the result of the value
                    while( result.hasMoreTokens() )
                    {
                        // TODO
                        String tempnumber = result.nextToken().trim();

                        try
                        {
                            double data = Double.parseDouble( tempnumber );

                            m_Logger.info( "Spectrolino Measure Command : " + currentWavelength + " -> " + data );
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

                    // Now construct the reading
                    SpectrolinoReading reading = new SpectrolinoReading( tempstatus, settings, values );

                    if( error )
                    {
                        status.addError( "MSG_INVALID_DATA" );
                        return new SpectroEvent( this, status );
                    }

                    return new SpectroEvent( this, reading );
                }
                else
                {
                    // Check the status string
                    m_Logger.info( "Invalid return values : Can not find string 190 !!!!" );

                    status.addError( "MSG_INVALID_RETURN" );
                    return new SpectroEvent( this, status );
                } // if
            }
        }
        else if( token.countTokens() == 2 )
        {
            SpectroSettings settings = new SpectroSettings();

            String temp2 = token.nextToken();
            String temp1 = token.nextToken();

            m_Logger.info( "Line 1=" + temp2 );
            m_Logger.info( "Line 2=" + temp1 );

            SpectrolinoStatus tempstatus = SpectrolinoStatus.create( temp1 );

            if( tempstatus.isFailure() )
            {
                Iterator list = tempstatus.getErrors().iterator();

                while( list.hasNext() )
                {
                    if( list.next().equals( "MSG_UNKNOWN_STRING" ) )
                    {
                        status.addError( "MSG_INVALID_RETURN" );
                        return new SpectroEvent( this, tempstatus );
                    }
                }
            }
            // Get the reading out if the status is success
            else if( tempstatus.isSuccess() )
            {
                // Check whether is a reading string
                if( Pattern.matches( ": 190 .*", temp2 ) )
                {
                    temp2 = temp2.replaceFirst( ": 190 [0-9] [0-9]", "" );

                    StringTokenizer result = new StringTokenizer( temp2, " " );

                    Map values = new TreeMap( new DoubleCompare() );

                    int currentWavelength = 380;
                    int interval = 10;

                    m_Logger.info( "Parsing data" );
                    m_Logger.info( "Parsing " + result.countTokens() + " lines" );

                    // Now get the result of the value
                    while( result.hasMoreTokens() )
                    {
                        // TODO
                        String tempnumber = result.nextToken().trim();

                        try
                        {
                            double data = Double.parseDouble( tempnumber );

                            m_Logger.info( "Spectrolino Measure Command : " + currentWavelength + " -> " + data );
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

                    // Now construct the reading
                    SpectrolinoReading reading = new SpectrolinoReading( tempstatus, settings, values );

                    if( error )
                    {
                        status.addError( "MSG_INVALID_DATA" );
                        return new SpectroEvent( this, status );
                    }

                    return new SpectroEvent( this, reading );
                }
                else
                {
                    // Check the status string
                    m_Logger.info( "Invalid return values : Can not find sring 190 !!!!" );

                    status.addError( "MSG_INVALID_RETURN" );
                    return new SpectroEvent( this, status );
                } // if
            }
        }
        else
        {
            // if the measurement result string is not correct
            m_Logger.info( "This is not three lines, Line : " + token.countTokens() );
            m_Logger.info( "Extra lines ! : " + response );

            status.addError( "MSG_INVALID_RETURN" );
            return new SpectroEvent( this, status );
        }

        return new SpectroEvent( this, status );
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
