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

package org.color4j.spectro.minolta.cm2600d;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;

/**
 * Enables the Switch on the spectrophotometer to be used for making the actual
 * measurement.
 *
 */
public class SDRCommand
    implements SpectroCommand, CommandStruc
{
    private int m_Environ;
    private int m_Sample;

    public SDRCommand( int environ, int sample )
    {
        m_Environ = environ;
        m_Sample = sample;
    }

    public String getName()
    {
        return "Store Data Request";
    }

    public String construct()
    {
        return "SDR," + m_Environ + ", 0, " + m_Sample + DELIM;
    }

    public SpectroEvent interpret( byte[] response )
    {
        StringTokenizer packets = new StringTokenizer( new String( response ), DELIM );
        String infoPacket = packets.nextToken();
        StringTokenizer infos = new StringTokenizer( infoPacket, "," );
        String check = infos.nextToken();
        if( check.startsWith( "OK" ) )
        {
            String gloss = infos.nextToken();
            String uv = infos.nextToken();
            SpectroSettings settings = createSettings( uv, gloss );
            String spectral1 = packets.nextToken();
            SpectroEvent event = MESCommand.interpretSpectralData( spectral1, this, settings );
            return event;
        }
        else
        {
            CM2600dStatus status = CM2600dStatus.create( check );
            return new SpectroEvent( this, status );
        }
    }

    private SpectroSettings createSettings( String uv, String gloss )
    {
        SpectroSettings settings = new SpectroSettings();
        settings.setAperture( new MediumAperture() );
        if( uv.equals( "0" ) )
        {
            settings.setLightFilter( new UVIncludedLightFilter() );
        }
        else
        {
            settings.setLightFilter( new UVExcludedLightFilter() );
        }
        if( gloss.equals( "0" ) )
        {
            settings.setLensPosition( new MediumAreaView() );
            settings.setSpecular( true );
        }
        else if( gloss.equals( "1" ) )
        {
            settings.setLensPosition( new MediumAreaView() );
            settings.setSpecular( true );
        }
        else if( gloss.equals( "2" ) )
        {
            settings.setLensPosition( new MediumAreaView() );
            settings.setSpecular( false );
        }
        else if( gloss.equals( "3" ) )
        {
            settings.setLensPosition( new SmallAreaView() );
            settings.setSpecular( true );
        }
        else if( gloss.equals( "4" ) )
        {
            settings.setLensPosition( new SmallAreaView() );
            settings.setSpecular( true );
        }
        else if( gloss.equals( "5" ) )
        {
            settings.setLensPosition( new SmallAreaView() );
            settings.setSpecular( false );
        }
        return settings;
    }
}
