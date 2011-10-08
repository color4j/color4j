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
 * MESCommand.java
 *
 * Created on March 18, 2007, 5:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.color4j.spectro.minolta.cm2600d;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;

/**
 * @author Robin Lee
 * @author Niclas Hedhman
 */
public class MESCommand
    implements SpectroCommand, CommandStruc
{
    private static final Logger m_Logger = Logger.getLogger( MESCommand.class.getName() );

    private SpectroSettings m_Settings;

    /**
     * Creates a new instance of MESCommand
     *
     * @param settings The settings used to make the measurement.
     */
    public MESCommand( SpectroSettings settings )
    {
        m_Settings = settings;
    }

    public String construct()
    {
        return "MES" + DELIM;
    }

    public String getName()
    {
        return "Measurement Data Output Request";
    }

    public SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );
        StringTokenizer sTok = new StringTokenizer( response, DELIM );

        String line = "";
        int count = sTok.countTokens();
        for( int i = 0; i < count; i++ )
        {
            // Locate the Last Token
            // The measurement is made and will return 1,2,3,4 or 6 reflectance spectra
            // but it just so happens that for CPS 0,1,2,3 we always need the last one,
            // since we map the specular and lightfilter settings against those modes.
            line = sTok.nextToken();
        }
        return interpretSpectralData( line, this, m_Settings );
    }

    static SpectroEvent interpretSpectralData( String line, Object source, SpectroSettings settings )
    {
        StringTokenizer commas = new StringTokenizer( line, "," );
        String returnCode = commas.nextToken();
        CM2600dStatus status = CM2600dStatus.create( returnCode );

        // The instrument measure 39 values starting from 360nm.
        int wavelength = 360;
        Map values = new TreeMap();
        if( status.isSuccess() )
        {
            if( commas.countTokens() == 39 )
            {
                while( commas.hasMoreTokens() )
                {
                    String data = commas.nextToken();
                    try
                    {
                        Double doubleData = new Double( Double.parseDouble( data ) / 100 );
                        values.put( new Double( wavelength ), doubleData );
                        wavelength += 10;
                    }
                    catch( NumberFormatException numEx )
                    {
                        status.addError( "MSG_DATA_NUMBER_FORMAT_ERROR" );
                    }
                }
            }
            else
            {
                status.addError( "MSG_UNKNOWN_STRING" );
            }
        }

        MinoltaReading reading = new MinoltaReading( status, settings, values );
        m_Logger.info( "Measured: " + reading );
        return new SpectroEvent( source, reading );
    }
}
