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

import org.color4j.colorimetry.encodings.Din99Lab;
import org.color4j.colorimetry.math.Maths;

public class Din99
    implements DifferenceAlgorithm<Din99Lab>
{
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
        System.arraycopy( s, 0, st, 0, s.length - 3 );
        return st;
    }

    public ColorDifference compute( Din99Lab target, Din99Lab sample )
    {
        double[] p = computeAll( target, sample );
        ColorDifference cd = new ColorDifferenceDin99( this, p[ 0 ], p[ 1 ], p[ 2 ], p[ 3 ], p[ 4 ], p[ 5 ],
                                                       p[ 6 ], p[ 7 ], p[ 8 ], p[ 9 ], p[ 10 ] );

        return cd;
    }

    private double[] computeAll( Din99Lab standard, Din99Lab batch )
    {
        double[] p = new double[ 11 ];

        double deltacStar = batch.getc() - standard.getc();
        double deltahStar = Maths.computeDifferenceHue( standard.geth(), standard.getc(), batch.geth(), batch.getc() );

        double deltaL99 = batch.getL() - standard.getL();
        double deltaa99 = batch.geta() - standard.geta();
        double deltab99 = batch.getb() - standard.getb();

        double k1 = Math.pow( deltaL99, 2.0 );
        double k2 = Math.pow( deltaa99, 2.0 );
        double k3 = Math.pow( deltab99, 2.0 );
        p[ 0 ] = deltaL99;
        p[ 1 ] = deltaa99;
        p[ 2 ] = deltab99;
        p[ 3 ] = batch.getc() - standard.getc();
        p[ 4 ] = Maths.computeDifferenceHue( standard.geth(), standard.getc(), batch.geth(), batch.getc() );

        p[ 5 ] = Math.pow( ( k1 + k2 + k3 ), .5 );  //deltaE
        p[ 6 ] = batch.getL() - standard.getL(); //delta L star
        p[ 7 ] = batch.geta() - standard.geta(); //delta a star
        p[ 8 ] = batch.getb() - standard.getb();//delta b star
        p[ 9 ] = deltacStar;
        p[ 10 ] = deltahStar;

        return p;
    }
}
