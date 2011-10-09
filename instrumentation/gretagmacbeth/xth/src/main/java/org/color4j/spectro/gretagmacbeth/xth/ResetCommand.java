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

package org.color4j.spectro.gretagmacbeth.xth;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author hywong
 */

public class ResetCommand
    implements SpectroCommand
{
    /**
     * Creates a new instance of GetVersionCommand
     */
    public ResetCommand()
    {
    }

    public String getName()
    {
        return "Reset Command";
    }

    public String construct()
    {
        String command;

        command = "R";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SpectroStatus status = new XTHStatus();

        try
        {
            Pattern pattern = Pattern.compile( "(\\S\\S\\d\\d\\d\\d)[\r\n]" );

            Matcher matcher = pattern.matcher( response );

            if( matcher.find() )
            {
                status.addMessage( "SERIAL:" + matcher.group( 1 ) );
            }
            else
            {
                return null;
            }
        }
        catch( PatternSyntaxException patSynEx )
        {
            Logger.getLogger( ResetCommand.class.getName() ).info( "Malformed Regular Expression" );
            return null;
        }

        return new SpectroEvent( this, status );
    }
}
