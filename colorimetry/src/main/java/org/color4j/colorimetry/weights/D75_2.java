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

public class D75_2 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.003, 0.008, 0.021,
            0.120, 0.378, 1.403, 2.820,
            4.028, 4.244, 3.677, 2.350,
            1.087, 0.313, 0.029, 0.058,
            0.599, 1.702, 2.890, 4.265,
            5.592, 6.853, 8.161, 8.429,
            8.777, 8.176, 6.737, 4.728,
            3.279, 1.956, 1.128, 0.599,
            0.301, 0.128, 0.067, 0.036,
            0.014, 0.009, 0.004, 0.002,
            0.001, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.001,
            0.003, 0.010, 0.040, 0.108,
            0.254, 0.467, 0.739, 1.071,
            1.642, 2.262, 3.484, 5.371,
            7.281, 9.005, 9.564, 9.845,
            9.375, 8.571, 7.725, 6.174,
            5.176, 4.064, 2.980, 1.938,
            1.275, 0.735, 0.417, 0.219,
            0.109, 0.046, 0.024, 0.013,
            0.005, 0.003, 0.002, 0.001,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.012, 0.038, 0.098,
            0.567, 1.798, 6.728, 13.727, 20.146,
            22.301, 21.106, 15.552, 9.485,
            4.951, 2.929, 1.657, 0.754, 0.430,
            0.192, 0.081, 0.034, 0.018, 0.015,
            0.009, 0.007, 0.003, 0.001,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 94.972;
    static final private double m_CksY = 99.999;
    static final private double m_CksZ = 122.639;

    static final private double m_WhiteX = 94.972;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 122.638;

    public D75_2()
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
