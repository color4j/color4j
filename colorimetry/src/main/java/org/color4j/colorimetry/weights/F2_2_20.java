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

public class F2_2_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, -0.015, 0.126, 0.723,
            7.638, 2.320, 0.931, -0.106,
            0.034, 5.711, 13.144, 27.390,
            24.880, 12.425, 3.276, 0.613,
            0.082, 0.014, 0.002, 0.000,
            0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, -0.001, 0.006, 0.016,
            0.413, 0.518, 1.364, 3.077,
            5.636, 18.719, 23.526, 25.997,
            13.965, 5.247, 1.258, 0.222,
            0.030, 0.005, 0.001, 0.000,
            0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, -0.075, 0.604, 3.459,
            37.775, 13.826, 8.340, 2.271,
            0.725, 0.319, 0.088, 0.044,
            0.017, 0.001, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000
        };

    static final private double m_CksX = 99.188;
    static final private double m_CksY = 99.999;
    static final private double m_CksZ = 67.394;

    static final private double m_WhiteX = 99.186;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 67.393;

    public F2_2_20()
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
