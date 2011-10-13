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

/*
 * BoundingBox.java
 *
 * Created on June 20, 2002, 5:11 PM
 */
package org.color4j.colorimetry.indexing;

import org.color4j.colorimetry.ColorCalculator;
import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.observers.ObserverImpl;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The bounding box takes in a set of three dimensional points
 * and calculates the sphere that surrounds all the points.
 *
 * The centre point of the box is the relevant data for our search indexing
 * and the raidus defines a sphere that contains all the points
 *
 */
public class BoundingSphereCalculator
{
    private static double TOLERANCE = 1.5;

    public BoundingSphereCalculator()
    {
    }

    /* Collection contains colorencoding objects */
    public Collection createColorEncodingsFor( Reflectance refl, String colorspace )
        throws ColorException
    {
        String[] names = getObserverNames();
        int noOfObs = names.length;
        String[] illums = getIlluminantNames();
        Collection ret = new ArrayList( noOfObs * illums.length );
        for( int i = 0; i < noOfObs; i++ )
        {
            Observer observer = ObserverImpl.create( names[ i ] );
            // should this be synchronized?
            ret.addAll( createColorEncodingsFor( refl, observer, colorspace ) );
        }
        return ret;
    }

    /* Collection contains colorencoding objects */
    public Collection createColorEncodingsFor( Reflectance refl, Observer observer, String colorspace )
        throws ColorException
    {
        Collection ret = null;
        String[] names = getIlluminantNames();
        int noOfIllums = names.length;
        ret = new ArrayList( noOfIllums );
        for( int i = 0; i < noOfIllums; i++ )
        {
            Illuminant ill = IlluminantImpl.create( names[ i ] );
            // should this be synchronized?
            ret.add( createColorEncodingsFor( ill, refl, observer, colorspace ) );
        }
        return ret;
    }

    /* Collection returned contains colorencoding objects */
    public ColorEncoding createColorEncodingsFor( Illuminant ill,
                                                  Reflectance refl,
                                                  Observer observer,
                                                  String colorspace
    )
        throws ColorException
    {
        Class clz = ColorCalculator.getColorEncodingClass( colorspace );
        return ColorCalculator.create( clz, ill, refl, observer );
    }

    public double[] calculateCentrePoint( ColorEncoding[] points )
        throws IllegalArgumentException
    {
        if( points == null || points.length == 0 )
        {
            throw new IllegalArgumentException( "Argument is either null or length nil." ); //NOI18N
        }
        double maxDepth;
        double minDepth;
        double maxWidth;
        double minWidth;
        double maxLength;
        double minLength;

        int noOfPoints = points.length;
        double[] tmpPoint = points[ 0 ].getColorValues();
        // access the array once for each value to cut down on
        // bounds checking with each access
        double depth = tmpPoint[ 0 ];
        double width = tmpPoint[ 1 ];
        double len = tmpPoint[ 2 ];
        maxDepth = depth;
        minDepth = depth;
        maxWidth = width;
        minWidth = width;
        maxLength = len;
        minLength = len;
        if( noOfPoints == 1 )
        {
            return new double[]{ depth, width, len };
        }
        for( int i = 1; i < noOfPoints; i++ )
        {
            double valuex = points[ i ].getColorValues()[ 0 ];
            double valuey = points[ i ].getColorValues()[ 1 ];
            double valuez = points[ i ].getColorValues()[ 2 ];
            //QQQQ sledgehammer approach will have to be revisited
            if( valuex > maxDepth )
            {
                maxDepth = valuex;
            }
            if( valuey > maxWidth )
            {
                maxWidth = valuey;
            }
            if( valuez > maxLength )
            {
                maxLength = valuez;
            }

            if( valuex < minDepth )
            {
                minDepth = valuex;
            }
            if( valuey < minWidth )
            {
                minWidth = valuey;
            }
            if( valuez < minLength )
            {
                minLength = valuez;
            }
        }
        return new double[]{
            average( maxDepth, minDepth ),
            average( maxWidth, minWidth ),
            average( maxLength, minLength )
        };
    }

    public double findRadius( Number[] centre, ColorEncoding[] points )
        throws IllegalArgumentException
    {
        if( points == null || points.length == 0 )
        {
            throw new IllegalArgumentException( "Argument is either null or length nil." ); //NOI18N
        }
        double maxPoint = 0.0;
        int noOfPoints = points.length;
        for( int i = 0; i < noOfPoints; i++ )
        {
            Number[] sample = new Double[ 3 ];
            sample[ 0 ] = new Double( points[ i ].getColorValues()[ 0 ] );
            sample[ 1 ] = new Double( points[ i ].getColorValues()[ 1 ] );
            sample[ 2 ] = new Double( points[ i ].getColorValues()[ 2 ] );
            double dist = distance( centre, sample );
            if( dist > maxPoint )
            {
                maxPoint = dist;
            }
        }
        //QQQQ get TOLERANXE level policy from Niclas
        return maxPoint * TOLERANCE;
    }
//	@TODO: Clean up. ML - 05/08/2003    
//    private double[] calculateCentrePoint( double maxx, double minx, 
//                                           double maxy, double miny, 
//                                           double maxz, double minz )
//                                           
//    {
//        double[] ret = new double[ 3 ];
//        ret[ 0 ] = average( maxx, minx );
//        ret[ 1 ] = average( maxy, miny );
//        ret[ 2 ] = average( maxz, minz );
//        return ret;
//    }

    private double average( double a, double b )
    {
        return ( a + b ) / 2;
    }

    private double distance( Number[] here, Number[] there )
    {
        double x = here[ 0 ].doubleValue() - there[ 0 ].doubleValue();
        double y = here[ 1 ].doubleValue() - there[ 1 ].doubleValue();
        double z = here[ 2 ].doubleValue() - there[ 2 ].doubleValue();
        return distance( x, y, z );
    }

    private double distance( double pt1, double pt2, double pt3 )
    {
        double ret = Math.pow( pt1, 2 ) + Math.pow( pt2, 2 ) + Math.pow( pt3, 2 );
        return Math.sqrt( ret );
    }
//	@TODO: Clean up. ML - 05/08/2003   
//    private String[] getColorEncodingNames()
//    {
//        return ColorCalculator.getColorEncodingNames();
//    }

    private String[] getIlluminantNames()
    {
        return IlluminantImpl.getStandardIlluminantNames();
    }

    private String[] getObserverNames()
    {
        //package protected method
        return ObserverImpl.getObserverNames();
    }
}