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
 * Created on Aug 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.color4j.colorimetry;

import java.util.Collection;

/**
 * Abstract class for clustering algorithms and the
 * sequencing algorithms that are included with them
 *
 */
public abstract class AbstractClusterImpl implements Clustering, Sequencing
{
    protected final double m_MaxVal = 9999999;

    public abstract void calculateClusters();

    public abstract void calculateClusters( int minPts );

    public abstract Collection getClusters();

    public abstract void setDataPoints( Collection points );

    public abstract void sequenceClusters();

    public PointND calculateMean( PointND[] points )
    {
        int dimensions = points[ 0 ].getNumDims();
        double[] mean = new double[ dimensions ];
        PointND theMean = new PointNDReflectanceImpl( dimensions, "Mean" );
        double[] point;
        for( int i = 0; i < points.length; i++ )
        {
            point = points[ i ].getDims();

            for( int j = 0; j < dimensions; j++ )
            {
                mean[ j ] += point[ j ];
            }
        }
        //average it out now
        for( int i = 0; i < dimensions; i++ )
        {
            mean[ i ] = mean[ i ] / points.length;
            theMean.setDim( i, mean[ i ] );
        }

        return theMean;
    }

    /**
     * Linear Distance in N dimensions
     *
     * @param pt1
     * @param pt2
     *
     * @return
     */
    protected double getDistance( PointND pt1, PointND pt2 )
    {
        double[] p1, p2;

        double dist, temp;

        p1 = pt1.getDims();
        p2 = pt2.getDims();
        if( p1.length != p2.length )
        {
            return -1;
        }
        int m_Dimensions = p1.length;
        temp = ( p2[ 0 ] - p1[ 0 ] ) * ( p2[ 0 ] - p1[ 0 ] );
        for( int i = 1; i < m_Dimensions; i++ )
        {
            temp += ( p2[ i ] - p1[ i ] ) * ( p2[ i ] - p1[ i ] );
        }

        dist = Math.sqrt( temp );
        return dist;
    }
}
