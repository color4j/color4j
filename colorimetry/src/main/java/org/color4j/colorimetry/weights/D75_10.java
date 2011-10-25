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

public class D75_10 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.001, 0.006,
            0.119, 0.745, 1.985, 2.773,
            4.009, 4.254, 3.437, 2.112,
            0.805, 0.116, 0.008, 0.328,
            1.051, 2.203, 3.392, 4.713,
            5.997, 7.149, 8.154, 8.303,
            8.386, 7.580, 6.088, 4.312,
            2.843, 1.669, 0.940, 0.504,
            0.236, 0.102, 0.051, 0.025,
            0.010, 0.006, 0.003, 0.001,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.001,
            0.013, 0.077, 0.205, 0.330,
            0.628, 0.998, 1.417, 1.986,
            2.751, 3.374, 4.534, 5.863,
            7.042, 8.241, 8.711, 8.858,
            8.493, 7.773, 6.959, 5.736,
            4.885, 3.855, 2.811, 1.879,
            1.179, 0.666, 0.371, 0.197,
            0.092, 0.040, 0.020, 0.010,
            0.004, 0.002, 0.001, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, -0.001, 0.005, 0.026,
            0.535, 3.396, 9.410, 13.652,
            20.503, 22.859, 19.79, 14.275,
            8.104, 3.981, 2.105, 1.043,
            0.539, 0.274, 0.116, 0.030,
            -0.003, 0.001, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 94.416;
    static final private double m_CksY = 100.102;
    static final private double m_CksZ = 120.640;

    static final private double m_WhiteX = 94.413;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 120.641;

    public D75_10()
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
