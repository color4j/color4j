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

public class F2_10_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, -0.038, 0.234, 1.022,
            7.898, 2.301, 0.686, -0.133,
            0.444, 6.953, 14.911, 28.878,
            24.810, 11.708, 3.014, 0.516,
            0.073, 0.010, 0.001, 0.000,
            0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, -0.005, 0.028, 0.100,
            1.121, 1.042, 2.475, 4.279,
            5.769, 17.713, 22.281, 24.639,
            13.883, 5.211, 1.241, 0.197,
            0.030, 0.004, 0.001, 0.000,
            0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, -0.171, 1.066, 4.782,
            39.933, 13.716, 7.408, 1.613,
            0.511, 0.191, -0.001, 0.002,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000
        };

    static final private double m_CksX = 103.288;
    static final private double m_CksY = 100.009;
    static final private double m_CksZ = 69.050;

    static final private double m_WhiteX = 103.279;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 69.027;

    public F2_10_20()
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
