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
 * ResetCommand.java
 *
 * Created on July 11, 2002, 4:19 PM
 */

package org.color4j.spectro.gretagmacbeth.ce2180;

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
     * Creates a new instance of BlackTrapCommand
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

        SpectroStatus status = new CE2180Status();

        if( response.matches( "[a-zA-Z]{2}[0-9]{4}" ) )
        {
            status.addMessage( "SERIAL_NO:" + response );
        }
        else if( response.matches( "S[0-9]\\.[0-9]\\.[0-9]" ) )
        {
            status.addMessage( "STARTUP" );
        }
        else
        {
            return null;
        }

        return new SpectroEvent( this, status );
    }
}