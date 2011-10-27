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

public class CMC
    implements DifferenceAlgorithm<CIELab>
{
    private double m_l;

    public CMC( double a )
    {
        m_l = a;
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

    public String getName()
    {
        if( m_l > 1.0 )
        {
            return MatchingFactory.CMC21;
        }

        return MatchingFactory.CMC11;
    }

    public ColorDifference compute( CIELab target, CIELab sample )
    {
        double[] p = computeAll( target, sample );
        ColorDifference cd = new ColorDifferenceCJ94( this, p[ 0 ], p[ 1 ], p[ 2 ],
                                                      p[ 3 ], p[ 4 ], p[ 5 ], p[ 6 ],
                                                      p[ 7 ], p[ 8 ], p[ 9 ] );

        return cd;
    }

    private double[] computeAll( ColorEncoding standard, ColorEncoding batch )
    {
        double[] p = new double[ 10 ];
        double dp = Math.PI / 180.0;
        double lt, ht, gt, t, ct;
        double dlch[] = new double[ 3 ];
        CIELab tlab = (CIELab) standard;
        CIELab blab = (CIELab) batch;

        double[] target_lch = { tlab.getL(), tlab.getc(), tlab.geth() };
        double[] predict_lch = { blab.getL(), blab.getc(), blab.geth() };

        //deltaL = batchL-StandardL
        dlch[ 0 ] = predict_lch[ 0 ] - target_lch[ 0 ];
        dlch[ 1 ] = predict_lch[ 1 ] - target_lch[ 1 ];
        dlch[ 2 ] = predict_lch[ 2 ] - target_lch[ 2 ];

        if( dlch[ 2 ] < -180.0 )
        {
            dlch[ 2 ] = dlch[ 2 ] + 360;
        }
        if( dlch[ 2 ] > 180 )
        {
            dlch[ 2 ] = dlch[ 2 ] - 360;
        }
        dlch[ 2 ] = 2.0 * Math.sqrt( target_lch[ 1 ] * predict_lch[ 1 ] ) * Math.sin( dlch[ 2 ] * dp / 2 );

        if( target_lch[ 0 ] > 16.0 )
        {
            lt = 0.040975 * target_lch[ 0 ] / ( 1 + 0.01765 * target_lch[ 0 ] );
        }
        else
        {
            lt = 0.511;
        }

        ct = 0.0638 * target_lch[ 1 ] / ( 1 + 0.0131 * target_lch[ 1 ] ) + 0.638;

        if( target_lch[ 2 ] > 164 && target_lch[ 2 ] < 345 )
        {
            ht = 0.56 + Math.abs( .2 * Math.cos( ( target_lch[ 2 ] + 168 ) * dp ) );
        }
        else
        {
            ht = 0.36 + Math.abs( 0.4 * Math.cos( ( target_lch[ 2 ] + 35 ) * dp ) );
        }

        gt = Math.sqrt( Math.pow( target_lch[ 1 ], 4.0 ) / ( Math.pow( target_lch[ 1 ], 4.0 ) + 1900 ) );

        t = ht * gt + 1 - gt;
        ht = t * ct;
        p[ 0 ] = dlch[ 0 ];          //deltaL Star
        p[ 1 ] = dlch[ 1 ];          //deltaC Star
        p[ 2 ] = dlch[ 2 ];          //deltaH Star
        p[ 3 ] = m_l * lt;               //L weight
        p[ 4 ] = ct;               //C weight
        p[ 5 ] = ht;               //H weight
        double k1 = Math.pow( p[ 0 ] / ( p[ 3 ] ), 2.0 );
        double k2 = Math.pow( p[ 1 ] / p[ 4 ], 2.0 );
        double k3 = Math.pow( p[ 2 ] / p[ 5 ], 2.0 );
        p[ 6 ] = 0.0;
        p[ 7 ] = Math.pow( k1 + k2 + k3, .5 );          //deltaE
        p[ 8 ] = blab.geta() - tlab.geta();      //deltaA Star
        p[ 9 ] = blab.getb() - tlab.getb();      //deltaB Star
        return p;
    }
}
