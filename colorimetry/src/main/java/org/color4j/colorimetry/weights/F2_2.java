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

public class F2_2 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.001, -0.007,
            0.082, 0.175, -0.048, 2.994,
            4.235, 1.115, 1.462, 1.020,
            0.487, 0.150, 0.008, 0.025,
            0.292, 0.656, 2.917, 5.409,
            6.217, 10.109, 13.826, 13.136,
            12.110, 9.497, 6.361, 3.637,
            1.867, 0.864, 0.363, 0.140,
            0.054, 0.021, 0.008, 0.003,
            0.001, 0.001, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,
            0.002, 0.005, -0.010, 0.139,
            0.248, 0.145, 0.290, 0.463,
            0.714, 1.063, 1.592, 2.406,
            3.473, 4.112, 9.247, 12.968,
            10.369, 12.644, 13.167, 9.598,
            7.113, 4.706, 2.802, 1.484,
            0.723, 0.324, 0.134, 0.051,
            0.020, 0.008, 0.003, 0.001,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.00, 0.004, -0.038,
            0.390, 0.836, -0.293, 14.707,
            21.081, 5.992, 8.373, 6.727,
            4.211, 2.353, 1.318, 0.738,
            0.370, 0.214, 0.176, 0.124,
            0.034, 0.027, 0.024, 0.014,
            0.009, 0.003, 0.001, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 99.188;
    static final private double m_CksY = 100.004;
    static final private double m_CksZ = 67.395;

    static final private double m_WhiteX = 99.186;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 67.393;

    public F2_2()
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

