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
 * Created on July 11, 2002, 5:46 PM
 */

package org.color4j.spectro.gretagmacbeth.ce7000;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author hywong
 */
public class ResetCommand implements SpectroCommand
{
    String m_HeadNumber;
    String m_TileNumber;

    /**
     * Creates a new instance of ResetCommand
     */
    public ResetCommand()
    {
    }

    public String getName()
    {
        return "Insert Command";
    }

    public String getSerialNumber()
    {
        return m_HeadNumber + " - " + m_TileNumber;
    }

    public String getHeadNumber()
    {
        return m_HeadNumber;
    }

    public String getTileNumber()
    {
        return m_TileNumber;
    }

    public String construct()
    {
        return "R";
    }

    public SpectroEvent interpret( byte[] bytes )
    {
        String received = new String( bytes );

        if( received.length() >= 12 )
        {
            m_HeadNumber = received.substring( 0, 6 );
            m_TileNumber = received.substring( 6 );
        }

        return new SpectroEvent( this );
    }
}
