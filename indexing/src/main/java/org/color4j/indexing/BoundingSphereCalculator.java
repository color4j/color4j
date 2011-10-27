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

/*
 * BoundingBox.java
 *
 * Created on June 20, 2002, 5:11 PM
 */
package org.color4j.indexing;

import java.util.ArrayList;
import java.util.Collection;
import org.color4j.colorimetry.ColorEncoding;
import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.Illuminant;
import org.color4j.colorimetry.Observer;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.DefaultEncodingFactory;
import org.color4j.colorimetry.encodings.EncodingFactory;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.observers.ObserverImpl;

/**
 * The bounding box takes in a set of three dimensional points
 * and calculates the sphere that surrounds all the points.
 *
 * The centre point of the box is the relevant data for our search indexing
 * and the raidus defines a sphere that contains all the points
 */
public class BoundingSphereCalculator
{
    private static double TOLERANCE = 1.5;
    private EncodingFactory factory;

    public BoundingSphereCalculator()
    {
        factory = new DefaultEncodingFactory();
    }

    /* Collection contains colorencoding objects */
    public <T extends ColorEncoding> Collection<T> createColorEncodingsFor( Reflectance refl, Class<T> encoding )
        throws ColorException
    {
        String[] names = getObserverNames();
        int noOfObs = names.length;
        String[] illums = getIlluminantNames();
        Collection<T> ret = new ArrayList<T>( noOfObs * illums.length );
        for( int i = 0; i < noOfObs; i++ )
        {
            Observer observer = ObserverImpl.create( names[ i ] );
            // should this be synchronized?
            ret.addAll( createColorEncodingsFor( refl, observer, encoding ) );
        }
        return ret;
    }

    /* Collection contains colorencoding objects */
    public <T extends ColorEncoding> Collection<T> createColorEncodingsFor( Reflectance refl,
                                                                            Observer observer,
                                                                            Class<T> encoding
    )
        throws ColorException
    {
        String[] names = getIlluminantNames();
        int noOfIllums = names.length;
        Collection<T> ret = new ArrayList<T>( noOfIllums );
        for( int i = 0; i < noOfIllums; i++ )
        {
            Illuminant ill = IlluminantImpl.create( names[ i ] );
            // should this be synchronized?
            ret.add( factory.create( encoding, ill, refl, observer ) );
        }
        return ret;
    }

    public double[] calculateCentrePoint( CIELab[] points )
        throws IllegalArgumentException
    {
        if( points == null || points.length == 0 )
        {
            throw new IllegalArgumentException( "Argument is either null or length nil." ); //NOI18N
        }
        int noOfPoints = points.length;
        double maxL = Double.MAX_VALUE;
        double minL = Double.MIN_VALUE;
        double maxA = Double.MAX_VALUE;
        double minA = Double.MIN_VALUE;
        double maxB = Double.MAX_VALUE;
        double minB = Double.MIN_VALUE;
        for( int i = 0; i < noOfPoints; i++ )
        {
            double valueL = points[ i ].getL();
            double valuea = points[ i ].geta();
            double valueB = points[ i ].getb();
            //QQQQ sledgehammer approach will have to be revisited

            if( valueL > maxL )
            {
                maxL = valueL;
            }
            else if( valueL < minL )
            {
                minL = valueL;
            }
            if( valuea > maxA )
            {
                maxA = valuea;
            }
            else if( valuea < minA )
            {
                minA = valuea;
            }
            if( valueB > maxB )
            {
                maxB = valueB;
            }
            else if( valueB < minB )
            {
                minB = valueB;
            }
        }
        return new double[]{
            average( maxL, minL ),
            average( maxA, minA ),
            average( maxB, minB )
        };
    }

    public double findRadius( CIELab centre, CIELab[] points )
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
            double lStar = points[ i ].getL();
            double aStar = points[ i ].geta();
            double bStar = points[ i ].getb();
            double x = centre.getL() - lStar;
            double y = centre.geta() - aStar;
            double z = centre.getb() - bStar;
            double ret = Math.pow( x, 3 ) + Math.pow( y, 3 ) + Math.pow( z, 3 );
            double dist = Math.pow( ret, 1 / 3 );
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