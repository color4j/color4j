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

public class D50_10_20 extends AbstractWeights
{
    static final private double[] m_WX =
        {
            0.000, 0.001, 0.035, 1.856, 5.234, 5.206, 1.104, -0.238, 1.816, 6.614, 12.430, 17.595, 19.678, 15.166, 7.075, 2.387, 0.612, 0.111, 0.030, 0.006, 0.001, 0.001
        };

    static final private double[] m_WY =
        {
            0.000, -0.001, 0.009, 0.174, 0.748, 1.975, 4.046, 7.459, 13.203, 17.441, 17.746, 14.952, 11.219, 6.902, 2.898, 0.931, 0.240, 0.043, 0.012, 0.002, 0.000, 0.000
        };

    static final private double[] m_WZ =
        {
            0.000, 0.010, 0.131, 8.631, 26.634, 29.874, 12.054, 2.948, 0.969, 0.186, -0.014, 0.004, -0.001, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000
        };

    static final private double m_CksX = 96.720;
    static final private double m_CksY = 99.999;
    static final private double m_CksZ = 81.426;

    static final private double m_WhiteX = 96.720;
    static final private double m_WhiteY = 100.00;
    static final private double m_WhiteZ = 81.427;

    public D50_10_20()
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
