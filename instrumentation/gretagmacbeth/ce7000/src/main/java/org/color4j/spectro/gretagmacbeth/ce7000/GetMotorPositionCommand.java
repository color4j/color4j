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
 * GetMotorPositionCommand.java
 *
 * Created on July 26, 2002, 5:25 PM
 */

package org.color4j.spectro.gretagmacbeth.ce7000;

import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 */
public class GetMotorPositionCommand implements SpectroCommand
{
    protected int motorsteps = -1;

    static private Logger m_Logger;

    static
    {
        m_Logger = Logger.getLogger( GetMotorPositionCommand.class.getName() );
    }

    /**
     * Creates a new instance of GetMotorPositionCommand
     */
    public GetMotorPositionCommand()
    {
    }

    public String getName()
    {
        return "Get Motor Position Command";
    }

    public String construct()
    {
        String command = "K";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        m_Logger.info( "CE7000 Spectro : Result from GetMotorPositionCommand, " + response );

        try
        {
            motorsteps = Integer.parseInt( response.substring( 7, 11 ) );
        }
        catch( IndexOutOfBoundsException inExp )
        {
            SpectroStatus status = CE7000Status.create( response );
            status.addError( "MSG_MOTORPOSITION_INTERPRET_FAIL" );
            return new SpectroEvent( this, status );
        }
        catch( NumberFormatException noExp )
        {
            SpectroStatus status = CE7000Status.create( response );
            status.addError( "MSG_MOTORPOSITION_INTERPRET_FAIL" );
            return new SpectroEvent( this, status );
        }

        SpectroStatus status = CE7000Status.create( response );
        return new SpectroEvent( this, status );
    }

    public int getMotorsteps()
    {
        return motorsteps;
    }
}