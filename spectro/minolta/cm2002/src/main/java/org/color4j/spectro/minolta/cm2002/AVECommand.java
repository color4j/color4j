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
 * AVECommand.java
 *
 * Created on October 14, 2002, 3:16 PM
 */

package org.color4j.spectro.minolta.cm2002;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author chc
 */
public class AVECommand implements SpectroCommand
{
    protected int m_Measurements;

    /**
     * Creates a new instance of AVECommand
     */
    public AVECommand()
    {
        m_Measurements = 1;
    }

    public AVECommand( Integer measurements )
    {
        m_Measurements = measurements.intValue();
    }

    public String construct()
    {
        return "AVE\r\n" + m_Measurements;
    }

    public String getName()
    {
        return "Set Automatic Averaging Number Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM2002Status.create( new String( values ) ) );
    }
}