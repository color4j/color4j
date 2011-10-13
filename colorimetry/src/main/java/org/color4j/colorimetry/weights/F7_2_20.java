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

public class F7_2_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.00, -0.007, 0.121, 1.323,      //360-420
            10.790, 4.665, 1.708, -0.218,      //440-500
            0.379, 7.709, 10.453, 18.791,      //520-580
            17.996, 13.114, 5.97, 1.965,      //600-660
            0.204, 0.073, 0.003, 0.003,      //680-740
            0.0, 0.0
        };

    static final private double[] m_WY =
        {
            0.0, -0.001, 0.007, 0.028,      //360-420
            0.584, 0.963, 2.492, 5.611,      //440-500
            11.237, 23.952, 18.318, 17.848,      //520-580
            10.198, 5.65, 2.291, 0.72,      //600-660
            0.074, 0.026, 0.0010, 0.0010,      //680-740
            0.0, 0.0
        };

    static final private double[] m_WZ =
        {
            0.00, -0.033, 0.578, 6.323,      //360-420
            53.336, 27.365, 15.213, 4.189,      //440-500
            1.309, 0.351, 0.071, 0.03,      //520-580
            0.013, 0.0010, 0.0, 0.0,      //600-660
            0.0, 0.0, 0.0, 0.0,      //680-740
            0.0, 0.0
        };

    static final private double m_CksX = 95.042;
    static final private double m_CksY = 100.002;
    static final private double m_CksZ = 108.747;

    static final private double m_WhiteX = 95.041;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 108.747;

    public F7_2_20()
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