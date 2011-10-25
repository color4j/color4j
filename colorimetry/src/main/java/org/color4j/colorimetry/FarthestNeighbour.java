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
 * Created on Aug 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.color4j.colorimetry;

import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses farthest-neighbour clustering algorithm for initial clustering. Then
 * uses "2nd pass" to determine outliers and re-merge them with existing
 * clusters from initial clustering. Also uses Nearest-Neighbour sequencing of
 * points. This is called "Adaptive Neighbour" in the UI because it's not 100% Farthest
 * Neighbour anymore
 *
 * Delta E Tolerance value will not be accurate for larger values (ie. greater than 5 or 6.)
 * It is only a linear calculation of Delta E based on Delta's L,C,H and comparing
 * 2 separate points related to the standard (based on their distance from standard) but not
 * to each other. It is accurate for realistic measurement comparisons
 * of clusters with Delta E of less than 4. But when approaching 5+, it starts to be less
 * accurate.
 *
 */
public class FarthestNeighbour extends AbstractClusterImpl
{

    int m_Dimensions;
    int m_NumPoints; //# of points
    int m_NumClusters; //# of classes desired (this is currently unused
    //as user should not have to know how many clusters he/she wants
    private ArrayList m_Points;
    private ArrayList m_Clusters; //this will be an ArrayList of ArrayLists of
    // PointND's
    private double m_Epsilon;
    private double m_CurDistance;
    private ArrayList m_Outliers;
    private int m_NumReclustered;
    private int m_MinPts;

    private static Logger m_Logger = LoggerFactory.getLogger( FarthestNeighbour.class );

    /**
     * Constructor where a number of clusters is set as the termination
     * condition
     *
     * @param points
     * @param dims
     * @param clusters
     */
//    public FarthestNeighbour(Collection points, int dims, int clusters)
//    {
//        m_Points = new ArrayList();
//        setDataPoints( points );
//
//        m_Dimensions = dims;
//        m_NumClusters = clusters;
//        m_Clusters = new ArrayList();
//        m_Outliers = new ArrayList();
//        initializeData();
//    }

    /**
     * Constructor where a tolerance value is set as the termination condition
     *
     * @param points
     * @param dims
     * @param tolerance
     */
    public FarthestNeighbour( Collection points, int dims, double tolerance )
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

    /**
     * Given current set of data points, calculates clusters with each
     * cluster restricted to have at least minNum points
     */
    public void calculateClusters( int minNum )
    {
        m_MinPts = minNum;
        clusterPoints();
    }

    /**
     * Given current set of data points, calculates clusters with each
     * cluster restricted to have at least 1 point
     */
    public void calculateClusters()
    {
        m_MinPts = 1;
        clusterPoints();
    }

    /**
     * Get the clustered data. Clusters are stored as an ArrayList of
     * ArrayLists, each of the latter being a cluster
     */
    public Collection getClusters()
    {
        return m_Clusters;
    }

    /**
     * Outliers are stored as an array list of points
     */
    public Collection getOutliers()
    {
        return m_Outliers;
    }

    /**
     * Gets the number of outlying points which were placed back into
     * existing clusters
     *
     * @return Number of reclustered points
     */
    public int getNumReclusteredPts()
    {
        return m_NumReclustered;
    }

    /**
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
                m_Logger.debug( "Error: calculateSequences()" );
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

    private void initializeData()
    {
        m_NumReclustered = 0;
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
     * Take current sequence of clusters, and sequence the points within each
     * one, leaving the first and last points unchanged
     */
    private void sequencePoints()
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

    /**
     * Removes all clusters with fewer than minNumber points
     *
     * @param minNumber -
     *                  cutoff value for cluster membership
     */
    private void removeOutliers( int minNumber )
    {
        if( minNumber <= 1 )
        {
            return;
        }
        ArrayList currCluster;
//        m_NumReclustered = 0;
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
        //for now, include optimization in the removal of outliers
        //TODO: May want to make this a separate action controlled by a button
        // or something. 
        // -lijen-
        mergeOutliersToClusters();
        //        clusterOutliers();
    }

    /**
     * After outliers have been removed, they are in a separate arraylist. Take
     * those outlying points and try to merge them into existing clusters.
     */
    private void mergeOutliersToClusters()
    {
        if( m_Outliers.size() == 0 )
        {
            return;
        }
        ArrayList subList = new ArrayList();
        ArrayList distances = new ArrayList();
        double dist;

        for( int i = ( m_Outliers.size() - 1 ); i >= 0; i-- )
        {
            //create sublist of clusters that outlier can be grouped with
            for( int j = 0; j < m_Clusters.size(); j++ )
            {
                dist = getFarthestDistance( (PointND) m_Outliers.get( i ),
                                            (ArrayList) m_Clusters.get( j ) );
                if( dist < m_Epsilon )
                {
                    subList.add( m_Clusters.get( j ) );
                    distances.add( new Double( dist ) );
                }
            }
            //now determine which cluster to group the outlier with
            //1. gage by # of points in each cluster (more pts = better pick)
            //2. upon a tie, use nearest cluster
            int retIdx, tempPts;
            int curPts = 0;
            double dist1, dist2;
            //if outlier doesn't fit anywhere
            if( subList.size() > 0 )
            {
                curPts = ( (ArrayList) subList.get( 0 ) ).size();
                retIdx = 0;
                for( int k = 1; k < subList.size(); k++ )
                {
                    tempPts = ( (ArrayList) subList.get( k ) ).size();
                    if( tempPts > curPts )
                    {
                        curPts = tempPts;
                        retIdx = k;
                    }
                    //in the event of a tie
                    else if( tempPts == curPts )
                    {
                        dist1 = ( (Double) distances.get( retIdx ) )
                            .doubleValue();
                        dist2 = ( (Double) distances.get( k ) ).doubleValue();
                        //take nearest cluster
                        if( dist2 < dist1 )
                        {
                            retIdx = k;
                        }
                    }
                }
                //retIdx has index of best cluster to merge. So merge, darnit!
                ( (ArrayList) subList.get( retIdx ) ).add( m_Outliers.get( i ) );
                m_NumReclustered++;
                m_Outliers.remove( i );
                subList.clear();
            }
        }
    }

    /**
     * Takes remaining outlier points and runs farthest neighbour algorithm on
     * them independently. THen adds the new clusters to existing cluster list
     */
    private void clusterOutliers()
    {
        if( m_Outliers.size() < m_MinPts )
        {
            return;
        }

        int clustIdx = m_Outliers.size();
        double distance = 0;
        // No point in doing this if there aren't enough outlier points
        // left to re-cluster
        ArrayList outlierCopy = new ArrayList( m_Outliers.size() );
        ArrayList tempArr;
        for( int i = 0; i < m_Outliers.size(); i++ )
        {
            tempArr = new ArrayList( 1 );
            tempArr.add( m_Outliers.get( i ) );
            outlierCopy.add( tempArr );
        }
        m_Logger.debug( "\n m_Outliers initial size=" + m_Outliers.size() );
        do
        {
            distance = mergeNearestClusters( m_Epsilon, outlierCopy );
            clustIdx--;
        }
        while( ( distance < m_Epsilon ) && ( clustIdx > 1 ) );

        m_Logger.debug( "\n outlierCopy size=" + outlierCopy.size() );

        m_Outliers.clear();
        for( int j = 0; j < outlierCopy.size(); j++ )
        {

            if( ( (ArrayList) outlierCopy.get( j ) ).size() >= m_MinPts )
            {// enough pts to be a valid cluster
                m_Logger.debug( "\n New cluster being added to m_Clusters::"
                                + ( j ) + " It's size="
                                + ( (ArrayList) outlierCopy.get( j ) ).size() );
                m_Clusters.add( outlierCopy.get( j ) );
            }
            else
            {// place these remaining outliers in new Outlier class

                ArrayList arr = (ArrayList) outlierCopy.get( j );
                m_Logger.debug( "\n This cluster not added to m_Clusters::"
                                + ( j ) + " It's size=" + arr.size() );
                for( int k = 0; k < arr.size(); k++ )
                {
                    m_Outliers.add( arr.get( k ) );
                }
            }
        }
    }

    /**
     * Finds the distance between two furthest points in 2 given arrays
     *
     * @param arrA
     * @param arrB
     *
     * @return the distance between furthest points
     */
    private double getFarthestDistance( ArrayList arrA, ArrayList arrB )
    {
        double tempDist;
        double curDist = 0;

        for( int i = 0; i < arrA.size(); i++ )
        {
            for( int j = 0; j < arrB.size(); j++ )
            {
                tempDist = getDistance( (PointND) arrA.get( i ), (PointND) arrB
                    .get( j ) );
                if( tempDist > curDist )
                {
                    curDist = tempDist;
                }
            }
        }

        return curDist;
    }

    /**
     * Gets the distance between given "point" and the farthest point in cluster
     * arrB
     *
     * @param point
     * @param arrB
     *
     * @return the distance
     */
    private double getFarthestDistance( PointND point, ArrayList arrB )
    {
        double tempDist;
        double curDist = 0;

        for( int j = 0; j < arrB.size(); j++ )
        {
            tempDist = getDistance( point, (PointND) arrB.get( j ) );
            if( tempDist > curDist )
            {
                curDist = tempDist;
            }
        }

        return curDist;
    }

    /**
     * Calculates nearest clusters using farthest points between any 2 clusters
     * and merges them into one new cluster
     *
     * @return the distance between the two merged clusters
     */
    private double mergeNearestClusters( double tolerance, ArrayList clusterPts )
    {
        double tempDist;
        double curDist = m_MaxVal;

        //Cluster indices and Point indices
        int clusterIdxA, clusterIdxB;//, ptIdxA, ptIdxB;
        int tempIdx;
        clusterIdxA = -1;
        clusterIdxB = -1;

        if( clusterPts.size() <= 1 )
        {
            return -1;
        }
        for( int i = 0; i < clusterPts.size(); i++ )
        {
            tempIdx = findNearestCluster( i, clusterPts );
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
            ( (ArrayList) clusterPts.get( clusterIdxA ) )
                .addAll( (ArrayList) clusterPts.get( clusterIdxB ) );
            clusterPts.remove( clusterIdxB );
        }
        return curDist;
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
     * Given a specific cluster of points (targetCluster), finds the nearest
     * cluster in a provided set of clusters (clustArr), based on nearest point
     * in "targetCluster" to each cluster in "clustArr". -- "targetCluster" is
     * not a member of clustArr
     * -Used in Sequencing-
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
        // re-arrange so final element in target cluster is point nearest to
        // next cluster
        if( targetMany )
        {
            Object targObj = targetCluster.get( targetIdx );
            targetCluster.remove( targetIdx );
            targetCluster.add( targObj );
        }
        // re-arrange so first element in nearest cluster is point nearest to
        // previous cluster
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
            m_Logger
                .debug( "findNearestCluster:: Error retrieving nearest point in clustering." );
            return null;
        }
        return nearestCluster;
    }

    /**
     * Given a target index within an ArrayList of clusters, returns the index
     * of the cluster nearest based on the farthest distance between points in
     * each cluster.
     * -Used in Clustering-
     *
     * @param targetIdx -
     *                  index of target cluster in arraylist
     * @param clustArr  -
     *                  arraylist of clusters
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
                tempDist = getFarthestDistance( arrayA, arrayB );
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
     * Calculates clusters from all points with the Farthest-Neighbour algorithm
     */
    private void clusterPoints()
    {
        int clustIdx = m_NumPoints;
        if( m_Outliers.size() != 0 )
        {
            m_Outliers.clear();
        }
        double distance = 0;

        if( m_NumClusters <= 0 )
        {//Epsilon termination
            do
            {
                distance = mergeNearestClusters( m_Epsilon, m_Clusters );
                clustIdx--;
            }
            while( ( distance < m_Epsilon ) && ( clustIdx > 1 ) );
        }
        m_Outliers.clear();
        determineOutliers();
        //        TODO: Use this command if the other stuff does not work out
        //        removeOutliers(m_MinPts);
    }

    /**
     * Figures out which clusters should be considered outliers. This is a
     * recursive method whose exit condition is either having found a cluster
     * that satisfies the minimum points criteria or having paired down the
     * cluster list to the one remaining, largest cluster possible.
     */
    private void determineOutliers()
    {
        if( m_MinPts == 1 )
        {
            return;
        }
        // Now we check the clusters to see if any of them satisfy the minimum
        // points criteria.
        boolean hasValidCluster = false;
        for( int i = 0; i < m_Clusters.size(); i++ )
        {
            if( ( (ArrayList) m_Clusters.get( i ) ).size() >= m_MinPts )
            {
                hasValidCluster = true;
                break;
            }
        }
        if( hasValidCluster )
        {
            //exit condition #1
            removeOutliers( m_MinPts );
        }
        else
        { // if none satisfy, we take the lower half of the clusters (by
            // membership) and make them outliers
            // then try merging them with the remaining top half clusters
            if( m_Clusters.size() <= 1 )
            {
                //exit condition #2, only 1 cluster left
                mergeOutliersToClusters();
                m_Logger.debug( "\nJust merged outliers" );
                return;
            }

            int halfClustIdx = ( m_Clusters.size() / 2 ) - 1;
            if( halfClustIdx <= 0 )
            {
                halfClustIdx = m_Clusters.size() - 2;
            }
            int[] sizes = new int[ m_Clusters.size() ];
            for( int i = 0; i < sizes.length; i++ )
            {
                sizes[ i ] = ( (ArrayList) m_Clusters.get( i ) ).size();
            }
            QuickSortAlg qs = new QuickSortAlg();
            try
            {
                qs.sort( sizes );
            }
            catch( Exception e )
            {
                m_Logger.debug( e.getMessage() );
                return;
            }
            int cutoff = sizes[ halfClustIdx ];
            removeOutliers( cutoff + 1 );
            determineOutliers();
        }
    }

    //*************************************************************************
    //**************************** INNER CLASSES ******************************
    //*************************************************************************

    /**
     * This is James Gosling's famous quick sort algorithm.
     */
    class QuickSortAlg
    {

        void sort( int arr[], int lowIdx, int hiIdx )
            throws Exception
        {
            int lo = lowIdx;
            int hi = hiIdx;
            if( lo >= hi )
            {
                return;
            }
            else if( lo == hi - 1 )
            {
                if( arr[ lo ] > arr[ hi ] )
                {
                    int T = arr[ lo ];
                    arr[ lo ] = arr[ hi ];
                    arr[ hi ] = T;
                }
                return;
            }
            int pivot = arr[ ( lo + hi ) / 2 ];
            arr[ ( lo + hi ) / 2 ] = arr[ hi ];
            arr[ hi ] = pivot;
            while( lo < hi )
            {
                while( arr[ lo ] <= pivot && lo < hi )
                {
                    lo++;
                }
                while( pivot <= arr[ hi ] && lo < hi )
                {
                    hi--;
                }
                if( lo < hi )
                {
                    int T = arr[ lo ];
                    arr[ lo ] = arr[ hi ];
                    arr[ hi ] = T;
                }
            }
            arr[ hiIdx ] = arr[ hi ];
            arr[ hi ] = pivot;
            sort( arr, lowIdx, lo - 1 );
            sort( arr, hi + 1, hiIdx );
        }

        void sort( int arr[] )
            throws Exception
        {
            sort( arr, 0, arr.length - 1 );
        }
    }
}