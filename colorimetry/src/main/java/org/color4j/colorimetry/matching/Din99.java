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

import org.color4j.colorimetry.ColorCalculator;
import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.encodings.CIELab;

public class Din99
    implements DifferenceAlgorithm
{
    private double m_Ke, m_Kch;

    public Din99( double Ke, double Kch )
    {
        m_Ke = Ke;
        m_Kch = Kch;
    }

    public String getName()
    {
        return MatchingFactory.DIN99; // For now this will be OK.
    }

    public String[] getAllDeltaNames()
    {
        ColorDifference cd = new ColorDifferenceDin99( this, 0.0, 0.0, 0.0,
                                                       0.0, 0.0, 0.0,
                                                       0.0, 0.0, 0.0,
                                                       0.0, 0.0 );

        String[] s = cd.getAllValueNames();
        String[] st = new String[ s.length - 3 ];
        for( int i = 0; i < s.length - 3; i++ )
        {
            st[ i ] = s[ i ];
        }

        return st;
    }

    public ColorDifference compute( ColorEncoding target, ColorEncoding sample )
    {

        if( !( target instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "CIE2000 only accepts CIELab color encodings." );   //NOI18N
        }

        if( !( sample instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "CIE2000 only accepts CIELab color encodings." );   //NOI18N
        }

        double[] p = computeAll( target, sample );
        ColorDifference cd = new ColorDifferenceDin99( this, p[ 0 ], p[ 1 ], p[ 2 ], p[ 3 ], p[ 4 ], p[ 5 ],
                                                       p[ 6 ], p[ 7 ], p[ 8 ], p[ 9 ], p[ 10 ] );

        return cd;
    }

    private double[] computeAll( ColorEncoding standard, ColorEncoding batch )
    {
        double[] p = new double[ 11 ];

        CIELab tlab = (CIELab) standard;
        CIELab blab = (CIELab) batch;

        double deltacStar = blab.getc() - tlab.getc();
        double deltahStar = Util.computeDifferenceHue( tlab.geth(), tlab.getc(), blab.geth(), blab.getc() );

        double[] tlab99 = ColorCalculator.computeDin99Lab( tlab.getColorValues(), m_Ke, m_Kch );
        double[] blab99 = ColorCalculator.computeDin99Lab( blab.getColorValues(), m_Ke, m_Kch );
        double[] tlch99 = ColorCalculator.computeDin99Lch( tlab.getColorValues(), m_Ke, m_Kch );
        double[] blch99 = ColorCalculator.computeDin99Lch( blab.getColorValues(), m_Ke, m_Kch );

        double deltaL99 = blab99[ 0 ] - tlab99[ 0 ];
        double deltaa99 = blab99[ 1 ] - tlab99[ 1 ];
        double deltab99 = blab99[ 2 ] - tlab99[ 2 ];

        double k1 = Math.pow( deltaL99, 2.0 );
        double k2 = Math.pow( deltaa99, 2.0 );
        double k3 = Math.pow( deltab99, 2.0 );
        p[ 0 ] = deltaL99;
        p[ 1 ] = deltaa99;
        p[ 2 ] = deltab99;
        p[ 3 ] = blch99[ 1 ] - tlch99[ 1 ];
        p[ 4 ] = Util.computeDifferenceHue( tlch99[ 2 ], tlch99[ 1 ], blch99[ 2 ], blch99[ 1 ] );

        p[ 5 ] = Math.pow( ( k1 + k2 + k3 ), .5 );  //deltaE
        p[ 6 ] = blab.getL() - tlab.getL(); //delta L star
        p[ 7 ] = blab.geta() - tlab.geta(); //delta a star
        p[ 8 ] = blab.getb() - tlab.getb();//delta b star
        p[ 9 ] = deltacStar;
        p[ 10 ] = deltahStar;

        return p;
    }
}
