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

public class F7_2 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.001, -0.004,
            0.105, 0.266, 0.203, 4.113,
            5.834, 2.301, 2.650, 1.855,
            0.889, 0.273, 0.016, 0.052,
            0.537, 1.118, 3.686, 5.727,
            4.699, 7.124, 9.875, 8.833,
            8.895, 7.999, 6.510, 4.709,
            3.068, 1.924, 1.076, 0.417,
            0.168, 0.073, 0.029, 0.013,
            0.005, 0.002, 0.001, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,
            0.003, 0.007, -0.005, 0.186,
            0.346, 0.278, 0.527, 0.842,
            1.303, 1.933, 2.937, 4.495,
            6.254, 6.620, 11.541, 13.711,
            7.698, 8.867, 9.422, 6.445,
            5.236, 3.978, 2.879, 1.929,
            1.192, 0.723, 0.397, 0.152,
            0.061, 0.026, 0.011, 0.005,
            0.002, 0.001, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, -0.001, 0.005, -0.021,
            0.499, 1.265, 0.904, 20.179,
            29.065, 12.246, 15.185, 12.235,
            7.684, 4.285, 2.432, 1.372,
            0.661, 0.329, 0.214, 0.131,
            0.021, 0.019, 0.017, 0.010,
            0.007, 0.003, 0.001, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 95.042;
    static final private double m_CksY = 100.002;
    static final private double m_CksZ = 108.747;

    static final private double m_WhiteX = 95.041;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 108.747;

    public F7_2()
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

