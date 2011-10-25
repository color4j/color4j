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

package org.color4j.colorimetry.illuminants;

import org.color4j.colorimetry.IlluminationException;
import org.color4j.colorimetry.Spectrum;
import org.color4j.colorimetry.entities.Illuminant;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the Illuminant.
 * <p>The IlluminantImpl serves two independent purposes. First it is the implementation that understands the Standard Illuminant Names, and can create (<code>static public Illuminant create( String illlumant)</code> method) hardcoded Illuminant instances. But it also allows the user to create Illuminant objects out of given <code>org.color4j.colorimetry.Spectrum</code> objects, and associate it with a new.</p>
 *
 * @see org.color4j.colorimetry.entities.Illuminant
 * @see org.color4j.colorimetry.Spectrum
 */
public class IlluminantImpl
    implements Illuminant
{
    private Spectrum m_Spectrum;
    private static Map m_Illuminants;

    static private final String[] m_Standards =
        {
            "A",        //NOI18N
            "C",        //NOI18N
            "CWF",       //NOI18N
            "D50",      //NOI18N
            "D55",      //NOI18N
            "D65",      //NOI18N
            "D75",      //NOI18N
            "Daylight", //NOI18N
            "Deluxe",   //NOI18N
//      "F1",       //NOI18N
            "F2",       //NOI18N
//      "F3",       //NOI18N
//      "F4",       //NOI18N
//      "F5",       //NOI18N
//      "F6",       //NOI18N
            "F7",       //NOI18N
//      "F8",       //NOI18N
//      "F9",       //NOI18N
//      "F10",      //NOI18N
            "F11",      //NOI18N
            "F12",      //NOI18N
//      "B",        //NOI18N
            "Incan",    //NOI18N
            "TL83",     //NOI18N
            "TL84",     //NOI18N
            "U30"      //NOI18N
        };
    private String m_Name;

    /**
     * Creates a Standard Illuminant.
     * <p>The given parameter <code>name</code> refers to one of the Standard Illuminants available on the current system. The method <code>getStandardIlluminantNames</code> can be used to retrieve the available standard illuminant names.</p>
     *
     * @throws IlluminationException if the given <code>name</code> does not exist.
     */
    static synchronized public Illuminant create( String name )
        throws IlluminationException
    {
        if( name == null )
        {
            throw new IlluminationException( "Illuminant's Name is required" ); //NOI18N
        }
        if( m_Illuminants == null )
        {
            m_Illuminants = new HashMap();
        }

        Illuminant cache = (Illuminant) m_Illuminants.get( name );
        if( cache != null )
        {
            return cache;
        }

        Spectrum spectrum = null;
        for( int i = 0; i < m_Standards.length; i++ )
        {
            if( m_Standards[ i ].equals( name ) )
            {
                try
                {
                    String classname = "org.color4j.colorimetry.illuminants." + m_Standards[ i ];  //NOI18N
                    Class cls = IlluminantImpl.class.getClassLoader().loadClass( classname );
                    Object obj = cls.newInstance();
                    if( !( obj instanceof Spectrum ) )
                    {
                        throw new IlluminationException( "Standard Illuminant '" + name + "' not known to the system." );   //NOI18N
                    }
                    spectrum = (Spectrum) obj;
                    break;
                }
                catch( IllegalAccessException e )
                {
                    throw new IlluminationException( "Standard Illuminant '" + name + "' not known to the system." );   //NOI18N
                }
                catch( ClassNotFoundException e )
                {
                    throw new IlluminationException( "Standard Illuminant '" + name + "' not known to the system." );   //NOI18N
                }
                catch( InstantiationException e )
                {
                    throw new IlluminationException( "Standard Illuminant '" + name + "' not known to the system." );   //NOI18N
                }
            }
        }
        Illuminant result = new IlluminantImpl( name, spectrum );
        m_Illuminants.put( name, result );
        return result;
    }

    static public Illuminant create( String name, Spectrum spectrum )
        throws IlluminationException
    {
        return new IlluminantImpl( name, spectrum );
    }

    private IlluminantImpl( String name,
                            Spectrum spectrum
    )
        throws IlluminationException
    {
        if( name == null )
        {
            throw new IlluminationException( "Illuminant's Name is required" ); //NOI18N
        }
        if( spectrum == null )
        {
            throw new IlluminationException( "Illuminant's Spectrum is required" ); //NOI18N
        }
        m_Name = name;
        m_Spectrum = spectrum;
    }

    static public String[] getStandardIlluminantNames()
    {
        return m_Standards;
    }

    public String getName()
    {
        return m_Name;
    }

    /**
     * Returns the Power distribution of the light source.
     */
    public Spectrum getSpectrum()
    {
        return m_Spectrum;
    }

    public Class getTypeInterface()
    {
        return Illuminant.class;
    }
}