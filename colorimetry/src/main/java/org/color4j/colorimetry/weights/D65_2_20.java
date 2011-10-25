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

public class D65_2_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.040, -0.026, 2.114,
            7.323, 6.815, 1.843, -0.219, 1.003,
            5.723, 11.284, 16.548, 18.528, 14.397,
            6.646, 2.290, 0.574, 0.120, 0.034,
            0.007, 0.001, 0.001
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.004, 0.041, 0.411, 1.281, 2.797, 6.291, 14.463, 19.509, 19.106, 15.600, 10.607, 6.240, 2.540, 0.842, 0.208, 0.043, 0.012, 0.003, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.187, -0.120, 10.065, 36.235, 39.090, 16.753, 4.727, 1.532, 0.314, 0.058, 0.027, 0.013, 0.002, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 95.046;
    static final private double m_CksY = 99.998;
    static final private double m_CksZ = 108.883;

    static final private double m_WhiteX = 95.047;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 108.883;

    public D65_2_20()
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
