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

import org.color4j.colorimetry.Reflectance;

/**
 * PointND Implementation that has knowledge of Reflectances and LAB values.
 * These objects are created with reflectances and get sorted by clustering
 * algorithms.
 *
 */
public class PointNDReflectanceImpl implements PointND
{
    public PointNDReflectanceImpl( int numDims, String name )
    {
        dim = new double[ numDims ];
        m_NumDims = numDims;
        m_Name = name;
    }

    public PointNDReflectanceImpl( Reflectance refl )//, int numDims)
    {
        dim = new double[ m_NumDims ];

        m_Name = "refl.getID()"; //TODO: FIX
        m_Reflectance = refl;
    }

    public void setDim( int dimIdx, double val )
    {
        if( ( dimIdx < dim.length ) && ( dimIdx >= 0 ) )
        {
            dim[ dimIdx ] = val;
        }
    }

    public int getNumDims()
    {
        return m_NumDims;
    }

    public double[] getDims()
    {
        return dim;
    }

    public double getDim( int dimIdx )
    {
        if( ( dimIdx <= m_NumDims ) && ( dimIdx >= 0 ) )
        {
            return dim[ dimIdx ];
        }
        else
        {
            return 0;
        }
    }

    public void setName( String name )
    {
        m_Name = name;
    }

    public String getName()
    {
        return m_Name;
    }

    public void setNumDims( int dims )
    {
        m_NumDims = dims;
    }

    public Reflectance getReflectance()
    {
        return m_Reflectance;
    }

    public void setReflectance( Reflectance refl )
    {
        m_Reflectance = refl;
    }

    public double distFromOrigin()
    {
        double dist = 0;
        for( int i = 0; i < m_NumDims; i++ )
        {
            dist += ( dim[ i ] * dim[ i ] );
        }
        dist = dist / m_NumDims;
        return dist;
    }

    private double[] dim;
    private int m_NumDims = 3;
    private String m_Name;
    private Reflectance m_Reflectance;
}

