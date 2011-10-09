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
 * CE3000Reading.java
 *
 * Created on July 11, 2002, 4:24 PM
 */

package org.color4j.spectro.gretagmacbeth.ce3000;

import java.util.Map;
import org.color4j.spectro.spi.SpectroReading;
import org.color4j.spectro.spi.SpectroSettings;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author chc
 */
public class CE3000Reading implements SpectroReading
{
    private SpectroStatus m_Status;
    private SpectroSettings m_Settings;
    private Map m_Values;

    /**
     * Creates a new instance of CE3000Reading
     */
    public CE3000Reading( SpectroStatus status, SpectroSettings settings, Map values )
    {
        m_Status = status;
        m_Settings = settings;
        m_Values = values;
    }

    public SpectroStatus getStatus()
    {
        return m_Status;
    }

    public Map getValues()
    {
        return m_Values;
    }

    public SpectroSettings getSettings()
    {
        return m_Settings;
    }

    public void setSettings( SpectroSettings s )
    {
        m_Settings = s;
    }
}
