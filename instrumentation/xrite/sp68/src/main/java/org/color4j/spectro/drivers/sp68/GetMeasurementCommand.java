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
import java.util.regex.Pattern;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

public class GetMeasurementCommand
    implements SpectroCommand
{
    boolean m_Specular;

    public GetMeasurementCommand( Boolean specular )
    {
        m_Specular = specular.booleanValue();
    }

    public String getName()
    {
        return "Get Measurement Command";
    }

    public String construct()
    {
        return ( m_Specular ? "0" : "1" ) + "ms";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        TreeMap values = new TreeMap();

        Pattern lexa = Pattern.compile( "[\r\n]" );

        String[] tokens = lexa.split( response );

        if( tokens.length > 31 )
        {
            for( int i = 0; i < 31; i++ )
            {
                try
                {
                    values.put( new Double( 400.00 + i * 10.0 ), new Double( tokens[ i ] ) );
                }
                catch( NumberFormatException numEx )
                {
                    Logger logger = Logger.getLogger( GetMeasurementCommand.class.getName() );
                    logger.info( "Exception while parsing " + tokens[ i ] + " at " + ( 400 + i * 10 ) );
                    values.put( new Double( 400.00 + i * 10.0 ), new Double( 0.0 ) );
                }
            }
        }

        SP68Reading reading = new SP68Reading( null, null, values );

        return new SpectroEvent( this, reading );
    }
}
