/*
 * Copyright (c) 2000-2011 Niclas Hedhman.
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

public class Spectrum
{
    private int m_StartWavelength;
    private int m_Interval;
    private double[] m_Values;

    static public Spectrum create( int start, int interval, double[] values )
    {
        return new Spectrum( start, interval, values );
    }

    static public Spectrum create( int start, int interval, float[] values )
    {
        return new Spectrum( start, interval, values );
    }

    protected Spectrum( int start, int interval, double[] values )
    {
        m_StartWavelength = start;
        m_Interval = interval;
        m_Values = values;
    }

    protected Spectrum( int start, int interval, float[] values )
    {
        m_StartWavelength = start;
        m_Interval = interval;
        double[] v = new double[ values.length ];
        for( int i = 0; i < values.length; i++ )
        {
            v[ i ] = values[ i ];
        }
        m_Values = v;
    }

    /**
     * @return the longest wavelength included in the Spectrum.
     */
    public int getLongestWavelength()
    {
        return ( m_Values.length - 1 ) * m_Interval + m_StartWavelength;
    }

    /**
     * @return the shortest wavelength included in the Spectrum.
     */
    public int getShortestWavelength()
    {
        return m_StartWavelength;
    }

    /**
     * @return the Interval between the wavelength readings.
     */
    public int getInterval()
    {
        return m_Interval;
    }

    public double[] getValues()
    {
        double[] result = new double[ m_Values.length ];
        System.arraycopy( m_Values, 0, result, 0, result.length );
        return result;
    }
}