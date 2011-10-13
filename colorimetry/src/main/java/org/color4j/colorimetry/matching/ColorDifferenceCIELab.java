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

public class ColorDifferenceCIELab extends ColorDifference
    implements java.io.Serializable
{
    static final long serialVersionUID = 2L;

    private double m_DeltaL;
    private double m_DeltaA;
    private double m_DeltaB;
    private double m_DeltaChroma;
    private double m_DeltaHue;
    private double m_DeltaE;

    public ColorDifferenceCIELab( DifferenceAlgorithm algorithm,
                                  double deltaL, double deltaA, double deltaB,
                                  double deltaC, double deltaH, double deltaE
    )
    {
        super( algorithm );
        m_DeltaL = deltaL;
        m_DeltaA = deltaA;
        m_DeltaB = deltaB;
        m_DeltaE = deltaE;
        m_DeltaChroma = deltaC;
        m_DeltaHue = deltaH;
    }

    public double getDeltaE()
    {
        return m_DeltaE;
    }

    public double getDeltaLStar()
    {
        return m_DeltaL;
    }

    public double getDeltaCStar()
    {
        return m_DeltaChroma;
    }

    public double getDeltaHStar()
    {
        return m_DeltaHue;
    }

    public double getDeltaL()
    {
        return m_DeltaL;
    }

    public double getDeltaC()
    {
        return m_DeltaChroma;
    }

    public double getDeltaH()
    {
        return m_DeltaHue;
    }

    /**
     * Returns the Delta a of the two CIELab values.
     */
    public double getDeltaaStar()
    {
        return m_DeltaA;
    }

    /**
     * Returns the Delta b of the two CIElab values.
     */
    public double getDeltabStar()
    {
        return m_DeltaB;
    }
}
