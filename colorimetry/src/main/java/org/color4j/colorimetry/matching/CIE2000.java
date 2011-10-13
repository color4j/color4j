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

import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.encodings.CIELab;

public class CIE2000
    implements DifferenceAlgorithm
{
    public CIE2000()
    {
    }

    public String getName()
    {
        return MatchingFactory.CIE2000DE; // For now this will be OK.
    }

    public String[] getAllDeltaNames()
    {
        ColorDifference cd = new ColorDifferenceB2000( this, 0.0, 0.0, 0.0,
                                                       0.0, 0.0, 0.0,
                                                       0.0, 0.0, 0.0,
                                                       0.0, 0.0, 0.0, 0.0 );

        String[] s = cd.getAllValueNames();
        String[] st = new String[ s.length - 3 ];
        System.arraycopy( s, 0, st, 0, s.length - 3 );
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
        return new ColorDifferenceB2000( this, p[ 0 ], p[ 1 ], p[ 2 ], p[ 3 ], p[ 4 ], p[ 5 ],
                                                       p[ 6 ], p[ 7 ], p[ 8 ], p[ 9 ], p[ 10 ], p[ 11 ], p[ 12 ] );
    }

    private double[] computeAll( ColorEncoding standard, ColorEncoding batch )
    {
        double[] p = new double[ 13 ];
        double SL, SC, SH, RT, RC, CTA;//CT,DL,DC,DH;
        double mean_lch22;
        double dp = 3.14159 / 180.0;
        double seven25 = Math.pow( 25.0, 7.0 );
        CIELab tlab = (CIELab) standard;

        CIELab blab = (CIELab) batch;

        p[ 11 ] = blab.getc() - tlab.getc();
        p[ 12 ] = Util.computeDifferenceHue( tlab.geth(), tlab.getc(), blab.geth(), blab.getc() );

        double mean_lch1 = ( tlab.getc() + blab.getc() ) / 2.0;
        double G = 0.5 * ( 1 - Math.pow( ( Math.pow( mean_lch1, 7.0 ) / ( Math.pow( mean_lch1, 7.0 ) + seven25 ) ), .5 ) );
        CIELab tlab2 = new CIELab( tlab.getL(), ( 1 + G ) * tlab.geta(), tlab.getb() );
        CIELab blab2 = new CIELab( blab.getL(), ( 1 + G ) * blab.geta(), blab.getb() );

        double[] dlch = {
            blab2.getL() - tlab2.getL(), blab2.getc() - tlab2.getc(),
            Util.computeDifferenceHue( tlab2.geth(), tlab2.getc(), blab2.geth(), blab2.getc() )
        };

        double mean_lch20 = ( tlab2.getL() + blab2.getL() ) / 2.0;

        double mean_lch21 = ( tlab2.getc() + blab2.getc() ) / 2.0;

        if( Math.abs( tlab2.geth() - blab2.geth() ) < 180.0 )
        {
            mean_lch22 = ( tlab2.geth() + blab2.geth() ) / 2.0;
        }
        else
        {
            mean_lch22 = ( tlab2.geth() + blab2.geth() - 360.0 ) / 2.0;
        }

        double T = 1 - 0.17 * Math.cos( ( mean_lch22 - 30.0 ) * dp ) + .24 * Math.cos( 2 * mean_lch22 * dp ) +
                   .32 * Math.cos( ( 3 * mean_lch22 + 6.0 ) * dp ) - .2 * Math.cos( ( 4.0 * mean_lch22 - 63.0 ) * dp );

        SL = 1 + ( 0.015 * Math.pow( mean_lch20 - 50.0, 2.0 ) / Math.pow( 20.0 + Math.pow( mean_lch20 - 50.0, 2.0 ), .5 ) );
        SC = 1 + .045 * mean_lch21;
        SH = 1 + .015 * mean_lch21 * T;
        CTA = 30.0 * Math.exp( -( Math.pow( ( mean_lch22 - 275.0 ) / 25.0, 2.0 ) ) );
        RC = Math.pow( Math.pow( mean_lch21, 7.0 ) / ( Math.pow( mean_lch21, 7.0 ) + seven25 ), .5 ) * 2.0;
        RT = -Math.sin( 2 * CTA * dp ) * RC;
        p[ 0 ] = dlch[ 0 ];        //delta L pram
        p[ 1 ] = dlch[ 1 ];        //delta C pram
        p[ 2 ] = dlch[ 2 ];        //delta H pram
        p[ 3 ] = SL;             //weight L
        p[ 4 ] = SC;             //weight C
        p[ 5 ] = SH;             //weight H
        p[ 6 ] = RT;             // total C,H intercept

        double k1 = Math.pow( p[ 0 ] / p[ 3 ], 2.0 );
        double k2 = Math.pow( p[ 1 ] / p[ 4 ], 2.0 );
        double k3 = Math.pow( p[ 2 ] / p[ 5 ], 2.0 );
        double k4 = p[ 6 ] * p[ 1 ] * p[ 2 ] / ( p[ 4 ] * p[ 5 ] );
        p[ 7 ] = Math.pow( k1 + k2 + k3 + k4, .5 );  //deltaE
        p[ 8 ] = blab.getL() - tlab.getL(); //delta L star
        p[ 9 ] = blab.geta() - tlab.geta(); //delta a star
        p[ 10 ] = blab.getb() - tlab.getb();//delta b star

        return p;
    }
}
