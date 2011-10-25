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

public class D65_10 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.001, 0.005,
            0.097, 0.616, 1.660, 2.377,
            3.512, 3.789, 3.103, 1.937,
            0.747, 0.110, 0.007, 0.314,
            1.027, 2.174, 3.380, 4.735,
            6.081, 7.310, 8.393, 8.603,
            8.771, 7.996, 6.476, 4.635,
            3.074, 1.814, 1.031, 0.557,
            0.261, 0.114, 0.057, 0.028,
            0.011, 0.006, 0.003, 0.001,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,
            0.010, 0.064, 0.171, 0.283,
            0.549, 0.888, 1.277, 1.817,
            2.545, 3.164, 4.309, 5.631,
            6.896, 8.136, 8.684, 8.903,
            8.614, 7.950, 7.164, 5.945,
            5.110, 4.067, 2.990, 2.020,
            1.275, 0.724, 0.407, 0.218,
            0.102, 0.044, 0.022, 0.011,
            0.004, 0.002, 0.001, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, -0.001, 0.004, 0.020,
            0.436, 2.808, 7.868, 11.703, 17.958,
            20.358, 17.861, 13.085, 7.510,
            3.743, 2.003, 1.004, 0.529, 0.271,
            0.116, 0.030, -0.003, 0.001, 0.000,
            0.000, 0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 94.813;
    static final private double m_CksY = 99.997;
    static final private double m_CksZ = 107.304;

    static final private double m_WhiteX = 94.811;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 107.304;

    public D65_10()
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
