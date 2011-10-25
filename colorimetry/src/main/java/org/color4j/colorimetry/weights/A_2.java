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

public class A_2 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.001, 0.004,
            0.017, 0.057, 0.246, 0.660,
            0.942, 1.039, 1.043, 0.790,
            0.416, 0.148, 0.016, 0.028,
            0.388, 1.187, 2.288, 3.702,
            5.484, 7.562, 9.739, 11.644,
            12.811, 12.782, 11.460, 8.991,
            6.536, 4.296, 2.583, 1.405,
            0.780, 0.388, 0.200, 0.106,
            0.054, 0.028, 0.014, 0.007,
            0.003, 0.002, 0.001
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.002, 0.007, 0.025,
            0.059, 0.113, 0.205, 0.353,
            0.608, 1.012, 1.749, 3.047,
            4.778, 6.345, 7.625, 8.594,
            9.255, 9.496, 9.265, 8.567,
            7.563, 6.365, 5.076, 3.689,
            2.543, 1.616, 0.954, 0.514,
            0.283, 0.140, 0.072, 0.038,
            0.020, 0.010, 0.005, 0.002,
            0.001, 0.001, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.001, 0.005, 0.018,
            0.081, 0.272, 1.178, 3.214,
            4.710, 5.454, 5.969, 5.209,
            3.602, 2.277, 1.493, 0.963,
            0.505, 0.305, 0.157, 0.071,
            0.034, 0.020, 0.018, 0.013,
            0.010, 0.004, 0.002, 0.001,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 109.848;
    static final private double m_CksY = 99.997;
    static final private double m_CksZ = 35.586;

    static final private double m_WhiteX = 109.850;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 35.585;

    public A_2()
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
