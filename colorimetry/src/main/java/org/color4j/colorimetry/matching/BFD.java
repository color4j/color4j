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

import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.encodings.CIELab;

public class BFD
    implements DifferenceAlgorithm
{
    public BFD()
    {
    }

    public String getName()
    {
        return MatchingFactory.BFD;// For now this will be OK.
    }

    public ColorDifference compute( ColorEncoding target, ColorEncoding sample )
    {
        if( !( target instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "BFD only accepts CIELab color encodings." );   //NOI18N
        }

        if( !( sample instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "BFD only accepts CIELab color encodings." );   //NOI18N
        }

        double[] p = computeAll( target, sample );
        ColorDifference cd = new ColorDifferenceB2000( this, p[ 0 ], p[ 1 ], p[ 2 ], p[ 3 ], p[ 4 ], p[ 5 ],
                                                       p[ 6 ], p[ 7 ], p[ 8 ], p[ 9 ], p[ 10 ], p[ 11 ], p[ 12 ] );

        return cd;
    }

    public String[] getAllDeltaNames()
    {
        ColorDifference cd = new ColorDifferenceB2000( this, 0.0, 0.0, 0.0,
                                                       0.0, 0.0, 0.0,
                                                       0.0, 0.0, 0.0,
                                                       0.0, 0.0, 0.0, 0.0 );

        String[] s = cd.getAllValueNames();
        String[] st = new String[ s.length - 3 ];
        for( int i = 0; i < s.length - 3; i++ )
        {
            st[ i ] = s[ i ];
        }

        return st;
    }

    private double[] computeAll( ColorEncoding standard, ColorEncoding batch )
    {
        double[] p = new double[ 13 ];
        double loge = .434294481;
//       double dp = 3.14159 / 180.0;
        double pi = 3.14159;
        double l1, l2, a1, a2, b1, b2;
        double c1, c2, h1, h2, lbfd1, lbfd2, avec, aveh;
        double de, deltal, deltac, deltah, dc, t, g, dh, rh;
        double rc, rt, yt;//deltae,yr,xt,zt;
        CIELab tlab = (CIELab) standard;
        CIELab blab = (CIELab) batch;
        l1 = tlab.getL();
        a1 = tlab.geta();
        b1 = tlab.getb();
        l2 = blab.getL();
        a2 = blab.geta();
        b2 = blab.getb();

        if( l1 > 7.996969 )
        {
            yt = ( Math.pow( ( l1 + 16.0 ) / 116.0, 2.0 ) * ( ( l1 + 16.0 ) / 116.0 ) ) * 100.0;
        }
        else
        {
            yt = 100.0 * ( l1 / 903.3 );
        }

        lbfd1 = 54.6 * ( loge * ( Math.log( yt + 1.5 ) ) ) - 9.6;
        if( l2 > 7.996969 )
        {
            yt = ( Math.pow( ( l2 + 16.0 ) / 116.0, 2.0 ) * ( ( l2 + 16.0 ) / 116.0 ) ) * 100.0;
        }
        else
        {
            yt = 100.0 * ( l2 / 903.3 );
        }

        lbfd2 = 54.6 * ( loge * ( Math.log( yt + 1.5 ) ) ) - 9.6;

        c1 = tlab.getc();
        c2 = blab.getc();
        deltal = lbfd2 - lbfd1;
        deltac = c2 - c1;

        de = Math.pow( l2 - l1, 2.0 );
        de = de + Math.pow( a2 - a1, 2.0 );
        de = de + Math.pow( b2 - b1, 2.0 );
        de = Math.pow( de, .5 );

        if( Math.pow( de, 2.0 ) > ( Math.pow( l2 - l1, 2.0 ) + Math.pow( deltac, 2.0 ) ) )
        {
            deltah = Math.pow( Math.pow( de, 2.0 ) - Math.pow( l2 - l1, 2.0 ) - Math.pow( deltac, 2.0 ), .5 );
        }
        else
        {
            deltah = 0;
        }

        avec = ( c1 + c2 ) / 2;
        h1 = tlab.geth();
        h2 = blab.geth();

        double h21 = h2 - h1;
        if( ( ( h21 < 0.0 ) && ( h21 > -180.0 ) ) || ( ( h21 > 180 ) && ( h21 < 360 ) ) )
        {
            deltah = -deltah;
        }

        aveh = ( h1 + h2 ) / 2;
        dc = 0.035 * avec / ( 1 + 0.00365 * avec ) + 0.521;
        g = Math.pow( Math.pow( avec, 4.0 ) / ( Math.pow( avec, 4.0 ) + 14000 ), .5 );
        t = 0.627 + ( 0.055 * Math.cos( ( aveh - 254 ) / ( 180 / pi ) ) -
                      0.040 * Math.cos( ( 2 * aveh - 136 ) / ( 180 / pi ) ) +
                      0.070 * Math.cos( ( 3 * aveh - 31 ) / ( 180 / pi ) ) +
                      0.049 * Math.cos( ( 4 * aveh + 114 ) / ( 180 / pi ) ) -
                      0.015 * Math.cos( ( 5 * aveh - 103 ) / ( 180 / pi ) ) );
        dh = dc * ( g * t + 1 - g );
        rh = -0.260 * Math.cos( ( aveh - 308 ) / ( 180 / pi ) ) -
             0.379 * Math.cos( ( 2 * aveh - 160 ) / ( 180 / pi ) ) -
             0.636 * Math.cos( ( 3 * aveh + 254 ) / ( 180 / pi ) ) +
             0.226 * Math.cos( ( 4 * aveh + 140 ) / ( 180 / pi ) ) -
             0.194 * Math.cos( ( 5 * aveh + 280 ) / ( 180 / pi ) );

        rc = Math.pow( ( Math.pow( avec, 6.0 ) ) / ( ( Math.pow( avec, 6.0 ) ) + 70000000 ), .5 );
        rt = rh * rc;
//        double fdc = deltac/dc;
//        double fdh = deltah/dh;
//        double w   = rt*(deltac/dc)*(deltah/dh);
        p[ 0 ] = deltal;        //deltaL dot
        p[ 1 ] = deltac;        //deltaC dot
        p[ 2 ] = deltah;        //deltaH dot
        p[ 3 ] = 1.0;           //L weight
        p[ 4 ] = dc;            //C weight
        p[ 5 ] = dh;            //H weight
        p[ 6 ] = rt * p[ 1 ] * p[ 2 ] / ( p[ 4 ] * p[ 5 ] );            //deltaR

        double k1 = Math.pow( p[ 0 ] / p[ 3 ], 2.0 );
        double k2 = Math.pow( p[ 1 ] / p[ 4 ], 2.0 );
        double k3 = Math.pow( p[ 2 ] / p[ 5 ], 2.0 );
        double k4 = p[ 6 ];
        p[ 7 ] = Math.pow( k1 + k2 + k3 + k4, .5 );            //deltaE
        p[ 8 ] = l2 - l1;                             //delta L star
        p[ 9 ] = a2 - a1;                             //delta a star
        p[ 10 ] = b2 - b1;//delta b star
        p[ 11 ] = c2 - c1;//delta c star
        p[ 12 ] = Util.computeDifferenceHue( h1, c1, h2, c2 );

        return p;
    }
}
