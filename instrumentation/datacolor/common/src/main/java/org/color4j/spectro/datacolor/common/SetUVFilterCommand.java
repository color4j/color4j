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

import org.color4j.spectro.spi.LightFilter;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroStatus;

/**
 */

public class SetUVFilterCommand
    implements SpectroCommand
{
    LightFilter m_LightFilter;
    private ResponseDecoder decoder;
    String param;

    public SetUVFilterCommand( LightFilter lightFilter, ResponseDecoder decoder )
    {
        m_LightFilter = lightFilter;
        this.decoder = decoder;
    }

    public String getName()
    {
        return "Set Light Filter Command";
    }

    public String construct()
    {
        String command;

        if( m_LightFilter.getName().equals( "UV Inc" ) )
        {
            param = "000";
        }
        else if( m_LightFilter.getName().equals( "400 nm" ) )
        {
            param = "001";
        }
        else if( m_LightFilter.getName().equals( "420 nm" ) )
        {
            param = "002";
        }
        else if( m_LightFilter.getName().equals( "460 nm" ) )
        {
            param = "003";
        }
        else
        {
            //error should not happen
            return null;
        }

        command = "F" + param + " ";

        return command;
    }

    public SpectroEvent interpret( byte[] received )
    {
        String response = new String( received );

        if( response.substring( 3, 6 ).equals( param ) )
        {
            SpectroStatus status = decoder.decode( response );
            return new SpectroEvent( this, status );
        }

        return null;
    }
}
