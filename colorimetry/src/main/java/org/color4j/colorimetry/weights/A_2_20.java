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

public class A_2_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.013, -0.026, 0.483,
            1.955, 2.145, 0.848, -0.112,
            0.611, 4.407, 10.804, 19.601,
            26.256, 23.295, 12.853, 4.863,
            1.363, 0.359, 0.100, 0.023,
            0.006, 0.002
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, 0.009,
            0.106, 0.385, 1.119, 3.247,
            9.517, 15.434, 18.703, 18.746,
            15.233, 10.105, 4.939, 1.784,
            0.495, 0.129, 0.036, 0.008,
            0.002, 0.001
        };

    static final private double[] m_WZ =
        {
            0.000, 0.060, -0.123, 2.306,
            9.637, 12.257, 7.301, 2.727,
            1.035, 0.274, 0.055, 0.034,
            0.018, 0.003, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000
        };

    static final private double m_CksX = 109.849;
    static final private double m_CksY = 99.998;
    static final private double m_CksZ = 35.584;

    static final private double m_WhiteX = 109.850;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 35.585;

    public A_2_20()
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
