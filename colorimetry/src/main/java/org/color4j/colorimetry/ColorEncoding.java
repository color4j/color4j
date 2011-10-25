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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.color4j.colorimetry.encodings.CIELab;
import org.color4j.colorimetry.encodings.CIELch;
import org.color4j.colorimetry.encodings.CIELuv;
import org.color4j.colorimetry.encodings.HunterLab;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.entities.Reflectance;

/**
 */
public abstract class ColorEncoding
{
    private final static ConcurrentHashMap<String, Class<? extends ColorEncoding>> m_Encodings;

    protected double[] m_Values;
    /**
     * by default, we will assume that the colors calculated are in-gamut
     */
    protected boolean m_InGamut = true;

    public boolean isInGamut()
    {
        return m_InGamut;
    }

    /**
     * @return all available ColorEncoding types.
     */
    public static String[] getEncodingTypes()
    {
        Map m = m_Encodings;
        String[] types = new String[ m.size() ];
        Iterator list = m.keySet().iterator();
        for( int i = 0; list.hasNext(); i++ )
        {
            types[ i ] = (String) list.next();
        }
        return types;
    }

    /**
     * Create an instance of an encoding type from a Reflectance
     * object.
     */
    public static ColorEncoding createInstance( String type, Illuminant ill, Reflectance refl, Observer obs )
    {
        try
        {
            Map m = m_Encodings;
            Class cls = (Class) m.get( type );
            Class[] args = new Class[]{ Illuminant.class, Reflectance.class, Observer.class };
            Method method = cls.getMethod( "create", args ); //NOI18N
            if( method == null )
            {
                return null;
            }
            if( ( method.getModifiers() & Modifier.STATIC ) == 0 )
            {
                return null;
            }

            Object[] params = new Object[]{ ill, refl, obs };
            return (ColorEncoding) method.invoke( null, params );
        }
        catch( Exception e )
        { // ignore the exception and return null.
        }
        return null;
    }

    /**
     * Create an instance of an encoding type from another colorencoding and an
     * XYZ whitepoint
     *
     * @param type       The Requested output type.
     * @param ce         The ColorEncoding containing the source data, typically an XYZ instance.
     * @param whitepoint The whitepoint to use.
     *
     * @return The encoded color representation.
     */
    public static ColorEncoding createInstance( String type, ColorEncoding ce, XYZ whitepoint )
    {
        try
        {
            Class cls = m_Encodings.get( type );
            Class[] args = new Class[]{ ColorEncoding.class, XYZ.class };
            Method method = cls.getMethod( "convert", args );  //NOI18N
            if( method == null )
            {
                return null;
            }
            if( ( method.getModifiers() & Modifier.STATIC ) == 0 )
            {
                return null;
            }

            Object[] params = new Object[]{ ce, whitepoint };
            return (ColorEncoding) method.invoke( null, params );
        }
        catch( Exception e )
        { // ignore the exception and return null.
        }
        return null;
    }

    public static void registerEncoding( String type, Class<? extends ColorEncoding> encodingClass )
    {
        synchronized( m_Encodings )
        {
            m_Encodings.put( type, encodingClass );
        }
    }

    static
    {
        m_Encodings = new ConcurrentHashMap<String, Class<? extends ColorEncoding>>();
        m_Encodings.put( "CIELab", CIELab.class );       //NOI18N
        m_Encodings.put( "XYZ", XYZ.class );          //NOI18N
        m_Encodings.put( "HunterLab", HunterLab.class );    //NOI18N
        m_Encodings.put( "CIELuv", CIELuv.class );       //NOI18N
        m_Encodings.put( "CIELch", CIELch.class );       //NOI18N
    }

    protected ColorEncoding()
    {
        super();
    }

    /**
     * @return list of colour values
     */
    public double[] getColorValues()
    {
        return m_Values;
    }

    public String toString()
    {
        String str = getClass().getName();
        int pos = str.lastIndexOf( "." );  //NOI18N
        if( pos > 0 )
        {
            str = str.substring( pos + 1 );
        }
        StringBuilder sb = new StringBuilder();
        sb.append( str );
        sb.append( "(" );  //NOI18N
        sb.append( m_Values[ 0 ] );
        for( int i = 1; i < m_Values.length; i++ )
        {
            sb.append( ", " );  //NOI18N
            sb.append( m_Values[ i ] );
        }
        sb.append( ")" );  //NOI18N
        return sb.toString();
    }

    public int hashCode()
    {
        double hashcode = 0;
        for( double m_Value : m_Values )
        {
            hashcode = m_Value + hashcode;
        }
        return (int) ( hashcode * 10000 );
    }

    public boolean equals( Object o )
    {
        if( o == null )
        {
            return false;
        }

        if( !o.getClass().equals( this.getClass() ) )
        {
            return false;
        }

        ColorEncoding ce = (ColorEncoding) o;

        if( ce.m_Values.length != m_Values.length )
        {
            return false;
        }

        for( int i = 0; i < m_Values.length; i++ )
        {
            if( m_Values[ i ] != ce.m_Values[ i ] )
            {
                return false;
            }
        }
        return true;
    }

    //public abstract ColorConvertor getConvertor( ColorEncoding encoding );
}
