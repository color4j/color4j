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
 * MinoltaReading.java
 *
 * Created on March 18, 2007, 5:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.color4j.spectro.minolta.cm2600d;

import java.util.Map;
import org.color4j.spectro.spi.SpectroReading;
import org.color4j.spectro.spi.SpectroSettings;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author Robin Lee
 */
public class MinoltaReading
    implements SpectroReading
{
    private SpectroStatus m_Status;
    private SpectroSettings m_Settings;
    private Map m_Values;

    /**
     * Creates a new instance of MinoltaReading
     *
     * @param status   The SpectroStatus returned as a result of this reading.
     * @param settings The settings used for this reading.
     * @param values   A Map<Integer,Double> where the key is the nanometers of the reading and the value is the relative value.
     */
    public MinoltaReading( SpectroStatus status, SpectroSettings settings, Map values )
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

    public void setSettings( SpectroSettings settings )
    {
        m_Settings = settings;
    }

    public String toString()
    {
        if( m_Status.isSuccess() )
        {
            return "Readings:" + m_Values;
        }
        return "Reading Error:" + m_Status.getErrors();
    }
}