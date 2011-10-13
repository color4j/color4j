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

public class F11_10 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.000, 0.001, -0.019,
            0.102, 0.196, -0.134, 2.908,
            4.426, 1.339, 1.348, 0.657,
            0.329, 0.176, -0.006, 0.039,
            0.015, -1.070, 11.643, 13.374,
            0.159, 0.674, 7.362, 9.374,
            4.309, 29.011, 12.930, 3.263,
            0.571, 0.473, 0.261, 0.073,
            0.036, 0.023, 0.010, 0.015,
            0.001, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WY =
        {
            0.000, 0.000, 0.000, -0.001,
            0.011, 0.021, -0.028, 0.381,
            0.666, 0.355, 0.566, 0.513,
            1.316, 3.206, 1.464, 0.510,
            0.238, -1.951, 27.854, 26.520,
            -0.660, 0.822, 6.226, 6.653,
            2.597, 14.710, 6.080, 1.353,
            0.232, 0.187, 0.103, 0.029,
            0.014, 0.009, 0.004, 0.006,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.000, 0.004, -0.088,
            0.460, 0.897, -0.756, 14.502,
            22.492, 7.327, 7.783, 4.313,
            3.539, 4.053, 0.581, 0.020,
            0.024, 0.005, 0.327, 0.149,
            -0.023, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000, 0.000,
            0.000, 0.000, 0.000
        };

    static final private double m_CksX = 103.869;
    static final private double m_CksY = 100.006;
    static final private double m_CksZ = 65.609;

    static final private double m_WhiteX = 103.863;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 65.607;

    public F11_10()
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

