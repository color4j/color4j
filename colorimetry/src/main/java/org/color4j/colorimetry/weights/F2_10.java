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

public class F2_10 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.001, -0.020,
            0.130, 0.326, 0.088, 3.107,
            4.387, 1.169, 1.441, 0.954,
            0.383, 0.060, 0.002, 0.151,
            0.528, 0.934, 3.551, 6.295,
            6.984, 11.012, 14.508, 13.512,
            12.111, 9.208, 6.030, 3.450,
            1.702, 0.767, 0.317, 0.122,
            0.045, 0.017, 0.006, 0.002,
            0.001, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, -0.001, 0.014,
            0.034, -0.005, 0.407, 0.658,
            0.312, 0.587, 0.895, 1.257,
            1.664, 2.156, 2.752, 3.519,
            3.956, 8.791, 12.243, 9.828,
            11.985, 12.451, 9.315, 7.032,
            4.671, 2.776, 1.496, 0.704,
            0.305, 0.125, 0.048, 0.017,
            0.007, 0.002, 0.001, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.000, 0.003, -0.097,
            0.588, 1.494, 0.303, 15.491,
            22.288, 6.415, 8.294, 6.428,
            3.794, 1.973, 0.989, 0.492,
            0.267, 0.148, 0.108, 0.056,
            -0.004, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 103.281;
    static final private double m_CksY = 100.002;
    static final private double m_CksZ = 69.030;

    static final private double m_WhiteX = 103.279;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 69.027;

    public F2_10()
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

