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

public class D50_2_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.021, -0.013, 1.297, 5.218, 5.326, 1.554, -0.191, 0.915, 5.528, 11.324, 17.119, 20.222, 16.400, 7.922, 2.835, 0.741, 0.150, 0.044, 0.009, 0.002, 0.001
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.003, 0.023, 0.290, 0.984, 2.291, 5.461, 13.421, 18.956, 19.226, 16.204, 11.611, 7.117, 3.030, 1.043, 0.268, 0.054, 0.016, 0.003, 0.001, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.1000, -0.060, 6.170, 25.788, 30.489, 13.965, 4.224, 1.430, 0.313, 0.057, 0.028, 0.014, 0.002, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 96.424;
    static final private double m_CksY = 100.002;
    static final private double m_CksZ = 82.520;

    static final private double m_WhiteX = 96.422;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 82.521;

    public D50_2_20()
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
