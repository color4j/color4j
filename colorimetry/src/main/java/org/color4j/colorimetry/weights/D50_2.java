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

public class D50_2 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.001, 0.003, 0.008,
            0.058, 0.191, 0.751, 1.592,
            2.519, 2.824, 2.556, 1.717,
            0.832, 0.250, 0.025, 0.047,
            0.538, 1.590, 2.770, 4.210,
            5.662, 7.092, 8.681, 9.175,
            9.966, 9.556, 8.099, 5.835,
            4.199, 2.539, 1.517, 0.831,
            0.423, 0.178, 0.096, 0.049,
            0.020, 0.012, 0.006, 0.002,
            0.001, 0.001, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,
            0.002, 0.005, 0.021, 0.060,
            0.158, 0.310, 0.511, 0.776,
            1.246, 1.783, 2.892, 4.610,
            6.586, 8.435, 9.185, 9.733,
            9.503, 8.882, 8.225, 6.728,
            5.884, 4.752, 3.584, 2.392,
            1.633, 0.954, 0.560, 0.304,
            0.153, 0.064, 0.035, 0.018,
            0.007, 0.004, 0.002, 0.001,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.005, 0.014, 0.039,
            0.277, 0.906, 3.603, 7.747,
            12.593, 14.834, 14.659, 11.344,
            7.240, 3.934, 2.447, 1.432,
            0.688, 0.403, 0.186, 0.080,
            0.035, 0.019, 0.016, 0.010,
            0.008, 0.003, 0.002, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 96.422;
    static final private double m_CksY = 99.998;
    static final private double m_CksZ = 82.524;

    static final private double m_WhiteX = 96.422;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 82.521;

    public D50_2()
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
