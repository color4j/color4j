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

package org.color4j.colorimetry;

import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.observers.ObserverImpl;
import org.color4j.colorimetry.weights.WeightsImpl;
import java.util.HashMap;

public class WeightsCache
{
    private static WeightsCache m_Instance;

    private HashMap m_CachedObjects;

    static public WeightsCache getInstance()
    {
        if( m_Instance != null )
        {
            return m_Instance;
        }
        synchronized( WeightsCache.class )
        {
            if( m_Instance == null )
            {
                m_Instance = new WeightsCache();
                m_Instance.loadDefaults();
            }
            return m_Instance;
        }
    }

    private WeightsCache()
    {
        m_CachedObjects = new HashMap();
    }

    public Weights getWeights( Illuminant illum, Observer obs )
    {
        return getWeights( illum, obs, 10 );
    }

    public Weights getWeights( Illuminant illum, Observer obs, int interval )
    {
        synchronized( m_CachedObjects )
        {
            Pair p = new Pair( illum, obs, interval );
            Weights w = (Weights) m_CachedObjects.get( p );

            if( ( w == null ) )
            {
                try
                {
                    w = WeightsImpl.create( illum, obs, interval );
                    m_CachedObjects.put( p, w );
                }
                catch( ColorException cex )
                {
                    return null;
                }
            }
            return w;
        }
    }

    private void loadAndCache( String illumName, String obsName, String interval )
    {
        Class c = null;
        try
        {
            if( interval.equals( "10" ) )
            {
                c = WeightsCache.class.getClassLoader()
                    .loadClass( "org.color4j.colorimetry.weights." + illumName + "_" + obsName );  //NOI18N
            }
            else
            {
                c = WeightsCache.class.getClassLoader()
                    .loadClass( "org.color4j.colorimetry.weights." + illumName + "_" + obsName + "_" + interval );  //NOI18N
            }
        }
        catch( ClassNotFoundException e )
        {
            throw new IllegalArgumentException( "Requested illuminant does not exist." );
        }
        try
        {
            Weights w = (Weights) c.newInstance();
            Illuminant illuminant = IlluminantImpl.create( illumName );
            Observer observer;
            if( obsName.equals( "2" ) )  //NOI18N
            {
                observer = ObserverImpl.create( Observer.NAME_CIE1931 );
            }
            else if( obsName.equals( "10" ) )  //NOI18N
            {
                observer = ObserverImpl.create( Observer.NAME_CIE1964 );
            }
            else
            {
                observer = ObserverImpl.create( obsName );
            }
            int k = Integer.parseInt( interval );

            Pair p = new Pair( illuminant, observer, k );

            m_CachedObjects.put( p, w );
        }
        catch( IllegalAccessException e )
        {
            System.err.println( "Can not instantiate weights class for " + illumName + " and " + obsName );
            e.printStackTrace();
        }
        catch( ClassCastException e )
        {
            System.err.println( "Can not instantiate weights class for " + illumName + " and " + obsName );
            e.printStackTrace();
        }
        catch( InstantiationException e )
        {
            System.err.println( "Can not instantiate weights class for " + illumName + " and " + obsName );
            e.printStackTrace();
        }
        catch( ColorException e )
        {
            System.err.println( "ColorException for weights class for " + illumName + " and " + obsName );
            e.printStackTrace();
        }
    }

    public void clearCache()
    {
        m_CachedObjects = new HashMap();
    }

    public void loadDefaults()
    {
        m_CachedObjects = new HashMap();
        loadAndCache( "D50", "10", "10" );    //NOI18N
        loadAndCache( "D55", "10", "10" );    //NOI18N
        loadAndCache( "D65", "10", "10" );    //NOI18N
        loadAndCache( "D75", "10", "10" );    //NOI18N
        loadAndCache( "A", "10", "10" );      //NOI18N
        loadAndCache( "C", "10", "10" );      //NOI18N
        loadAndCache( "F2", "10", "10" );     //NOI18N
        loadAndCache( "F7", "10", "10" );     //NOI18N
        loadAndCache( "F11", "10", "10" );    //NOI18N
        loadAndCache( "D50", "2", "10" );     //NOI18N
        loadAndCache( "D55", "2", "10" );     //NOI18N
        loadAndCache( "D65", "2", "10" );     //NOI18N
        loadAndCache( "D75", "2", "10" );     //NOI18N
        loadAndCache( "A", "2", "10" );       //NOI18N
        loadAndCache( "C", "2", "10" );       //NOI18N
        loadAndCache( "F2", "2", "10" );      //NOI18N
        loadAndCache( "F7", "2", "10" );      //NOI18N
        loadAndCache( "F11", "2", "10" );     //NOI18N

        loadAndCache( "D50", "10", "20" );    //NOI18N
        loadAndCache( "D55", "10", "20" );    //NOI18N
        loadAndCache( "D65", "10", "20" );    //NOI18N
        loadAndCache( "D75", "10", "20" );    //NOI18N
        loadAndCache( "A", "10", "20" );      //NOI18N
        loadAndCache( "C", "10", "20" );      //NOI18N
        loadAndCache( "F2", "10", "20" );     //NOI18N
        loadAndCache( "F7", "10", "20" );     //NOI18N
        loadAndCache( "F11", "10", "20" );    //NOI18N
        loadAndCache( "D50", "2", "20" );     //NOI18N
        loadAndCache( "D55", "2", "20" );     //NOI18N
        loadAndCache( "D65", "2", "20" );     //NOI18N
        loadAndCache( "D75", "2", "20" );     //NOI18N
        loadAndCache( "A", "2", "20" );       //NOI18N
        loadAndCache( "C", "2", "20" );       //NOI18N
        loadAndCache( "F2", "2", "20" );      //NOI18N
        loadAndCache( "F7", "2", "20" );      //NOI18N
        loadAndCache( "F11", "2", "20" );     //NOI18N
    }

    private final class Pair
    {
        private Illuminant m_Illuminant;
        private Observer m_Observer;
        private int m_Interval;

        Pair( Illuminant illum, Observer observer, int interval )
        {
            if( illum == null || observer == null )
            {
                throw new IllegalArgumentException( "Arguments may not be null." );
            }

            m_Illuminant = illum;
            m_Observer = observer;
            m_Interval = interval;
        }

        Observer getObserver()
        {
            return m_Observer;
        }

        Illuminant getIlluminant()
        {
            return m_Illuminant;
        }

        public boolean equals( Object pair )
        {
            Pair p = (Pair) pair;
            return m_Interval == p.m_Interval &&
                   m_Illuminant.getName().equals( p.m_Illuminant.getName() ) &&
                   m_Observer.getName().equals( p.m_Observer.getName() );
        }

        public int hashCode()
        {
            return m_Illuminant.getName().hashCode() +
                   m_Observer.getName().hashCode() + m_Interval;
        }
    }
}

