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

public class C_10 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.000, 0.006,
            0.071, 0.519, 1.690, 3.050, 4.055,
            3.974, 3.207, 2.067, 0.792, 0.123,
            0.008, 0.297, 0.939, 1.944, 3.259,
            4.739, 6.340, 7.694, 8.479, 8.929,
            8.630, 7.794, 6.446, 4.848, 3.191,
            1.986, 1.114, 0.577, 0.280, 0.130,
            0.059, 0.027, 0.012, 0.005, 0.003,
            0.001, 0.001, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.001,
            0.007, 0.054, 0.173, 0.364,
            0.638, 0.936, 1.316, 1.938,
            2.693, 3.489, 4.395, 5.276,
            6.275, 7.299, 8.401, 8.926,
            8.995, 8.357, 7.236, 6.171,
            5.020, 3.966, 2.978, 2.114,
            1.323, 0.793, 0.439, 0.226,
            0.109, 0.050, 0.023, 0.010,
            0.005, 0.002, 0.001, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.000, 0.000, 0.025,
            0.317, 2.362, 7.995, 15.015,
            20.751, 21.364, 18.457, 13.957,
            7.968, 4.126, 2.006, 0.935,
            0.480, 0.244, 0.114, 0.030,
            -0.003, 0.001, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 97.286;
    static final private double m_CksY = 99.999;
    static final private double m_CksZ = 116.144;

    static final private double m_WhiteX = 97.285;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 116.145;

    public C_10()
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
