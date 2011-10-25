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

public class D50_10 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.001, 0.002,
            0.059, 0.385, 1.087, 1.598,
            2.556, 2.888, 2.437, 1.574,
            0.630, 0.096, 0.006, 0.284,
            0.965, 2.101, 3.317, 4.745,
            6.194, 7.547, 8.847, 9.218,
            9.712, 9.035, 7.465, 5.426,
            3.713, 2.208, 1.289, 0.714,
            0.338, 0.144, 0.075, 0.035,
            0.014, 0.008, 0.004, 0.002,
            0.001, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,
            0.006, 0.040, 0.112, 0.190,
            0.398, 0.675, 1.000, 1.469,
            2.130, 2.715, 3.842, 5.138,
            6.500, 7.872, 8.532, 8.931,
            8.780, 8.214, 7.557, 6.375,
            5.663, 4.597, 3.447, 2.366,
            1.541, 0.882, 0.509, 0.279,
            0.131, 0.056, 0.029, 0.014,
            0.005, 0.003, 0.002, 0.001,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.000, 0.002, 0.009,
            0.263, 1.751, 5.154, 7.864,
            13.066, 15.511, 14.023, 10.623,
            6.312, 3.227, 1.796, 0.919,
            0.501, 0.263, 0.114, 0.031,
            -0.003, 0.001, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 96.720;
    static final private double m_CksY = 100.001;
    static final private double m_CksZ = 81.427;

    static final private double m_WhiteX = 96.720;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 81.427;

    public D50_10()
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
