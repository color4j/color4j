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
 * WhiteEndCommand.java
 *
 * Created on June 26, 2002, 5:33 PM
 */

package org.color4j.spectro.datacolor.common;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author chc
 */
public class WhiteEndCommand
    implements SpectroCommand
{
    int m_Readings;
    private ResponseDecoder decoder;

    /**
     * Creates a new instance of WhiteEndCommand
     */
    public WhiteEndCommand( Integer numberReadings, ResponseDecoder decoder )
    {
        this.decoder = decoder;
        m_Readings = numberReadings.intValue();
    }

    public String getName()
    {
        return "White Calibration Command";
    }

    public String construct()
    {
        String command = "W" + m_Readings + "R ";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        StringTokenizer sTok = new StringTokenizer( response, "\r\n" );

        boolean warning = false;
        boolean error = false;

        if( sTok.countTokens() != 9 )
        {
            warning = true;
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

        if( status.substring( 3, 6 ).equals( "000" ) )
        {
            settings.setLightFilter( new UVIncludedLightFilter() );
        }
        else if( status.substring( 3, 6 ).equals( "001" ) )
        {
            settings.setLightFilter( new CutOff400LightFilter() );
        }
        else if( status.substring( 3, 6 ).equals( "002" ) )
        {
            settings.setLightFilter( new CutOff420LightFilter() );
        }
        else if( status.substring( 3, 6 ).equals( "003" ) )
        {
            settings.setLightFilter( new CutOff460LightFilter() );
        }
        else
        {
            warning = true;
        }

        //Finish parsing settings;

        //Parse status Errors

        SpectroStatus newStatus = decoder.decode( status );

        //Finish parsing errors;

        //Start parsing measurement data:
        // Specification states that there are 40 values
        // 8 lines of 5 reflectance starting from 360 to 750
        // at 10nm interval

        Map values = new TreeMap();

        int currentWavelength = 360;
        int interval = 10;

        for( int i = 0; i < 8; i++ )
        {
            String line = sTok.nextToken();

            StringTokenizer dataTok = new StringTokenizer( line, "," );

            for( int j = 0; j < 5; j++ )
            {
                String data = dataTok.nextToken();

                try
                {
                    values.put( new Double( currentWavelength ), new Double( data ) );
                }
                catch( NumberFormatException numEx )
                {
                    warning = true;
                }

                currentWavelength += interval;
            }
        }

        DCIReading reading = new DCIReading( newStatus, settings, values );

        if( error )
        {
            return null;
        }

        return new SpectroEvent( this, reading );
    }
}