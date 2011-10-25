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
 * TNSCommand.java
 *
 * Created on October 14, 2002, 3:24 PM
 */

package org.color4j.spectro.minolta.cm2002;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class TNSCommand implements SpectroCommand
{
    private String m_Number;

    /**
     * Creates a new instance of TNSCommand
     */
    public TNSCommand( String Number )
    {
        m_Number = Number;
    }

    public String construct()
    {
        return "TNS\r\n" + m_Number;
    }

    public String getName()
    {
        return "Target Number Set Command";
    }

    public org.color4j.spectro.spi.SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM2002Status.create( new String( values ) ) );
    }
}
