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
 * ParameterDownloadCommand.java
 *
 * Created on July 19, 2002, 4:04 PM
 */

package org.color4j.spectro.gretagmacbeth.spectrolino;

import java.util.Iterator;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author chc
 */
public class ParameterDownloadCommand implements SpectroCommand
{
    protected String Density;
    protected String Illum;
    protected String Observer;

    /**
     * Creates a new instance of ParameterDownloadCommand
     */
    public ParameterDownloadCommand( String m_Density, String m_Illum, String m_Observer )
    {
        Density = m_Density;
        Illum = m_Illum;
        Observer = m_Observer;
    }

    public String getName()
    {
        return "Parameter Download Command";
    }

    public String construct()
    {
        String command;

        command = "; 22 " + Density + " 1 " + Illum + " " + Observer;

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        SpectroStatus status = SpectrolinoStatus.create( response );

        if( status.isFailure() )
        {
            Iterator list = status.getErrors().iterator();

            while( list.hasNext() )
            {
                if( list.next().equals( "MSG_UNKNOWN_STRING" ) )
                {
                    return null;
                }
            }
        }

        return new SpectroEvent( this, status );
    }
}
