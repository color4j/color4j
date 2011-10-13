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
 * Created on Jul 16, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.color4j.colorimetry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implements the nearest-neighbour clustering algorithm. Also implements a
 * nearest-neighbour-type sequencing/sorting algorithm on already-clustered
 * points.
 *
 */
public class NearestNeighbour extends AbstractClusterImpl
{

    int m_Dimensions;
    int m_NumPoints; //# of points
    //this variable is unused at the moment. Users generally want to be told
    // how many clusters,
    //rather than selecting it themselves
    int m_NumClusters; //# of classes desired
    private ArrayList m_Points;
    private ArrayList m_Clusters; //this will be an ArrayList of ArrayLists of
    // PointND's
    private double m_Epsilon;
    private double m_CurDistance;
    private ArrayList m_Outliers;
    private int m_MinPts;

    /**
     * Constructor where a number of clusters is set as the termination
     * condition
     *
     * @param points
     * @param dims
     * @param clusters
     */
    public NearestNeighbour( Collection points, int dims, int clusters )
    {
        m_Points = new ArrayList();
        setDataPoints( points );

        m_Dimensions = dims;
        m_NumClusters = clusters;
        m_Clusters = new ArrayList();
        m_Outliers = new ArrayList();
        initializeData();
    }

    /**
     * Constructor where a tolerance value is set as the termination condition
     *
     * @param points
     * @param dims
     * @param tolerance
     */
    public NearestNeighbour( Collection points, int dims, double tolerance )
    {
        m_Points = new ArrayList();
        setDataPoints( points );

        m_Dimensions = dims;
        m_NumClusters = -1;
        m_Epsilon = tolerance;
        m_Clusters = new ArrayList();
        m_Outliers = new ArrayList();
        initializeData();
    }

    public void calculateClusters()
    {
        m_MinPts = 1;
        clusterPoints();
    }

    public void calculateClusters( int minNum )
    {
        m_MinPts = minNum;
        clusterPoints();
    }

    /**
     * Calculates clusters from all points with the Nearest-Neighbour algorithm
     */
    private void clusterPoints()
    {
        int clustIdx = m_NumPoints;
        if( m_Outliers.size() != 0 )
        {
            m_Outliers.clear();
        }
        if( m_Clusters.size() != m_Points.size() )
        {
            initializeData();
        }

        double distance = 0;
        if( m_NumClusters <= 0 )
        {//Epsilon termination
            do
            {
                distance = mergeNearestClusters( m_Epsilon );
                clustIdx--;
            }
            while( ( distance < m_Epsilon ) && ( clustIdx > 1 ) );
        }
        removeOutliers( m_MinPts );
    }

    /**
     * Reorders all current clusters in order of Nearest-Neighbour, starting
     * with cluster nearest origin. Same sequencing used for Nearest Neighbour
     * clustering class.
     */
    public void sequenceClusters()
    {
        int targetIdx, pointIdx;

        if( m_Clusters.size() == 0 )
        {
            return;
        }

        ArrayList newClusters, oldClusters;
        Object firstPoint;
        ArrayList refCluster, nearestCluster;

        oldClusters = new ArrayList( m_Clusters.size() );
        oldClusters.addAll( m_Clusters );
        newClusters = new ArrayList();
        //find out which cluster should be first
        //start with cluster whose mean is closest to origin or standard
        targetIdx = getClusterNearestOrigin();
        refCluster = (ArrayList) oldClusters.get( targetIdx );
        pointIdx = getPointNearestOrigin( refCluster );
        //Now make the first point of that cluster the point nearest the origin
        firstPoint = refCluster.get( pointIdx );
        refCluster.remove( firstPoint );
        refCluster.add( 0, firstPoint );
        nearestCluster = null;
        newClusters.add( refCluster );
        oldClusters.remove( refCluster );
        while( oldClusters.size() > 0 )
        {
            nearestCluster = findNearestCluster( refCluster, oldClusters );
            if( nearestCluster == null )
            {
                return;
            }
            newClusters.add( nearestCluster );
            oldClusters.remove( nearestCluster );
            refCluster = nearestCluster;
        }
        //arrA should have clusters in order... now must sequence points in
        // each cluster
        m_Clusters = newClusters;
        sequencePoints();
    }

    /**
     * Take current sequence of clusters, and sequence the points within each
     * one, leaving the first and last points unchanged
     */
    public void sequencePoints()
    {
        ArrayList cluster;
        Object nearestObj;
        int idx;
        for( int i = 0; i < m_Clusters.size(); i++ )
        {
            cluster = (ArrayList) m_Clusters.get( i );
            if( cluster.size() > 1 )
            {
                for( int j = 0; j < cluster.size() - 2; j++ )
                {
                    idx = findNearestPointAfterIdx( j, cluster );
                    nearestObj = cluster.get( idx );
                    cluster.remove( nearestObj );
                    cluster.add( j + 1, nearestObj );
                }
            }
        }
    }

    /*
     * Get the clustered data. Returns an ArrayList of ArrayLists, each being a
     * cluster
     */
    public Collection getClusters()
    {
        return m_Clusters;
    }

    public Collection getOutliers()
    {
        return m_Outliers;
    }

    /*
     * Sets a new set of data points
     */
    public void setDataPoints( Collection points )
    {
        if( !m_Points.isEmpty() )
        {
            m_Points.clear();
        }
        m_Points.addAll( points );
        m_NumPoints = m_Points.size();
    }

    /**
     * Removes all clusters with fewer than minNumber points
     *
     * @param minNumber -
     *                  cutoff value for cluster number
     */
    public void removeOutliers( int minNumber )
    {
        if( minNumber <= 1 )
        {
            return;
        }
        ArrayList currCluster;
        m_Outliers.clear();
        for( int i = m_Clusters.size() - 1; i >= 0; i-- )
        {
            currCluster = (ArrayList) m_Clusters.get( i );
            if( currCluster.size() < minNumber )
            {
                //break the cluster apart and place the points in outlier array
                for( int j = 0; j < currCluster.size(); j++ )
                {
                    m_Outliers.add( currCluster.get( j ) );
                }
                m_Clusters.remove( currCluster );
            }
        }
    }

    private int getClusterNearestOrigin()
    {
        PointND mean;
        double dist;
        double temp = 0;
        int idx = 0;
        if( m_Clusters.size() == 0 )
        {
            return -1;
        }
        mean = calculateMean( (PointND[]) ( (ArrayList) m_Clusters.get( 0 ) )
            .toArray( new PointND[ 0 ] ) );
        dist = mean.distFromOrigin();
        for( int i = 1; i < m_Clusters.size(); i++ )
        {
            mean = calculateMean( (PointND[]) ( (ArrayList) m_Clusters.get( i ) )
                .toArray( new PointND[ 0 ] ) );
            temp = mean.distFromOrigin();
            if( temp < dist )
            {
                dist = temp;
                idx = i;
            }
        }
        return idx;
    }

    private int getPointNearestOrigin( ArrayList points )
    {
        double dist, temp;
        int retIdx = 0;

        dist = m_MaxVal;
        for( int i = 0; i < points.size(); i++ )
        {
            temp = ( (PointND) points.get( i ) ).distFromOrigin();
            if( temp < dist )
            {
                dist = temp;
                retIdx = i;
            }
        }
        return retIdx;
    }

    /**
     * Given a target index within an ArrayList of clusters, returns the index
     * of the cluster nearest based on the nearest distance between points in
     * each cluster. Also returns the distance between those clusters
     *
     * @param targetIdx
     * @param clustArr
     *
     * @return index of nearest cluster
     */
    private int findNearestCluster( int targetIdx, ArrayList clustArr )
    {
        double tempDist;
        double curDist = m_MaxVal;
        int nearIdx = targetIdx;

        ArrayList arrayA, arrayB;
        arrayA = (ArrayList) clustArr.get( targetIdx );
        arrayB = null;

        for( int j = 0; j < clustArr.size(); j++ )
        {
            if( j != targetIdx )
            {
                arrayB = (ArrayList) clustArr.get( j );
                tempDist = getNearestDistance( arrayA, arrayB );
                if( tempDist < curDist )
                {
                    curDist = tempDist;
                    nearIdx = j;
                }
            }
        }//outer for
        m_CurDistance = curDist;
        return nearIdx;
    }

    /**
     * Finds the distance between two nearest points in 2 given arrays
     *
     * @param arrA
     * @param arrB
     *
     * @return the distance between furthest points
     */
    private double getNearestDistance( ArrayList arrA, ArrayList arrB )
    {
        double tempDist;
        double curDist = m_MaxVal;
        for( int i = 0; i < arrA.size(); i++ )
        {
            for( int j = 0; j < arrB.size(); j++ )
            {
                tempDist = getDistance( (PointND) arrA.get( i ), (PointND) arrB
                    .get( j ) );
                if( tempDist < curDist )
                {
                    curDist = tempDist;
                }
            }
        }
        return curDist;
    }

    /**
     * Given a specific cluster of points (targetCluster), finds the nearest
     * cluster in a provided set of clusters (clustArr), based on nearest point
     * in "targetCluster" to each cluster in "clustArr".
     * -- "targetCluster" is not a member of clustArr
     *
     * @return ArrayList - the nearest cluster to the targetCluster provided
     */
    private ArrayList findNearestCluster( ArrayList targetCluster,
                                          ArrayList clustArr
    )
    {
        double tempDist;
        double curDist = m_MaxVal;
        ArrayList nearestCluster;
        //Cluster index and Point indices
        int clustArrIdx, targetIdx, nearestIdx;
        boolean targetMany = ( targetCluster.size() > 1 );
        clustArrIdx = -1;
        targetIdx = -1;
        nearestIdx = -1;
        nearestCluster = null;
        for( int j = 0; j < clustArr.size(); j++ )
        {
            nearestCluster = (ArrayList) clustArr.get( j );

            for( int k = 0; k < targetCluster.size(); k++ )
            {
                //don't want to deal with 1st point if target cluster has
                //more than 1 point
                if( k == 0 && targetMany )
                {
                    continue;
                }
                for( int m = 0; m < nearestCluster.size(); m++ )
                {
                    tempDist = getDistance( (PointND) targetCluster.get( k ),
                                            (PointND) nearestCluster.get( m ) );

                    if( tempDist < curDist )
                    {
                        curDist = tempDist;
                        clustArrIdx = j;
                        targetIdx = k;
                        nearestIdx = m;
                    }
                }
            }
        }//outer for
        //re-arrange so final element in target cluster is point nearest to next
        // cluster 
        if( targetMany )
        {
            Object targObj = targetCluster.get( targetIdx );
            targetCluster.remove( targetIdx );
            targetCluster.add( targObj );
        }
        //re-arrange so first element in nearest cluster is point nearest to previous
        // cluster
        if( clustArrIdx == -1 )
        {
            return null;
        }

        nearestCluster = (ArrayList) clustArr.get( clustArrIdx );
        if( nearestCluster != null )
        {
            Object tempObj = nearestCluster.get( nearestIdx );
            nearestCluster.remove( nearestIdx );
            nearestCluster.add( 0, tempObj );
        }
        else
        {
//            m_Logger.debug( "findNearestCluster:: Error retrieving nearest point in clustering." );
            return null;
        }
        return nearestCluster;
    }

    /*
    * Returns index of point after 'idx', closest to the point located at 'idx'
    * in pointList
    */
    private int findNearestPointAfterIdx( int idx, ArrayList pointList )
    {
        double dist, temp;
        PointND point = (PointND) pointList.get( idx );
        if( idx >= pointList.size() - 2 )
        {
            return idx;
        }

        dist = getDistance( point, (PointND) pointList.get( idx + 1 ) );
        int retIdx = idx + 1;
        for( int i = idx + 2; i < ( pointList.size() - 1 ); i++ )
        {
            temp = getDistance( point, (PointND) pointList.get( i ) );
            if( temp < dist )
            {
                dist = temp;
                retIdx = i;
            }
        }
        return retIdx;
    }

    private void initializeData()
    {
        ArrayList tempArr;
        //start by making m_NumPoints clusters, 1 for each point
        for( int i = 0; i < m_NumPoints; i++ )
        {
            tempArr = new ArrayList( 1 );
            tempArr.add( m_Points.get( i ) );
            m_Clusters.add( tempArr );
        }
    }

    /**
     * Calculates nearest clusters using nearest points between any 2 clusters
     * and merges them into one new cluster
     *
     * @return the distance between the two merged clusters
     */
    /**
     * Calculates nearest clusters using farthest points between any 2 clusters
     * and merges them into one new cluster
     *
     * @return the distance between the two merged clusters
     */
    private double mergeNearestClusters( double tolerance )
    {
        double tempDist;
        double curDist = m_MaxVal;

        //Cluster indices and Point indices
        int clusterIdxA, clusterIdxB;//, ptIdxA, ptIdxB;
        int tempIdx;
        clusterIdxA = -1;
        clusterIdxB = -1;

        //if we've already reach 1 large cluster
        if( m_Clusters.size() <= 1 )
        {
            return -1;
        }
        for( int i = 0; i < m_Clusters.size(); i++ )
        {
            tempIdx = findNearestCluster( i, m_Clusters );
            tempDist = m_CurDistance;

            if( tempDist < curDist )
            {
                curDist = tempDist;
                clusterIdxA = i;
                clusterIdxB = tempIdx;
            }
        }
        //curDist holds the distance between the nearest clusters
        //only merge if it is within the tolerance
        if( curDist > tolerance )
        {
            return curDist;
        }
        //Now merge the clusters
        if( ( clusterIdxA != -1 ) && ( clusterIdxB != -1 ) )
        {
            ( (ArrayList) m_Clusters.get( clusterIdxA ) )
                .addAll( (ArrayList) m_Clusters.get( clusterIdxB ) );
            m_Clusters.remove( clusterIdxB );
        }
        return curDist;
    }
}