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

package org.color4j.spectro.drivers.sp68;

import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroSettings;

public class RetrieveReflectanceCommand implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( RetrieveReflectanceCommand.class.getName() );

    protected boolean m_Specular;

    static
    {
    }

    public RetrieveReflectanceCommand( Boolean specular )
    {
        if( specular != null )
        {
            m_Specular = specular.booleanValue();
        }
    }

    public String getName()
    {
        return "Retrieve Reflectance Command";
    }

    public String construct()
    {
        return ( m_Specular ? "0" : "2" ) + "ms";
    }

    public SpectroEvent interpret( byte[] received )
    {
        SpectroSettings settings = new SpectroSettings();

        String response = new String( received );

        SP68Status status = SP68Status.create( response );

        try
        {
            Pattern tokenPattern = Pattern.compile( "[\r\n]" );

            String[] tokens = tokenPattern.split( response );

            if( Pattern.matches( ".*INCLUDED.*", tokens[ 0 ] ) )
            {
                m_Logger.info( "Specular Included" );
                settings.setSpecular( true );
            }
            else if( Pattern.matches( ".*EXCLUDED.*", tokens[ 0 ] ) )
            {
                m_Logger.info( "Specular Excluded" );
                settings.setSpecular( false );
            }

            Pattern dataPattern = Pattern.compile( "\\s*(\\d?\\d?[.]\\d\\d).*" );

            TreeMap values = new TreeMap();

            double currentWavelength = 400;

            for( int i = 0; i < tokens.length; i++ )
            {
                Matcher matcher = dataPattern.matcher( tokens[ i ] );

                if( matcher.find() )
                {
                    String value = matcher.group( 1 );

                    try
                    {
                        double dValue = Double.parseDouble( value );
                        values.put( new Double( currentWavelength ), new Double( dValue / 100 ) );

                        m_Logger.info( ">" + new Double( currentWavelength ) + " -> " + values.get( new Double( currentWavelength ) ) );
                        currentWavelength += 10;
                    }
                    catch( NumberFormatException numEx )
                    {
                        m_Logger.info( "Error while parsing reading at " + currentWavelength + " -> " + value );
                    }
                }
                else
                {
                    m_Logger.info( "No match found : " + tokens[ i ] );
                }
            }

            SP68Reading reading = new SP68Reading( status, settings, values );
            m_Logger.info( "Reading created..." );

            return new SpectroEvent( this, reading );
        }
        catch( PatternSyntaxException patSynEx )
        {
            m_Logger.info( "Malformed Regular Expression" );
        }

        return new SpectroEvent( this, status );
    }
}
