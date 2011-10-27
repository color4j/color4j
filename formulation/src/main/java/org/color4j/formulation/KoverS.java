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

package org.color4j.formulation;

import org.color4j.colorimetry.Spectrum;

public class KoverS
{
    //@TODO: Clean up. ML - 05/08/2003
//    private Spectrum m_ReflectanceSpectrum; //Cached value
    private Spectrum m_Spectrum;

    public static KoverS create( Spectrum spectrum )
    {
        return new KoverS( spectrum );
    }

    private KoverS( Spectrum spectrum )
    {
        m_Spectrum = spectrum;
    }

    public Spectrum getSpectrum()
    {
        return m_Spectrum;
    }

    public KoverS add( KoverS term )
    {
        return null;
/*  Old code. New way can not deal with Maps and have to handle arrays properly
        TreeMap newKS = new TreeMap();

        Iterator keys = m_Spectrum.getPowerReadings().keySet().iterator();

        while( keys.hasNext() )
        {
            Object key = keys.next();
            Float value = new Float( ( ( Number )m_Spectrum.getPowerReadings().get( key ) ).doubleValue() +
                                   ( ( Number )term.getSpectrum().getPowerReadings().get( key ) ).doubleValue() );

            newKS.put( key, value );
        }
        return create( Spectrum.create( newKS ) );
*/
    }

    public KoverS subtract( KoverS term )
    {
        return null;
/*
        TreeMap newKS = new TreeMap();

        Iterator keys = m_Spectrum.getPowerReadings().keySet().iterator();

        while( keys.hasNext() )
        {
            Object key = keys.next();
            Float value = new Float( ( ( Number )m_Spectrum.getPowerReadings().get( key ) ).doubleValue() -
                                   ( ( Number )term.getSpectrum().getPowerReadings().get( key ) ).doubleValue() );

            newKS.put( key, value );
        }
        return create( Spectrum.create( newKS ) );
*/
    }

    public Spectrum getReflectanceSpectrum()
    {
        return null;
/*
        if( m_ReflectanceSpectrum != null )
            return m_ReflectanceSpectrum;

        TreeMap reflSpectrum = new TreeMap();

        Iterator keys = m_Spectrum.getPowerReadings().keySet().iterator();

        while( keys.hasNext() )
        {
            Object key = keys.next();
            double value = ( ( Number )m_Spectrum.getPowerReadings().get( key ) ).doubleValue();

            reflSpectrum.put( key, convertToReflectance( value ) );
        }
        m_ReflectanceSpectrum = Spectrum.create( reflSpectrum );
        return m_ReflectanceSpectrum;
*/
    }

    public double convertToReflectance( double ks )
    {
        return 1 + ks - Math.sqrt( ks * ( ks + 2 ) );
    }
}

