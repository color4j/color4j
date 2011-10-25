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

public class D65_10_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.003, 0.056, 2.951,
            7.227, 6.578, 1.278, -0.259, 1.951,
            6.751, 12.223, 16.779, 17.793, 13.135,
            5.859, 1.901, 0.469, 0.088, 0.023,
            0.005, 0.001, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, -0.001, 0.013, 0.280, 1.042, 2.534, 4.872, 8.438, 14.030, 17.715, 17.407, 14.210, 10.121, 5.971, 2.399, 0.741, 0.184, 0.034, 0.009, 0.002, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.025, 0.199, 13.768, 36.808, 37.827, 14.226, 3.254, 1.025, 0.184, -0.013, 0.004, -0.001, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 94.812;
    static final private double m_CksY = 100.001;
    static final private double m_CksZ = 107.306;

    static final private double m_WhiteX = 94.811;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 107.304;

    public D65_10_20()
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
