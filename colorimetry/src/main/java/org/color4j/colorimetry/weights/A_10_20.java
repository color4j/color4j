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

public class A_10_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.007, -0.016, 0.691,
            2.025, 2.158, 0.642, -0.160,
            1.284, 5.445, 12.238, 20.755,
            26.325, 22.187, 11.816, 4.221,
            1.154, 0.282, 0.068, 0.017,
            0.004, 0.001
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.066,
            0.285, 0.796, 2.043, 4.630,
            9.668, 14.621, 17.766, 17.800,
            15.129, 10.097, 4.858, 1.643,
            0.452, 0.109, 0.026, 0.007,
            0.002, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.037, -0.088, 3.226,
            10.278, 12.345, 6.555, 1.966,
            0.721, 0.171, -0.013, 0.004,
            -0.001, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000
        };

    static final private double m_CksX = 111.144;
    static final private double m_CksY = 99.998;
    static final private double m_CksZ = 35.201;

    static final private double m_WhiteX = 111.144;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 35.200;

    public A_10_20()
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
