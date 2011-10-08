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

package org.color4j.spectro.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DriverManagerImpl extends DriverManager
{
    final private Map m_Drivers;

    public DriverManagerImpl()
    {
        m_Drivers = new HashMap();
    }

    public String[] getAllDriverNames()
    {
        synchronized( m_Drivers )
        {
            String[] result = new String[ m_Drivers.size() ];
            Iterator list = m_Drivers.keySet().iterator();
            for( int i = 0; list.hasNext(); i++ )
            {
                result[ i ] = (String) list.next();
            }
            return result;
        }
    }

    public SpectroDriver getDriver( String name )
    {
        return (SpectroDriver) m_Drivers.get( name );
    }

    public void registerDriver( Class cls )
        throws SpectroException
    {
        try
        {
            Object obj = cls.newInstance();
            if( obj instanceof SpectroDriver )
            {
                synchronized( m_Drivers )
                {
                    SpectroDriver driver = (SpectroDriver) obj;
                    driver.initialize();
                    m_Drivers.put( driver.getName(), driver );
                }
            }
            else
            {
                throw new SpectroException( "Driver class '" +
                                            cls.getName() +
                                            "' is not an implementation of org.color4j.spectro.spi.SpectroDriver." );
            }
        }
        catch( InstantiationException e )
        {
            throw new SpectroException( "Driver class '" + cls.getName() + "' is not a concrete class." );
        }
        catch( IllegalAccessException e )
        {
            String mess = "Driver class '" + cls.getName() + "' does not have a public default constructor.";
            throw new SpectroException( mess );
        }
    }

    public void registerDriver( String classname )
        throws SpectroException
    {
        try
        {
            Class cls = this.getClass().getClassLoader().loadClass( classname );
            registerDriver( cls );
        }
        catch( ClassNotFoundException e )
        {
            throw new SpectroException( "Driver class '" + classname + "' not found." );
        }
    }

    public void unregisterDriver( SpectroDriver driver )
        throws SpectroException
    {
        synchronized( m_Drivers )
        {
            String name = driver.getName();
            driver.dispose();
            m_Drivers.remove( name );
        }
    }
}
