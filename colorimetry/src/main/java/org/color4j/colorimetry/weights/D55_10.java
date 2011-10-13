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

public class D55_10 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.001, 0.003,
            0.073, 0.466, 1.291, 1.870,
            2.910, 3.224, 2.686, 1.710,
            0.675, 0.101, 0.007, 0.296,
            0.989, 2.134, 3.345, 4.751,
            6.162, 7.468, 8.697, 8.966,
            9.336, 8.610, 7.061, 5.097,
            3.446, 2.039, 1.178, 0.647,
            0.305, 0.131, 0.067, 0.032,
            0.012, 0.007, 0.004, 0.001,
            0.001, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.000,
            0.008, 0.048, 0.133, 0.222,
            0.454, 0.755, 1.104, 1.599,
            2.289, 2.882, 4.021, 5.329,
            6.657, 7.993, 8.600, 8.939,
            8.732, 8.126, 7.426, 6.199,
            5.442, 4.380, 3.261, 2.222,
            1.430, 0.814, 0.465, 0.253,
            0.119, 0.051, 0.026, 0.012,
            0.005, 0.003, 0.001, 0.001,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.000, 0.003, 0.012,
            0.326, 2.122, 6.120, 9.203,
            14.875, 17.323, 15.458, 11.543,
            6.773, 3.418, 1.876, 0.952,
            0.513, 0.267, 0.115, 0.031,
            -0.003, 0.001, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 95.799;
    static final private double m_CksY = 100.001;
    static final private double m_CksZ = 90.928;

    static final private double m_WhiteX = 95.799;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 90.926;

    public D55_10()
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
