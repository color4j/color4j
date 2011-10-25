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
 * OISCommand.java
 *
 * Created on October 14, 2002, 3:12 PM
 */

package org.color4j.spectro.minolta.cm2002;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class OISCommand implements SpectroCommand
{
    private String m_Observer;
    private String m_Illuminant;

    /**
     * Creates a new instance of OISCommand
     */
    public OISCommand( String Observer, String Illuminant )
    {
        m_Observer = Observer;
        m_Illuminant = Illuminant;
    }

    public String construct()
    {
        return "OIS\r\n" + m_Observer + m_Illuminant;
    }

    public String getName()
    {
        return "Observer, Illuminant Set Command";
    }

    public org.color4j.spectro.spi.SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM2002Status.create( new String( values ) ) );
    }
}
