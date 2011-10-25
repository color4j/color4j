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

public class C_2 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.001, 0.004, 0.015,
            0.074, 0.261, 1.170, 3.074,
            4.066, 3.951, 3.421, 2.292,
            1.066, 0.325, 0.025, 0.052,
            0.535, 1.496, 2.766, 4.274,
            5.891, 7.353, 8.459, 9.036,
            9.005, 8.380, 7.111, 5.300,
            3.669, 2.320, 1.333, 0.683,
            0.356, 0.162, 0.077, 0.038,
            0.018, 0.008, 0.004, 0.002,
            0.001, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000,
            0.000, 0.002, 0.007, 0.032,
            0.118, 0.259, 0.437, 0.684,
            1.042, 1.600, 2.332, 3.375,
            4.823, 6.468, 7.951, 9.193,
            9.889, 9.898, 9.186, 8.008,
            6.621, 5.302, 4.168, 3.147,
            2.174, 1.427, 0.873, 0.492,
            0.250, 0.129, 0.059, 0.028,
            0.014, 0.006, 0.003, 0.001,
            0.001, 0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.003, 0.017,
            0.069, 0.350, 1.241, 5.605,
            14.967, 20.346, 20.769,
            19.624, 15.153, 9.294, 5.115,
            2.788, 1.481, 0.669, 0.381,
            0.187, 0.081, 0.036, 0.019,
            0.015, 0.010, 0.007, 0.003,
            0.001, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 98.074;
    static final private double m_CksY = 99.999;
    static final private double m_CksZ = 118.231;

    static final private double m_WhiteX = 98.074;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 118.232;

    public C_2()
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
