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

public class CutOff460LightFilter
    implements LightFilter
{
    protected String m_DisplayName;
    protected String m_Name;
    protected int m_Cutoff1;
    protected int m_Cutoff2;
    protected int m_Type;

    public CutOff460LightFilter()
    {
        m_DisplayName = "460 nm";
        m_Name = "460 nm";
        m_Cutoff1 = 460;
        m_Cutoff2 = 460;
        m_Type = TYPE_LOWPASS;
    }

    public String getName()
    {
        return m_Name;
    }

    public int getCutoffWavelength2()
    {
        return m_Cutoff2;
    }

    public int getCutoffWavelength1()
    {
        return m_Cutoff1;
    }

    public boolean isBandpassFilter()
    {
        return m_Type == TYPE_BANDPASS;
    }

    public boolean isLowpassFilter()
    {
        return m_Type == TYPE_LOWPASS;
    }

    public boolean isHighpassFilter()
    {
        return m_Type == TYPE_HIGHPASS;
    }

    public boolean isNotchFilter()
    {
        return m_Type == TYPE_NOTCH;
    }

    public int getType()
    {
        return m_Type;
    }

    public String getDisplayName()
    {
        return m_DisplayName;
    }

    public String toString()
    {
        return m_DisplayName;
    }
}
