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

public class C_10_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.043, -0.122, 3.216,
            8.476, 6.668, 1.430, -0.249,
            1.734, 6.364, 12.790, 17.338,
            17.597, 13.045, 6.283, 2.055,
            0.488, 0.100, 0.021, 0.004,
            0.001, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.002, -0.004, 0.301,
            1.239, 2.577, 5.320, 8.742,
            12.466, 16.891, 18.284, 14.617,
            10.019, 5.925, 2.581, 0.800,
            0.191, 0.039, 0.008, 0.002,
            0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.213, -0.622, 15.025,
            43.144, 38.431, 15.661, 3.219,
            0.897, 0.187, -0.014, 0.004,
            -0.001, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000
        };

    static final private double m_CksX = 97.282;
    static final private double m_CksY = 100.000;
    static final private double m_CksZ = 116.144;

    static final private double m_WhiteX = 97.285;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 116.145;

    public C_10_20()
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
