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
import org.color4j.colorimetry.math.Maths;

/**
 * This class stores L, a and b value
 *
 * <p>CIELab allows the specification of color perceptions in terms of a three-dimensional space.
 * The L value represents the lightness and extends from 0 (black) to 100 (white).
 * a and b represent redness-greeness and yellowness-blueness respectively.</p>
 */
public class HunterLab
    implements ColorEncoding
{
    private final double l;
    private final double a;
    private final double b;
    /**
     * by default, we will assume that the colors calculated are in-gamut
     */
    protected boolean m_InGamut = true;

    static public HunterLab convert( ColorEncoding ce, XYZ whitepoint )
        throws UnsupportedConversionException
    {
        if( ce instanceof XYZ )
        {
            return ((XYZ) ce).toHunterLab( whitepoint );
        }
        else
        {
            String message = "Unable to convert from " + ce.getClass().getName() + " to " + HunterLab.class.getName();
            throw new UnsupportedConversionException( message );   //NOI18N
        }
    }

    public HunterLab( double L, double a, double b )
    {
        this.l = L;
        this.a = a;
        this.b = b;
    }

    public double getL()
    {
        return this.l;
    }

    public double geta()
    {
        return this.a;
    }

    public double getb()
    {
        return this.b;
    }

    public double getc()
    {
        double d1 = Math.pow( this.a, 2.0 );
        double d2 = Math.pow( this.b, 2.0 );
        return Math.sqrt( d1 + d2 );
    }

    public double geth()
    {
        return Maths.atan( this.a, this.b );
    }

    public boolean isInGamut()
    {
        return m_InGamut;
    }
}
