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

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

public class SetBaudRateCommand implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( SetBaudRateCommand.class.getName() );

    public static final String BAUD300 = "03";
    public static final String BAUD600 = "06";
    public static final String BAUD1200 = "0C";
    public static final String BAUD2400 = "18";
    public static final String BAUD4800 = "30";
    public static final String BAUD9600 = "60";
    public static final String BAUD19200 = "C0";

    protected String m_Baud = BAUD9600;

    public SetBaudRateCommand( String baud )
    {
        m_Baud = baud;
    }

    public String getName()
    {
        return "Set Baud Rate Command";
    }

    public String construct()
    {
        return m_Baud + "t";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SP68Status status = SP68Status.create( response );

        try
        {
            Pattern pattern = Pattern.compile( "\\s(\\d+)[\r\n]" );

            Matcher matcher = pattern.matcher( response );

            if( matcher.find() )
            {
                String baudrate = matcher.group( 1 );

                status.addMessage( "BAUD SET AT " + baudrate );
            }
            else
            {
                m_Logger.info( "Incorrect response" );
                return null;
            }
        }
        catch( PatternSyntaxException patSynEx )
        {
            m_Logger.info( "Malformed Regular Expression" );
        }

        return new SpectroEvent( this, status );
    }
}
