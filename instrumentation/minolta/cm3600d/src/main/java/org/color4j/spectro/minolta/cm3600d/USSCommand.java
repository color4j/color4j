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
 * USSCommand.java
 *
 * Created on March 18, 2007, 11:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.color4j.spectro.minolta.cm3600d;

import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class USSCommand implements SpectroCommand, CommandStruc
{
    private boolean m_Enable;

    /**
     * Creates a new instance of USSCommand
     */
    public USSCommand( Boolean enable )
    {
        m_Enable = enable.booleanValue();
    }

    public String construct()
    {
        StringBuffer command = new StringBuffer( "USC" );

        if( m_Enable )
        {
            command.append( ",1" );
        }
        else
        {
            command.append( ",0" );
        }

        command.append( DELIM );

        String cmd = command.toString();
        command = null;

        return cmd;
    }

    public String getName()
    {
        return "User White Calibration Commmand";
    }

    public SpectroEvent interpret( byte[] valuesin )
    {
        String response = new String( valuesin );
        StringTokenizer sTok = new StringTokenizer( response, "," + DELIM );

        if( sTok.countTokens() == 1 )
        {
            String returnCode = sTok.nextToken();

            CM3600dStatus status = CM3600dStatus.create( returnCode );

            return new SpectroEvent( this, status );
        }
        else
        {
            CM3600dStatus errstatus = CM3600dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }
}
