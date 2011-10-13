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

public class D55_2_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.027, -0.016, 1.578, 5.983, 5.881, 1.663, -0.202, 0.950, 5.611, 11.328, 16.931, 19.527, 15.581, 7.384, 2.600, 0.669, 0.137, 0.040, 0.008, 0.001, 0.001
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.004, 0.029, 0.334, 1.094, 2.481, 5.771, 13.833, 19.214, 19.197, 16.001, 11.196, 6.759, 2.823, 0.956, 0.242, 0.049, 0.014, 0.003, 0.001, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.127, -0.072, 7.506, 29.586, 33.691, 15.012, 4.413, 1.471, 0.314, 0.057, 0.028, 0.013, 0.002, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 95.682;
    static final private double m_CksY = 100.001;
    static final private double m_CksZ = 92.148;

    static final private double m_WhiteX = 95.682;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 92.149;

    public D55_2_20()
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
