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

import org.color4j.colorimetry.Weights;
import org.color4j.colorimetry.encodings.XYZ;

public abstract class AbstractWeights implements Weights
{
    private double[] m_WX;

    public AbstractWeights()
    {
        m_WX = getWeightsX();
    }

    public int getInterval()
    {
        if( m_WX.length >= 43 )
        {
            return 10;
        }
        else
        {
            return 20;
        }
    }

    public int getShortestWavelength()
    {
        return 360;
    }

    public int getLongestWavelength()
    {
        return 780;
    }

    public abstract XYZ getChecksum();

    abstract public XYZ getWhitePoint();
}
