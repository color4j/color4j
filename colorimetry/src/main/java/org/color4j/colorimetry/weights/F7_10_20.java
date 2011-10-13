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

public class F7_10_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, -0.036, 0.246, 1.824,
            10.807, 4.506, 1.222, -0.261,
            1.147, 9.029, 11.459, 19.208,
            17.412, 12.049, 5.311, 1.641,
            0.169, 0.055, 0.001, 0.002,
            0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, -0.005, 0.031, 0.177,
            1.533, 1.899, 4.373, 7.596,
            11.062, 21.938, 16.827, 16.389,
            9.821, 5.451, 2.182, 0.638,
            0.067, 0.021, 0.000, 0.001,
            0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, -0.161, 1.106, 8.525,
            54.683, 26.455, 13.104, 2.884,
            0.890, 0.199, 0.000, 0.002,
            -0.001, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000
        };

    static final private double m_CksX = 95.791;
    static final private double m_CksY = 100.001;
    static final private double m_CksZ = 107.686;

    static final private double m_WhiteX = 95.792;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 107.686;

    public F7_10_20()
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
