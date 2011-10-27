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

package org.color4j.colorimetry.encodings;

import org.color4j.colorimetry.ColorEncoding;

/**
 * This class stores L, a and b value
 *
 * <p>CIELab allows the sepcification of color perceptions in terms of a three-dimensional space.
 * The L value represents the lightness and extends from 0 (black) to 100 (white).
 * a and b represent redness-greeness and yellowness-blueness respectively.</p>
 */
public class Din99Lab
    implements ColorEncoding
{
    private final double lStar;
    private final double aStar;
    private final double bStar;
    private final double cStar;
    private final double hStar;

    /**
     * by default, we will assume that the colors calculated are in-gamut
     */
    private final boolean m_InGamut = true;

    public Din99Lab( double L, double a, double b, double c, double h )
    {
        lStar = L;
        aStar = a;
        bStar = b;
        cStar = c;
        hStar = h;
    }

    public double getL()
    {
        return lStar;
    }

    public double geta()
    {
        return aStar;
    }

    public double getb()
    {
        return bStar;
    }

    public double getc()
    {
        return cStar;
    }

    public double geth()
    {
        return hStar;
    }

    public boolean isInGamut()
    {
        return m_InGamut;
    }
}