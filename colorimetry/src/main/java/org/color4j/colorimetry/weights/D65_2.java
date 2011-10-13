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

public class D65_2 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.002, 0.006, 0.016,
            0.097, 0.311, 1.164, 2.400,
            3.506, 3.755, 3.298, 2.141,
            1.001, 0.293, 0.028, 0.054,
            0.581, 1.668, 2.860, 4.257,
            5.632, 6.960, 8.344, 8.676,
            9.120, 8.568, 7.119, 5.049,
            3.522, 2.112, 1.229, 0.658,
            0.331, 0.142, 0.074, 0.039,
            0.016, 0.009, 0.005, 0.002,
            0.001, 0.001, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,
            0.003, 0.009, 0.033, 0.092,
            0.221, 0.413, 0.662, 0.973,
            1.509, 2.107, 3.288, 5.122,
            7.082, 8.833, 9.472, 9.830,
            9.446, 8.709, 7.901, 6.357,
            5.379, 4.259, 3.149, 2.070,
            1.370, 0.794, 0.454, 0.240,
            0.120, 0.051, 0.027, 0.014,
            0.006, 0.003, 0.002, 0.001,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.009, 0.029, 0.077,
            0.460, 1.477, 5.581, 11.684, 17.532,
            19.729, 18.921, 14.161, 8.730,
            4.623, 2.769, 1.584, 0.736, 0.421,
            0.191, 0.081, 0.034, 0.018, 0.015,
            0.009, 0.007, 0.003, 0.001,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 95.047;
    static final private double m_CksY = 100.001;
    static final private double m_CksZ = 108.882;

    static final private double m_WhiteX = 95.047;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 108.883;

    public D65_2()
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
