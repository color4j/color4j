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

package org.color4j.indexing;

import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.observers.ObserverImpl;
import org.color4j.colorimetry.weights.WeightsCache;

public class SpaceCalculations
{
    private static double[] computeEllipse( double[] xy, double[] target, double deltaE, int degree )
    {
        // TODO:
        double dp = Math.PI / 180.0;

        double rad = degree * dp;
        double rot = target[ 2 ] * dp;
        //rot = Math.PI/2.0;
        //double[] xy = computeEllipseAB(target, deltaE);

        if( degree == 0 )
        {
            return rotate( xy[ 0 ], 0.0, rot );
        }
        else if( degree == 180 )
        {
            return rotate( -xy[ 0 ], 0.0, rot );
        }
        else if( degree == 90 )
        {
            return rotate( 0.0, xy[ 1 ], rot );
        }
        else if( degree == 270 )
        {
            return rotate( 0.0, -xy[ 1 ], rot );
        }
        else
        {
            double tn = Math.tan( rad );
            double k = tn * tn + xy[ 1 ] * xy[ 1 ] / ( xy[ 0 ] * xy[ 0 ] );
            double x = xy[ 1 ] / Math.pow( k, .5 );
            if( ( degree >= 90 ) && ( degree <= 270 ) )
            {
                x = -x;
            }
            double y = xy[ 1 ] * Math.pow( ( 1 - x * x / ( xy[ 0 ] * xy[ 0 ] ) ), .5 );
            y = ( degree > 180 ) ? -y : y;

            return rotate( x, y, rot );
        }
    }

    private static double[] rotate( double x0, double y0, double rad )
    {
        double[] xy = new double[ 2 ];
        xy[ 0 ] = x0 * Math.cos( rad ) - y0 * Math.sin( rad );
        xy[ 1 ] = x0 * Math.sin( rad ) + y0 * Math.cos( rad );

        return xy;
        //double rt = Math.pow( x*x + y*y , .5);
        //return rt;
    }

    public static double computeDeltaLBoundary( CIELab target, double deltaE )
    {
        double lt;
        if( target.getL() > 16.0 )
        {
            lt = 0.040975 * target.getL() / ( 1 + 0.01765 * target.getL() );
        }
        else
        {
            lt = 0.511;
        }

        return deltaE * lt;
    }

    //get the ellipse long [0] & short [1] value
    public static double[] computeEllipseAB( double[] target, double deltaE )
    {
        double dp = Math.PI / 180.0;
        double[] xy = new double[ 2 ];
        double ct, ht, gt, t;

        ct = 0.0638 * target[ 1 ] / ( 1 + 0.0131 * target[ 1 ] ) + 0.638;

        xy[ 0 ] = Math.abs( deltaE * ct );//Math.abs(deltaE*Math.pow(ct,.5));

        if( target[ 2 ] > 164 && target[ 2 ] < 345 )
        {
            ht = 0.56 + Math.abs( .2 * Math.cos( ( target[ 2 ] + 168 ) * dp ) );
        }
        else
        {
            ht = 0.36 + Math.abs( 0.4 * Math.cos( ( target[ 2 ] + 35 ) * dp ) );
        }

        gt = Math.sqrt( Math.pow( target[ 1 ], 4.0 ) / ( Math.pow( target[ 1 ], 4.0 ) + 1900 ) );

        t = ht * gt + 1 - gt;
        ht = t * ct;
        xy[ 1 ] = Math.abs( deltaE * ht );//Math.abs(deltaE*Math.pow(ht,.5));

        return xy;
    }

    //return array[360][2], which is the xy points from angle 0 to 360 degree
    //target is the CIELab value of the color, deltaE control the size of the ellipse
    public static double[][] getEllipsePoint( ColorEncoding target, double deltaE )
    {
        if( deltaE <= 0 )
        {
            deltaE = 1.0;
        }

        if( !( target instanceof CIELab ) )
        {
            throw new IllegalArgumentException( "CIELabDE only accepts CIELab color encodings." );  //NOI18N
        }
        CIELab tlab = (CIELab) target;
        double[] lch = { tlab.getL(), tlab.getc(), tlab.geth() };
        double[][] ret = new double[ 360 ][ 2 ];

        double[] xy = computeEllipseAB( lch, deltaE );
        for( int i = 0; i < 360; i++ )
        {
            ret[ i ] = computeEllipse( xy, lch, deltaE, i );
        }

        return ret;
    }
}
