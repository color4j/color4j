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

package org.color4j.colorimetry.matching;

import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.encodings.CIELab;

public class ColorRange
{
    static final long serialVersionUID = 1L;

    private DifferenceAlgorithm m_Algorithm;
    private CIELab m_Target;
    private double m_DeltaE;

    public ColorRange( CIELab color, double deltaE )
    {
        m_Target = color;
        m_DeltaE = deltaE;
        m_Algorithm = MatchingFactory.getInstance().getDefaultAlgorithm();
    }

    public ColorRange( CIELab color, double deltaE, DifferenceAlgorithm algorithm )
    {
        m_Target = color;
        m_DeltaE = deltaE;
        m_Algorithm = algorithm;
    }

    public ColorRange( CIELab color,
                       double deltaE,
                       String algorithmName
    )
    {
        m_Target = color;
        m_DeltaE = deltaE;
        m_Algorithm = MatchingFactory.getInstance().getAlgorithm( algorithmName );
    }

    public DifferenceAlgorithm getAlgorithm()
    {
        return m_Algorithm;
    }

    public double getDeltaE()
    {
        return m_DeltaE;
    }

    public CIELab getTarget()
    {
        return m_Target;
    }

    public boolean isWithinRange( CIELab batch )
    {
        double delta = 0;
        try
        {
            ColorDifference diff = m_Algorithm.compute( m_Target, batch );
            delta = diff.getValue( ColorDifference.DELTA_E );
        }
        catch( ColorException e )
        {
        } // Can not happen.

        return m_DeltaE >= delta;
    }

    public String toString()
    {
        return "ColorRange(" + m_Target + ", " + m_DeltaE + ", " + m_Algorithm + " )";  //NOI18N
    }

    /**
     * Returns the delta for a given hueAngle of the target/standard.
     * <p>The hueAngle is the h in Lch, and the returned value is the
     * distance from the target in the hue angle direction, for which the
     * deltaE is within limits.</p>
     * <p>The returned value is expressed in chroma units, i.e. CIELab color
     * space units.
     */
    public double[] computeEllipse()
    {
        //return m_Algorithm.computeEllipse( m_Target, (float) m_DeltaE, hueAngle );
        return new double[ 1 ];
    }
}

