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

package org.color4j.colorimetry.weights;

import org.color4j.colorimetry.encodings.XYZ;

public class D55_2 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.001, 0.004, 0.011,
            0.072, 0.232, 0.897, 1.872, 2.881,
            3.169, 2.831, 1.874, 0.896, 0.266,
            0.026, 0.050, 0.554, 1.624, 2.807,
            4.236, 5.660, 7.052, 8.575, 8.968,
            9.626, 9.151, 7.698, 5.508, 3.916,
            2.356, 1.393, 0.757, 0.383, 0.162,
            0.087, 0.045, 0.018, 0.011, 0.005,
            0.002, 0.001, 0.001, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,
            0.002, 0.006, 0.026, 0.071,
            0.181, 0.348, 0.567, 0.849,
            1.346, 1.902, 3.042, 4.806,
            6.779, 8.605, 9.303, 9.789,
            9.497, 8.829, 8.123, 6.574,
            5.681, 4.550, 3.406, 2.258,
            1.523, 0.885, 0.514, 0.277,
            0.139, 0.059, 0.031, 0.016,
            0.007, 0.004, 0.002, 0.001,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.006, 0.019, 0.051,
            0.343, 1.105, 4.303, 9.113,
            14.405, 16.648, 16.238, 12.388,
            7.807, 4.187, 2.570, 1.490,
            0.707, 0.411, 0.188, 0.080,
            0.035, 0.018, 0.015, 0.010,
            0.008, 0.003, 0.002, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 95.678;
    static final private double m_CksY = 99.998;
    static final private double m_CksZ = 92.150;

    static final private double m_WhiteX = 95.682;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 92.149;

    public D55_2()
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
