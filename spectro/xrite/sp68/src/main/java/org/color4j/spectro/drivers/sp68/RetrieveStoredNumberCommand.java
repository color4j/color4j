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

public class RetrieveStoredNumberCommand implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( RetrieveStoredNumberCommand.class.getName() );

    static
    {
    }

    public RetrieveStoredNumberCommand()
    {
    }

    public String getName()
    {
        return "Get Stored Number Command";
    }

    public String construct()
    {
        return "gn";
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SP68Status status = SP68Status.create( response );

        try
        {
            Pattern pattern = Pattern.compile( ".*\\s*([0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]).*" );

            Matcher matcher = pattern.matcher( response );
            if( matcher.find() )
            {
                String number = matcher.group( 1 );
                m_Logger.info( "RAW STRING : '" + number + "'" );

                try
                {

                    int storedNumber = Hex.hexStringToInt( number.trim() );

                    status.addMessage( "STORED NUMBER : " + storedNumber );
                    m_Logger.info( "STORED NUMBER : " + storedNumber );
                }
                catch( NumberFormatException numEx )
                {
                    m_Logger.info( "Unable to parse number of stored reflectances : " + number );
                    return null;
                }
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
