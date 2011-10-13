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

public class A_10 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.000, 0.002,  // 360-390
            0.018, 0.118, 0.372, 0.686,  // 400-430
            0.982, 1.094, 1.024, 0.747,  // 440-470
            0.326, 0.061, 0.003, 0.189,  // 480-510
            0.717, 1.617, 2.823, 4.296,  // 520-550
            6.177, 8.285, 10.218, 12.041,  // 560-590
            12.850, 12.441, 10.872, 8.604,  // 600-630
            5.951, 3.846, 2.259, 1.242,  // 640-670
            0.643, 0.324, 0.160, 0.078,  // 680-710
            0.039, 0.019, 0.010, 0.005,  // 720-750
            0.002, 0.001, 0.001           // 760-780
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,  // 360-390
            0.002, 0.012, 0.038, 0.082,  // 400-430
            0.154, 0.255, 0.414, 0.688,  // 440-470
            1.073, 1.589, 2.397, 3.503,  // 480-510
            4.857, 6.096, 7.290, 8.116,  // 520-550
            8.799, 9.039, 8.758, 8.350,  // 560-590
            7.492, 6.337, 5.025, 3.753,  // 600-630
            2.469, 1.537, 0.891, 0.485,  // 640-670
            0.250, 0.126, 0.062, 0.030,  // 680-710
            0.015, 0.007, 0.004, 0.002,  // 720-750
            0.001, 0.001, 0.000           // 760-780
        };

    static final private double[] m_WZ =
        {
            0.000, 0.000, 0.000, 0.007,  // 360-390
            0.078, 0.540, 1.760, 3.374,  // 400-430
            5.024, 5.876, 5.882, 5.023,  // 440-470
            3.236, 1.926, 1.129, 0.638,  // 480-510
            0.377, 0.205, 0.100, 0.028,  // 520-550
            -0.003, 0.001, 0.000, 0.000,  // 560-590
            0.000, 0.000, 0.000, 0.000,  // 600-630
            0.000, 0.000, 0.000, 0.000,  // 640-670
            0.000, 0.000, 0.000, 0.000,  // 680-710
            0.000, 0.000, 0.000, 0.000,  // 720-750
            0.000, 0.000, 0.000           // 760-780
        };

    static final private double m_CksX = 111.143;
    static final private double m_CksY = 99.999;
    static final private double m_CksZ = 35.201;

    static final private double m_WhiteX = 111.144;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 35.200;

    public A_10()
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
