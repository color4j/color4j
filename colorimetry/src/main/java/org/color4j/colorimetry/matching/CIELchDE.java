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

import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.math.Maths;

public class CIELchDE
    implements DifferenceAlgorithm<CIELab>
{
    public String[] getAllDeltaNames()
    {
        ColorDifference cd = new ColorDifferenceCIELab( this, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 );
        String[] s = cd.getAllValueNames();
        String[] st = new String[ s.length - 3 ];
        System.arraycopy( s, 0, st, 0, s.length - 3 );
        return st;
    }

    public String getName()
    {
        return MatchingFactory.LCHDE;
    }

    public ColorDifference compute( CIELab target, CIELab sample )
    {
        double deltaL = sample.getL() - target.getL();          //L star
        double deltaA = sample.geta() - target.geta();          //a star
        double deltaB = sample.getb() - target.getb();          //b star
        double deltaC = sample.getc() - target.getc();          //c star
        double deltaH = Maths.computeDifferenceHue( target.geth(), target.getc(), sample.geth(), sample.getc() );  //h star

        double k1 = Math.pow( deltaL, 2.0 );
        double k2 = Math.pow( deltaC, 2.0 );
        double k3 = Math.pow( deltaH, 2.0 );
        double deltaE = Math.pow( k1 + k2 + k3, .5 );

        return new ColorDifferenceCIELab( this, deltaL, deltaA, deltaB, deltaC, deltaH, deltaE );
    }
}
