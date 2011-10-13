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

public class D75_2_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.050, -0.030, 2.571, 8.429, 7.578, 1.982, -0.231, 1.042, 5.798, 11.210, 16.196, 17.836, 13.604, 6.169, 2.102, 0.518, 0.109, 0.031, 0.007, 0.001, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.005, 0.051, 0.475, 1.434, 3.045, 6.706, 14.911, 19.708, 18.953, 15.245, 10.201, 5.892, 2.358, 0.773, 0.188, 0.039, 0.011, 0.002, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.235, -0.142, 12.243, 41.731, 43.498, 18.114, 4.973, 1.575, 0.314, 0.057, 0.026, 0.012, 0.002, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 94.972;
    static final private double m_CksY = 99.997;
    static final private double m_CksZ = 122.638;

    static final private double m_WhiteX = 94.972;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 122.638;

    public D75_2_20()
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
