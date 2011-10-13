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

package org.color4j.colorimetry;

import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.colorimetry.entities.Spectro;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ReflectanceImpl
    implements Reflectance
{
    private SortedMap m_SpectrumMap;
    private Spectrum m_Spectrum;
    private Map m_Conditions;
    private Spectro m_Spectro;
    private String m_Type;
    private String m_Name;

    public static Reflectance create( Spectrum spectrum )
    {
        return new ReflectanceImpl( spectrum, null, null );
    }

    public static Reflectance create( Spectrum spectrum,
                                      Spectro spectro,
                                      Map conditions
    )
    {
        return new ReflectanceImpl( spectrum, spectro, conditions );
    }

    public ReflectanceImpl()
    {
        m_Name = "<unidentified>";
    }

    private ReflectanceImpl( Spectrum spectrum, Spectro spectro, Map conditions )
    {
        this();
        m_Spectrum = spectrum;
        m_Spectro = spectro;
        m_Conditions = conditions;
        createSpectrumMap();
    }

    public Spectrum getSpectrum()
    {
        return m_Spectrum;
    }

    public void setSpectrum( Spectrum spectrum )
    {
        m_Spectrum = spectrum;

        createSpectrumMap();
    }

    private void createSpectrumMap()
    {
        TreeMap map = new TreeMap();
        if( m_Spectrum != null )
        {
            int start = m_Spectrum.getShortestWavelength();
            int end = m_Spectrum.getLongestWavelength();
            int interval = m_Spectrum.getInterval();
            double[] readings = m_Spectrum.getValues();

            int count = 0;
            for( int i = start; i <= end; i += interval )
            {
                map.put( new Integer( i ), new Double( readings[ count++ ] ) );
            }
        }
        m_SpectrumMap = map;
    }

    public SortedMap getSpectrumMap()
    {
        return m_SpectrumMap;
    }

    public void setSpectrumMap( SortedMap map )
    {
        m_SpectrumMap = map;
    }

    public Spectro getSpectro()
    {
        return m_Spectro;
    }

    public void setSpectro( Spectro spectro )
    {
        m_Spectro = spectro;
    }

    public String getType()
    {
        return m_Type;
    }

    public void setType( String type )
    {
        m_Type = type;
    }

    public String getName()
    {
        return m_Name;
    }

    public Map getConditions()
    {
        return m_Conditions;
    }

    public Class getTypeInterface()
    {
        return Reflectance.class;
    }

    public java.util.Date getDeletedDate()
    {
        return null;
    }

    public void setConditions( Map condition )
    {
        m_Conditions = condition;
    }
}