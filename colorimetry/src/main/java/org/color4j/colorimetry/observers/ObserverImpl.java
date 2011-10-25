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

package org.color4j.colorimetry.observers;

import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.Weights;
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.weights.WeightsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ObserverImpl
    implements Observer
{
    private static Observer_1964 m_1964 = null;
    private static Observer_1931 m_1931 = null;
    private static final String STRING_10 = "10";
    private static final String STRING_2 = "2";

    private static Logger m_Logger = LoggerFactory.getLogger( ObserverImpl.class );
    private String m_Name;

    static public Observer create( String name )
        throws ColorException
    {
        if( name == null )
        {
            return null;
        }

        if( name.equals( NAME_CIE1964 ) )
        {
            if( m_1964 == null )
            {
                m_1964 = new Observer_1964();
            }
            return m_1964;
        }

        if( name.equals( NAME_CIE1931 ) )
        {
            if( m_1931 == null )
            {
                m_1931 = new Observer_1931();
            }
            return m_1931;
        }

        m_Logger.debug( "Observer creation: Couldn't find a match using degrees, using substring matching instead." );

        if( name.startsWith( STRING_10 ) )
        {
            if( m_1964 == null )
            {
                m_1964 = new Observer_1964();
            }
            return m_1964;
        }

        if( name.startsWith( STRING_2 ) )
        {
            if( m_1931 == null )
            {
                m_1931 = new Observer_1931();
            }
            return m_1931;
        }

        throw new ColorException( "No Observer with the name '" + name + "' defined." );  //NOI18N
    }

    static public String[] getObserverNames()
    {
        String[] sa = { NAME_CIE1931, NAME_CIE1964 };
        return sa;
    }

    protected ObserverImpl( String name )
    {
        m_Name = name;
    }

    public Weights getWeights( Illuminant illuminant )
        throws ColorException
    {
        return WeightsImpl.create( illuminant, this );
    }

    public double getObjectSize( double distance )
    {
        // tan( angle/2 ) = (diameter/2) / distance

        double alpha = getAngle() / 2;
        double diameter = Math.tan( alpha ) * distance * 2;
        return diameter;
    }

    public double getViewDistance( double diameter )
    {
        // tan( angle/2 ) = (diameter/2) / distance
        double alpha = getAngle() / 2;
        double distance = ( diameter / 2 ) / Math.tan( alpha );
        return distance;
    }

    public abstract double getAngle();

    public Class getTypeInterface()
    {
        return Observer.class;
    }
}
