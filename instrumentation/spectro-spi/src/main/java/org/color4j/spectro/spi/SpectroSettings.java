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

package org.color4j.spectro.spi;

import java.util.Map;

/**
 * Description of the settings for the Spectrophotometer
 * <p>The SpectroSettings are immutable, and will only trigger changes in the
 * Spectrophotometer if the <code>Spectrophotometer.setSettings()</code>
 * method is called.</p>
 *
 */
public class SpectroSettings
{
    private boolean m_Specular;
    private Aperture m_Aperture;
    private LightFilter m_Filter;
    private LensPosition m_LensPosition;
    private String m_Location;
    private Map m_CommParameters;

    public SpectroSettings()
    {
    }

    public void setSpecular( boolean specular )
    {
        m_Specular = specular;
    }

    public void setAperture( Aperture aperture )
    {
        m_Aperture = aperture;
    }

    public void setLightFilter( LightFilter lightFilter )
    {
        m_Filter = lightFilter;
    }

    public void setLensPosition( LensPosition lensPosition )
    {
        m_LensPosition = lensPosition;
    }

    public void setLocation( String location )
    {
        m_Location = location;
    }

    public void setCommParameters( Map commParam )
    {
        m_CommParameters = commParam;
    }

    public boolean getSpecular()
    {
        return m_Specular;
    }

    public String getLocation()
    {
        return m_Location;
    }

    public Aperture getAperture()
    {
        return m_Aperture;
    }

    public LightFilter getLightFilter()
    {
        return m_Filter;
    }

    public Map getCommParameters()
    {
        return m_CommParameters;
    }

    public LensPosition getLensPosition()
    {
        return m_LensPosition;
    }
}
