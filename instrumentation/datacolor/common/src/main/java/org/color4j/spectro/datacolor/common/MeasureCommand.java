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

package org.color4j.spectro.datacolor.common;

import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;
import org.color4j.spectro.spi.SpectroStatus;

public class MeasureCommand
    implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( MeasureCommand.class.getName() );

    int m_Flashes;
    private ResponseDecoder decoder;

    public MeasureCommand( Integer numberOfFlashes, ResponseDecoder decoder )
    {
        this.decoder = decoder;
        m_Flashes = numberOfFlashes.intValue();
    }

    public String getName()
    {
        return "Measure Command";
    }

    public String construct()
    {
        String command = "M" + m_Flashes + "@ ";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        StringTokenizer sTok = new StringTokenizer( response, "\r" + "\n" );

//        boolean warning = false;
        boolean error = false;

        if( sTok.countTokens() != 9 )
        {
//            warning = true;
            m_Logger.warning( "Warning expected number of lines not received." );
        }

        //Create status:
        //First line is status string
        SpectroSettings settings = new SpectroSettings();

        String status = sTok.nextToken();

        if( status.charAt( 0 ) == 'I' )
        {
            settings.setSpecular( true );
        }
        else if( status.charAt( 0 ) == 'E' )
        {
            settings.setSpecular( false );
        }
        else
        {
            //Error in status string.... try to continue
            error = true;
        }

        if( status.charAt( 1 ) == 'A' )
        {
            settings.setLensPosition( new ExtraUltraSmallAreaView() ); //XUSAV
            settings.setAperture( new ExtraUltraSmallAperture() );
        }
        else if( status.charAt( 1 ) == 'U' )
        {
            settings.setLensPosition( new UltraSmallAreaView() ); //USAV
            settings.setAperture( new UltraSmallAperture() );
        }
        else if( status.charAt( 1 ) == 'S' )
        {
            settings.setLensPosition( new SmallAreaView() ); //SAV
            settings.setAperture( new SmallAperture() );
        }
        else if( status.charAt( 1 ) == 'M' )
        {
            settings.setLensPosition( new MediumAreaView() ); //MAV
            settings.setAperture( new MediumAperture() );
        }
        else if( status.charAt( 1 ) == 'N' )
        {
            settings.setLensPosition( new LargeAreaView() ); //LAV
            settings.setAperture( new LargeAperture() );
        }
        else if( status.charAt( 1 ) == 'X' )
        {
            settings.setLensPosition( new ExtraLargeAreaView() ); //XLAV
            settings.setAperture( new ExtraLargeAperture() );
        }
        else
        {
            error = true;
        }

        int currentWavelength = 360;
        int start = 360;
        if( status.substring( 3, 6 ).equals( "000" ) )
        {
            settings.setLightFilter( new UVIncludedLightFilter() );
        }
        else if( status.substring( 3, 6 ).equals( "001" ) )
        {
            settings.setLightFilter( new CutOff400LightFilter() );
            start = 400;
        }
        else if( status.substring( 3, 6 ).equals( "002" ) )
        {
            settings.setLightFilter( new CutOff420LightFilter() );
            start = 420;
        }
        else if( status.substring( 3, 6 ).equals( "003" ) )
        {
            settings.setLightFilter( new CutOff460LightFilter() );
            start = 460;
        }
        else
        {
//            warning = true;
        }

        //Finish parsing settings;

        //Parse status Errors

        SpectroStatus newStatus = decoder.decode( status );

        //Finish parsing errors;

        //Start parsing measurement data:
        // Specification states that there are 40 values
        // 8 lines of 5 reflectance starting from 360 to 750
        // at 10nm interval

        Map values = new TreeMap( new DoubleCompare() );

        int interval = 10;

        for( int i = 0; i < 8; i++ )
        {
            String line = sTok.nextToken();

            StringTokenizer dataTok = new StringTokenizer( line, "," );

            for( int j = 0; j < 5; j++ )
            {
                String tempnumber = dataTok.nextToken().trim();

                if( currentWavelength > 700 )
                {
                    break;
                }

                if( currentWavelength < start )
                {
                    currentWavelength += interval;
                    continue;
                }

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
                        error = true;
                    }
                }

                currentWavelength += interval;
            }
        }

        DCIReading reading = new DCIReading( newStatus, settings, values );

        if( error )
        {
            newStatus.addError( "MSG_INVALID_DATA" );
            return new SpectroEvent( this, newStatus );
        }

        return new SpectroEvent( this, reading );
    }

    class DoubleCompare implements Comparator
    {
        public int compare( Object o1, Object o2 )
            throws ClassCastException
        {
            Double d1 = (Double) o1;
            Double d2 = (Double) o2;

            return (int) ( d1.doubleValue() - d2.doubleValue() );
        }
    }
}