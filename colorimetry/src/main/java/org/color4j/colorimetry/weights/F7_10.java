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

public class F7_10 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.000, -0.021,
            0.156, 0.493, 0.461, 4.148,
            5.863, 2.334, 2.532, 1.682,
            0.677, 0.106, 0.004, 0.278,
            0.934, 1.518, 4.342, 6.462,
            5.108, 7.520, 10.048, 8.809,
            8.627, 7.521, 5.988, 4.332,
            2.715, 1.659, 0.912, 0.354,
            0.134, 0.058, 0.023, 0.009,
            0.003, 0.001, 0.001, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, -0.001,
            0.016, 0.051, 0.030, 0.535,
            0.885, 0.589, 1.034, 1.578,
            2.224, 2.935, 3.858, 4.979,
            6.139, 6.168, 10.634, 12.551,
            7.073, 8.149, 8.637, 6.065,
            5.018, 3.826, 2.766, 1.886,
            1.126, 0.663, 0.359, 0.138,
            0.052, 0.022, 0.009, 0.003,
            0.001, 0.001, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.000, 0.001, -0.101,
            0.700, 2.259, 2.055, 20.642,
            29.819, 12.685, 14.581, 11.335,
            6.711, 3.483, 1.770, 0.888,
            0.462, 0.221, 0.126, 0.058,
            -0.006, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.00
        };

    static final private double m_CksX = 95.791;
    static final private double m_CksY = 99.999;
    static final private double m_CksZ = 107.689;

    static final private double m_WhiteX = 95.792;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 107.686;

    public F7_10()
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

