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

public class D75_10_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.003, 0.071, 3.555, 8.252, 7.268, 1.358, -0.266, 2.006, 6.791, 12.060, 16.311, 17.015, 12.327, 5.403, 1.733, 0.421, 0.080, 0.021, 0.005, 0.001, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, -0.002, 0.015, 0.339, 1.195, 2.815, 5.270, 8.912, 14.363, 17.776, 17.154, 13.796, 9.671, 5.601, 2.212, 0.676, 0.165, 0.031, 0.008, 0.002, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.029, 0.252, 16.605, 42.050, 41.829, 15.257, 3.401, 1.045, 0.182, -0.013, 0.004, -0.001, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 94.415;
    static final private double m_CksY = 99.999;
    static final private double m_CksZ = 120.640;

    static final private double m_WhiteX = 94.413;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 120.641;

    public D75_10_20()
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
