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

public class CIELchDE
    implements DifferenceAlgorithm
{
    public CIELchDE()
    {
    }

    public String[] getAllDeltaNames()
    {
        ColorDifference cd = new ColorDifferenceCIELab( this, 0.0, 0.0, 0.0,
                                                        0.0, 0.0, 0.0 );

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
        return MatchingFactory.LCHDE;
    }

    public ColorDifference compute( ColorEncoding target, ColorEncoding sample )
    {
        if( !( target instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "CIELchDE only accepts CIELab color encodings." );  //NOI18N
        }

        if( !( sample instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "CIELchDE only accepts CIELab color encodings." );  //NOI18N
        }

        double[] p = computeAll( target, sample );
        ColorDifference cd = new ColorDifferenceCIELab( this, p[ 0 ], p[ 1 ], p[ 2 ],
                                                        p[ 3 ], p[ 4 ], p[ 5 ] );

        return cd;
    }

    private double[] computeAll( ColorEncoding standard, ColorEncoding batch )
    {
        double[] p = new double[ 6 ];
        CIELab tlab = (CIELab) standard;
        CIELab blab = (CIELab) batch;
        p[ 0 ] = blab.getL() - tlab.getL();          //L star
        p[ 1 ] = blab.geta() - tlab.geta();          //a star
        p[ 2 ] = blab.getb() - tlab.getb();          //b star
        p[ 3 ] = blab.getc() - tlab.getc();          //c star
        p[ 4 ] = Util.computeDifferenceHue(          //h star
                                                     tlab.geth(), tlab.getc(),
                                                     blab.geth(), blab.getc() );

        double k1 = Math.pow( p[ 0 ], 2.0 );
        double k2 = Math.pow( p[ 3 ], 2.0 );
        double k3 = Math.pow( p[ 4 ], 2.0 );
        p[ 5 ] = Math.pow( k1 + k2 + k3, .5 );

        return p;
    }
}
