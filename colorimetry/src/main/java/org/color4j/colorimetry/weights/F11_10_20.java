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

public class F11_10_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, -0.029, 0.181, 0.414,
            8.515, 1.544, 0.319, 1.673,
            -5.992, 24.601, 4.494, 10.526,
            24.099, 36.033, -4.279, 2.026,
            -0.397, 0.155, -0.025, 0.006,
            -0.001, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, -0.005, 0.026, 0.019,
            1.220, 0.977, 1.693, 8.341,
            -13.547, 55.948, 7.060, 8.885,
            13.702, 17.112, -2.247, 0.952,
            -0.198, 0.072, -0.013, 0.003,
            -0.001, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, -0.142, 0.869, 1.729,
            43.348, 9.002, 7.470, 3.484,
            -0.739, 0.625, -0.051, 0.014,
            -0.004, 0.001, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000
        };

    static final private double m_CksX = 103.869;
    static final private double m_CksY = 100.006;
    static final private double m_CksZ = 65.609;

    static final private double m_WhiteX = 103.863;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 65.607;

    public F11_10_20()
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
