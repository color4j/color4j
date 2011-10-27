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
import org.color4j.colorimetry.math.Maths;

public class CIELabDE
    implements DifferenceAlgorithm<CIELab>
{
    public CIELabDE()
    {
    }

    public String[] getAllDeltaNames()
    {
        ColorDifference cd = new ColorDifferenceCIELab( this, 0.0, 0.0, 0.0,
                                                        0.0, 0.0, 0.0 );

        String[] s = cd.getAllValueNames();
        String[] st = new String[ s.length - 3 ];
        System.arraycopy( s, 0, st, 0, s.length - 3 );
        return st;
    }

    public String getName()
    {
        return MatchingFactory.LABDE;
    }

    public ColorDifference compute( CIELab target, CIELab sample )
    {
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
        p[ 4 ] = Maths.computeDifferenceHue( tlab.geth(), tlab.getc(), blab.geth(), blab.getc() ); //h star

        double k1 = Math.pow( p[ 0 ], 2.0 );
        double k2 = Math.pow( p[ 1 ], 2.0 );
        double k3 = Math.pow( p[ 2 ], 2.0 );
        p[ 5 ] = Math.pow( k1 + k2 + k3, .5 );

        return p;
    }

    /** Returns the Delta Hue (h) between the two colors.
     **/
    /* 
    private double computeDelta_h( ColorEncoding standard, ColorEncoding batch )
    {
        // TODO:
        return 0.0;
    }
    */
}
