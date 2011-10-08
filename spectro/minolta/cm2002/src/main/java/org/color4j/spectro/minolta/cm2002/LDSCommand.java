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
 * LDSCommand.java
 *
 * Created on October 14, 2002, 3:26 PM
 */

package org.color4j.spectro.minolta.cm2002;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author chc
 */
public class LDSCommand implements SpectroCommand
{
    private String m_Colormode;
    private String[] m_Tolerance;

    /**
     * Creates a new instance of LDSCommand
     */
    public LDSCommand( String Colormode, String[] Tolerance )
    {
        m_Colormode = Colormode;
        m_Tolerance = Tolerance;
    }

    public String construct()
    {
        StringBuffer command = new StringBuffer();

        command.append( "LDS\r\n" + m_Colormode + "\r\n" );

        for( int i = 0; i < m_Tolerance.length - 1; i++ )
        {
            command.append( m_Tolerance[ i ] + "\r\n" );
        }

        command.append( m_Tolerance[ m_Tolerance.length ] );

        return command.toString();
    }

    public String getName()
    {
        return "Limit Data Set Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM2002Status.create( new String( values ) ) );
    }
}
