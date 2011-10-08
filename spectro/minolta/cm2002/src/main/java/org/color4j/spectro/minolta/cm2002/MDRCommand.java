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
 * MDRCommand.java
 *
 * Created on October 14, 2002, 3:29 PM
 */

package org.color4j.spectro.minolta.cm2002;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;
import org.color4j.spectro.spi.SpectroReading;

/**
 * @author chc
 */
public class MDRCommand implements SpectroCommand
{
    private SpectroReading m_Reflectance;
    private String m_DataID;
    private String m_Comment;
    private String[] m_Data;

    /**
     * Creates a new instance of MDRCommand
     */
    public MDRCommand()
    {
    }

    public String construct()
    {
        return "MDR";
    }

    public String getName()
    {
        return "One Memory Data Request";
    }

    public SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM2002Status.create( new String( values ) ) );
    }

    public SpectroReading getReflectance()
    {
        return m_Reflectance;
    }

    public String getDataID()
    {
        return m_DataID;
    }

    public String getComment()
    {
        return m_Comment;
    }

    public String[] getData()
    {
        return m_Data;
    }
}
