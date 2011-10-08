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

public class LoadReflectanceCommand implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( LoadReflectanceCommand.class.getName() );

    static
    {
    }

    protected Integer m_ReadingNumber;

    public LoadReflectanceCommand( Integer number )
    {
        m_ReadingNumber = number;
    }

    public String getName()
    {
        return "Load Reflectance Command";
    }

    public String construct()
    {
        return m_ReadingNumber + "oo";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SP68Status status = SP68Status.create( response );

        try
        {
            Pattern pattern = Pattern.compile( ".*(\\d\\d\\d).*" );

            Matcher matcher = pattern.matcher( response );
            if( matcher.find() )
            {
                status.addMessage( "LOADED READING : " + matcher.group( 1 ) );
            }
            else
            {
                m_Logger.info( "Incorrect response received: " + response );
                return null;
            }
        }
        catch( PatternSyntaxException patSynEx )
        {
            m_Logger.info( "Malformed Regular Expression" );
            return null;
        }

        return new SpectroEvent( this, status );
    }
}
