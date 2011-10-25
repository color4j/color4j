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

package org.color4j.colorimetry.weights;

import org.color4j.colorimetry.encodings.XYZ;

public class C_2_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.066, -0.164, 2.373,
            8.595, 6.939, 2.045, -0.217,
            0.881, 5.406, 11.842, 17.169,
            18.383, 14.348, 7.148, 2.484,
            0.600, 0.136, 0.031, 0.006,
            0.002, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.001, 0.044,
            0.491, 1.308, 3.062, 6.596,
            12.925, 18.650, 20.143, 16.095,
            10.537, 6.211, 2.743, 0.911,
            0.218, 0.049, 0.011, 0.002,
            0.001, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.311, -0.777, 11.296,
            42.561, 39.899, 18.451, 4.728,
            1.341, 0.319, 0.059, 0.028,
            0.013, 0.002, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000
        };

    static final private double m_CksX = 98.073;
    static final private double m_CksY = 99.998;
    static final private double m_CksZ = 118.231;

    static final private double m_WhiteX = 98.074;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 118.232;

    public C_2_20()
    {
    }

    public double[] getWeightsX()
    {
        return m_WX;
    }

    public double[] getWeightsY()
    {
        return m_WY;
    }

    public double[] getWeightsZ()
    {
        return m_WZ;
    }

    public XYZ getWhitePoint()
    {
        return new XYZ( m_WhiteX, m_WhiteY, m_WhiteZ );
    }

    public XYZ getChecksum()
    {
        return new XYZ( m_CksX, m_CksY, m_CksZ );
    }
}
