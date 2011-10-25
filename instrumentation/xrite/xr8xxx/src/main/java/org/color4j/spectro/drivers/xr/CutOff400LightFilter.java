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
 * Created on Nov 5, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.color4j.spectro.drivers.xr;

import org.color4j.spectro.spi.LightFilter;

public class CutOff400LightFilter implements LightFilter
{
    String m_Name;
    int m_CutOff1;
    int m_CutOff2;
    int m_Type;

    public CutOff400LightFilter()
    {
        m_Name = "400 nm";
        m_CutOff1 = 400;
        m_CutOff2 = m_CutOff1;
        m_Type = TYPE_LOWPASS;
    }

    public String getName()
    {
        return m_Name;
    }

    public String getDisplayName()
    {
        return m_Name;
    }

    public int getCutoffWavelength2()
    {
        return m_CutOff2;
    }

    public int getCutoffWavelength1()
    {
        return m_CutOff1;
    }

    public boolean isBandpassFilter()
    {
        return ( m_Type == TYPE_BANDPASS );
    }

    public boolean isLowpassFilter()
    {
        return ( m_Type == TYPE_LOWPASS );
    }

    public boolean isHighpassFilter()
    {
        return ( m_Type == TYPE_HIGHPASS );
    }

    public boolean isNotchFilter()
    {
        return ( m_Type == TYPE_NOTCH );
    }

    public int getType()
    {
        return m_Type;
    }
}
