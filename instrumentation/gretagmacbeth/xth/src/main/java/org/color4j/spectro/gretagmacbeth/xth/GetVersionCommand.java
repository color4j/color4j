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

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 */

public class GetVersionCommand
    implements SpectroCommand
{
    /**
     * Creates a new instance of GetVersionCommand
     */
    public GetVersionCommand()
    {
    }

    public String getName()
    {
        return "Get Version Command";
    }

    public String construct()
    {
        String command;

        command = "v";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SpectroStatus status = new XTHStatus();

        if( response.matches( "[0-9]\\.[0-9]\\.[0-9]{2}" ) )
        {
            status.addMessage( "VERSION:" + response );
        }
        else
        {
            return null;
        }

        return new SpectroEvent( this, status );
    }
}