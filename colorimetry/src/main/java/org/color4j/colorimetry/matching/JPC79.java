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

public class JPC79
    implements DifferenceAlgorithm
{
    public JPC79()
    {
    }

    public String getName()
    {
        return MatchingFactory.JPC79;
    }

    public String[] getAllDeltaNames()
    {
        ColorDifference cd = new ColorDifferenceCJ94( this, 0.0, 0.0, 0.0,
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
            throw new IllegalArgumentException( "JPC79 only accepts ANLab color encodings." );  //NOI18N
        }

        if( !( sample instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "JPC79 only accepts ANLab color encodings." );  //NOI18N
        }

        double[] p = computeAll( target, sample );
        return new ColorDifferenceCJ94( this, p[ 0 ], p[ 1 ], p[ 2 ], p[ 3 ],
                                        p[ 4 ], p[ 5 ], p[ 6 ], p[ 7 ],
                                        p[ 8 ], p[ 9 ] );
    }

    private double[] computeAll( ColorEncoding standard, ColorEncoding batch )
    {
        double[] p = new double[ 10 ];
        double sL, sC, sH, T;
        double dp = Math.PI / 180.0;
        CIELab tlab = (CIELab) standard;
        CIELab blab = (CIELab) batch;
        double[] dlch = new double[ 3 ];
        double[] target_lch = { tlab.getL(), tlab.getc(), tlab.geth() };
        double[] predict_lch = { blab.getL(), blab.getc(), blab.geth() };
        dlch[ 0 ] = predict_lch[ 0 ] - target_lch[ 0 ];
        dlch[ 1 ] = predict_lch[ 1 ] - target_lch[ 1 ];
        dlch[ 2 ] = predict_lch[ 2 ] - target_lch[ 2 ];

        if( dlch[ 2 ] < -180.0 )
        {
            dlch[ 2 ] = dlch[ 2 ] + 360.0;
        }
        if( dlch[ 2 ] > 180.0 )
        {
            dlch[ 2 ] = dlch[ 2 ] - 360.0;
        }
        dlch[ 2 ] = 2.0 * Math.sqrt( target_lch[ 1 ] * predict_lch[ 1 ] ) * Math.sin( dlch[ 2 ] * dp / 2 );
        sL = ( 0.08195 * target_lch[ 0 ] ) / ( 1.0 + 0.01765 * target_lch[ 0 ] );
        sC = ( ( 0.0638 * target_lch[ 1 ] ) / ( 1.0 + 0.0131 * target_lch[ 1 ] ) ) + 0.638;
        if( target_lch[ 1 ] < 0.638 )
        {
            T = 1.0;
        }
        else
        {
            if( ( target_lch[ 2 ] > 164.0 ) && ( target_lch[ 2 ] < 345.0 ) )
            {
                T = 0.56 + Math.abs( 0.2 * Math.cos( ( target_lch[ 2 ] + 168.0 ) * dp ) );
            }

            else
            {
                T = 0.36 + Math.abs( 0.4 * Math.cos( ( target_lch[ 2 ] + 35.0 ) * dp ) );
            }
        }
        sH = sC * T;
        p[ 0 ] = dlch[ 0 ];
        p[ 1 ] = dlch[ 1 ];
        p[ 2 ] = dlch[ 2 ];
        p[ 3 ] = sL;
        p[ 4 ] = sC;
        p[ 5 ] = sH;
        p[ 6 ] = 0.0;

        double k1 = Math.pow( p[ 0 ] / p[ 3 ], 2.0 );
        double k2 = Math.pow( p[ 1 ] / p[ 4 ], 2.0 );
        double k3 = Math.pow( p[ 2 ] / p[ 5 ], 2.0 );

        p[ 7 ] = Math.pow( k1 + k2 + k3, .5 );    //deltaE
        p[ 8 ] = blab.geta() - tlab.geta();
        p[ 9 ] = blab.getb() - tlab.getb();

        return p;
    }
}
