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

public class F11_2 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.001, -0.009,
            0.061, 0.107, -0.205, 2.800,
            4.264, 1.277, 1.367, 0.695,
            0.435, 0.341, -0.004, 0.007,
            -0.001, -0.925, 9.613, 11.438,
            0.196, 0.602, 7.021, 9.070,
            4.247, 29.903, 13.567, 3.446,
            0.630, 0.534, 0.297, 0.084,
            0.043, 0.028, 0.013, 0.020,
            0.001, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,
            0.002, 0.003, -0.014, 0.130,
            0.251, 0.164, 0.280, 0.255,
            0.754, 2.063, 1.088, 0.469,
            0.229, -2.067, 29.254, 28.030,
            -0.695, 0.870, 6.565, 6.866,
            2.617, 14.812, 6.132, 1.329,
            0.240, 0.199, 0.110, 0.031,
            0.016, 0.010, 0.005, 0.007,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.000, 0.005, -0.048,
            0.291, 0.511, -1.044, 13.758,
            21.231, 6.849, 7.848, 4.495,
            3.956, 4.778, 0.792, 0.054,
            0.032, 0.000, 0.535, 0.300,
            -0.031, 0.002, 0.012, 0.011,
            0.004, 0.010, 0.003, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 100.964;
    static final private double m_CksY = 100.005;
    static final private double m_CksZ = 64.354;

    static final private double m_WhiteX = 100.962;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 64.350;

    public F11_2()
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

