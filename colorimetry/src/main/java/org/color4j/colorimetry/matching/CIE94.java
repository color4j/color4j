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

public class CIE94
    implements DifferenceAlgorithm
{
    public CIE94()
    {
    }

    public String[] getAllDeltaNames()
    {
        ColorDifference cd = new ColorDifferenceCJ94( this, 0.0, 0.0, 0.0,
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

    public String getName()
    {
        return MatchingFactory.CIE94DE;
    }

    public ColorDifference compute( ColorEncoding target, ColorEncoding sample )
    {
        if( !( target instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "CIE94 only accepts ANLab color encodings." );  //NOI18N
        }

        if( !( sample instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "CIE94 only accepts ANLab color encodings." );  //NOI18N
        }

        double[] p = computeAll( target, sample );
        ColorDifference cdiff = new ColorDifferenceCJ94( this, p[ 0 ], p[ 1 ], p[ 2 ], p[ 3 ],
                                                         p[ 4 ], p[ 5 ], p[ 6 ], p[ 7 ],
                                                         p[ 8 ], p[ 9 ] );
        return cdiff;
    }

    private double[] computeAll( ColorEncoding standard, ColorEncoding batch )
    {
        double p[] = new double[ 10 ];
//        double dp=Math.PI/180.0;
        double CT, HT;
//        double  L1,a1,b1,c1,h1,L2,a2,b2,c2,h2;
        double dlch[] = new double[ 3 ];
        CIELab tlab = (CIELab) standard;
        CIELab blab = (CIELab) batch;

        dlch[ 0 ] = blab.getL() - tlab.getL();
        dlch[ 1 ] = blab.getc() - tlab.getc();
        dlch[ 2 ] = Util.computeDifferenceHue( tlab.geth(), tlab.getc(), blab.geth(), blab.getc() );

        CT = 1.0 + 0.045 * tlab.getc();
        HT = 1.0 + 0.015 * tlab.getc();
        p[ 0 ] = dlch[ 0 ];                 //deltaL star
        p[ 1 ] = dlch[ 1 ];                 //deltaC star
        p[ 2 ] = dlch[ 2 ];                 //deltaH star
        p[ 3 ] = 1.0;                     //L weight, LT no use
        p[ 4 ] = CT;                      //C weight
        p[ 5 ] = HT;                      //H weight
        p[ 6 ] = 0.0;                     //delta R
        double k1 = Math.pow( p[ 0 ] / p[ 3 ], 2.0 );
        double k2 = Math.pow( p[ 1 ] / p[ 4 ], 2.0 );
        double k3 = Math.pow( p[ 2 ] / p[ 5 ], 2.0 );

        p[ 7 ] = Math.pow( k1 + k2 + k3, .5 );    //deltaEE
        p[ 8 ] = blab.geta() - tlab.geta();
        p[ 9 ] = blab.getb() - tlab.getb();
        return p;
    }
}
