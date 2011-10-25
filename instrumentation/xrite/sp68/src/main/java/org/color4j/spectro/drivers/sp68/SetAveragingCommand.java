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

public class SetAveragingCommand
    implements SpectroCommand
{
    static private Logger m_Logger = Logger.getLogger( SetAveragingCommand.class.getName() );

    static
    {
    }

    int m_Avg_Count;

    public SetAveragingCommand( Integer count )
    {
        m_Avg_Count = count.intValue();
    }

    public String getName()
    {
        return "Set Averaging Command";
    }

    public String construct()
    {
        if( m_Avg_Count < 10 )
        {
            return "0" + m_Avg_Count + "sv";
        }
        else
        {
            return m_Avg_Count + "sv";
        }
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SP68Status status = SP68Status.create( response );

        try
        {
            Pattern pattern = Pattern.compile( "\\s*(\\d\\d)\\s*(\\d\\d)[\r\n]?" );

            Matcher matcher = pattern.matcher( response );

            if( matcher.find() )
            {
                String count = matcher.group( 1 );
                String total = matcher.group( 2 );

                status.addMessage( count + " / " + total + " averages" );
            }
            else
            {
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
