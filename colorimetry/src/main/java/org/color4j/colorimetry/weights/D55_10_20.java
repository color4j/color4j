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

public class D55_10_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.001, 0.044, 2.237, 5.965, 5.721, 1.170, -0.246, 1.870, 6.678, 12.373, 17.314, 18.909, 14.336, 6.563, 2.177, 0.551, 0.101, 0.027, 0.006, 0.001, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, -0.001, 0.010, 0.210, 0.856, 2.183, 4.359, 7.830, 13.538, 17.576, 17.649, 14.694, 10.768, 6.522, 2.688, 0.849, 0.216, 0.039, 0.011, 0.002, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.013, 0.165, 10.414, 30.366, 32.860, 12.878, 3.064, 0.992, 0.186, -0.013, 0.004, -0.001, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 95.798;
    static final private double m_CksY = 99.999;
    static final private double m_CksZ = 90.928;

    static final private double m_WhiteX = 95.799;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 90.926;

    public D55_10_20()
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
