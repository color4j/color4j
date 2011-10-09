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

package org.color4j.spectro.datacolor.common;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 */

public class SetSpecularCommand
    implements SpectroCommand
{
    boolean m_Specular;
    private ResponseDecoder decoder;

    public SetSpecularCommand( Boolean included, ResponseDecoder decoder )
    {
        this.decoder = decoder;
        m_Specular = included.booleanValue();
    }

    public String getName()
    {
        return "Set Specular Command";
    }

    public String construct()
    {
        String command;

        command = "G" + ( m_Specular ? "I" : "E" ) + " ";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        if( response.charAt( 0 ) != ( m_Specular ? 'I' : 'E' ) )
        {
            return null;
        }
        else if( response.charAt( 12 ) != 'x' )
        {
            return null;
        }
        else
        {
            SpectroStatus spectroStatus = decoder.decode( response );
            return new SpectroEvent( this, spectroStatus );
        }
    }
}