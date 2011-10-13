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

public class ColorDifferenceDin99 extends ColorDifference implements java.io.Serializable
{
    static final long serialVersionUID = 2L;

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
    private double m_dL99;
    private double m_da99;
    private double m_db99;
    private double m_dC99;
    private double m_dH99;

    public ColorDifferenceDin99( DifferenceAlgorithm algorithm,
                                 double dL99,
                                 double da99,
                                 double db99,
                                 double dC99,
                                 double dH99,
                                 double dE,
                                 double dLs,
                                 double dAs,
                                 double dBs,
                                 double dCs,
                                 double dHs
    )
    {
        super( algorithm );
        m_dL99 = dL99;
        m_da99 = da99;
        m_db99 = db99;
        m_dC99 = dC99;
        m_dH99 = dH99;
        m_dE = dE;
        m_dLStar = dLs;
        m_dAStar = dAs;
        m_dBStar = dBs;
        m_dCStar = dCs;
        m_dHStar = dHs;
    }

    public double getDeltaE()
    {
        return m_dE;
    }

    public double getDeltaL99()
    {
        return m_dL99;
    }

    public double getDeltaa99()
    {
        return m_da99;
    }

    public double getDeltab99()
    {
        return m_db99;
    }

    public double getDeltaC99()
    {
        return m_dC99;
    }

    public double getDeltaH99()
    {
        return m_dH99;
    }

    public double getDeltaL()
    {
        return m_dL99;
    }

    public double getDeltaC()
    {
        return m_dC99;
    }

    public double getDeltaH()
    {
        return m_dH99;
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
}
