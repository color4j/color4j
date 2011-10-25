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

package org.color4j.colorimetry.matching;

public class ColorDifferenceCJ94 extends ColorDifference
{
    /**
     * the variable end with star mean is using CIELab model,just the difference of
     * the the value, without star means is more complex, ex:
     * deltaL means deltaLstar/sL, sL : weight adjust for L
     * deltaLStar means L_batch - L_target
     */
    private double m_dLStar;
    private double m_dAStar;
    private double m_dBStar;
    private double m_dCStar;
    private double m_dHStar;
    private double m_dE;
    private double m_sL;
    private double m_sC;
    private double m_sH;
    private double m_dR;

    //BCJ : B:BFD, C:CMC, J:JPC
    public ColorDifferenceCJ94( DifferenceAlgorithm algorithm,
                                double deltaLs, double deltaCs, double deltaHs,
                                double sL, double sC, double sH, double deltaR,
                                double deltaE, double deltaAs, double deltaBs
    )
    {
        super( algorithm );
        m_dLStar = deltaLs;
        m_dCStar = deltaCs;
        m_dHStar = deltaHs;
        m_sL = sL;
        m_sC = sC;
        m_sH = sH;
        m_dR = deltaR;
        m_dE = deltaE;
        m_dAStar = deltaAs;
        m_dBStar = deltaBs;
    }

    public double getDeltaE()
    {
        return m_dE;
    }

    public double getDeltaL()
    {
        return m_dLStar / m_sL;
    }

    public double getDeltaC()
    {
        return m_dCStar / m_sC;
    }

    public double getDeltaH()
    {
        return m_dHStar / m_sH;
    }

    /**
     * Returns the Delta a of the two CIELab values.
     */
    public double getDeltaaStar()
    {
        return m_dAStar;
    }

    /**
     * Returns the Delta b of the two CIElab values.
     */
    public double getDeltabStar()
    {
        return m_dBStar;
    }

    /**
     * Returns the Delta a of the two CIELab values.
     */
    public double getDeltaLStar()
    {
        return m_dLStar;
    }

    public double getDeltaCStar()
    {
        return m_dCStar;
    }

    public double getDeltaHStar()
    {
        return m_dHStar;
    }

    public double getDeltaR()
    {
        return m_dR;
    }

    public double getSL()
    {
        return m_sL;
    }

    public double getSC()
    {
        return m_sC;
    }

    public double getSH()
    {
        return m_sH;
    }
}
