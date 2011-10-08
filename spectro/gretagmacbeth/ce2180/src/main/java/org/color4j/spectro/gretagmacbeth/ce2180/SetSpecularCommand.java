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
 * SetSpecularCommand.java
 *
 * Created on July 11, 2002, 5:46 PM
 */

package org.color4j.spectro.gretagmacbeth.ce2180;

import java.util.Iterator;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author chc
 */
public class SetSpecularCommand implements SpectroCommand
{

    boolean m_Specular;

    /**
     * Creates a new instance of SetSpecularCommand
     */
    public SetSpecularCommand( Boolean specular )
    {
        m_Specular = specular.booleanValue();
    }

    public String getName()
    {
        return "Set Specular Command";
    }

    public String construct()
    {
        String command;

        command = m_Specular ? "J" : "K";

        return command;
    }

    public SpectroEvent interpret( byte[] bytes )
    {
        String received = new String( bytes );

        SpectroStatus status = CE2180Status.create( received );

        if( status.isFailure() )
        {
            Iterator list = status.getErrors().iterator();

            while( list.hasNext() )
            {
                if( list.next().equals( "MSG_UNKNOWN_STATUS" ) )
                {
                    return null;
                }
            }
        }

        if( m_Specular )
        {
            if( received.charAt( 3 ) != '0' )
            {
                return null;
            }
        }
        else
        {
            if( received.charAt( 3 ) != '1' )
            {
                return null;
            }
        }

        return new SpectroEvent( this, status );
    }
}