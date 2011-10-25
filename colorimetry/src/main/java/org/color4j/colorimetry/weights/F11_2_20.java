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

public class F11_2_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, -0.014, 0.100, 0.256,
            8.207, 1.559, 0.600, 1.524,
            -5.091, 20.536, 3.973, 9.894,
            24.253, 37.637, -4.377, 2.164,
            -0.411, 0.172, -0.025, 0.006,
            -0.001, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, -0.001, 0.005, -0.001,
            0.419, 0.623, 0.507, 7.107,
            -14.004, 58.821, 7.524, 9.370,
            13.848, 17.208, -2.270, 0.978,
            -0.200, 0.075, -0.012, 0.003,
            -0.001, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, -0.076, 0.509, 1.093,
            40.877, 9.228, 8.258, 4.371,
            -0.965, 1.039, -0.034, 0.032,
            0.011, 0.009, -0.002, 0.001,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000
        };

    static final private double m_CksX = 100.962;
    static final private double m_CksY = 99.999;
    static final private double m_CksZ = 64.351;

    static final private double m_WhiteX = 100.962;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 64.350;

    public F11_2_20()
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
