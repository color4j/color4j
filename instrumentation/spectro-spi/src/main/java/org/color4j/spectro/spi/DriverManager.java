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

import java.util.logging.Logger;

/**
 * Handles the registration and retrieval of Drivers.
 * <p>The DriverManager is only responsible to receive a registration,
 * and upon such, load the <code>Class</code> (optional), instantiate
 * a single object and initialize it. If this is successful, the DriverManager
 * makes the instance available for use, via the <code>getDriver</code>
 * method.</p>
 *
 */
public abstract class DriverManager
{
    static private Logger m_Logger = Logger.getLogger( DriverManager.class.getName() );
    static private DriverManager m_Instance;

    public static DriverManager getInstance()
    {
        synchronized( DriverManager.class )
        {
            if( m_Instance != null )
            {
                return m_Instance;
            }

            String clsname = System.getProperty( "org.color4j.spectro.spi.DriverManager.class", "org.color4j.spectro.spi.DriverManagerImpl" );
            try
            {
                Class cls = DriverManager.class.getClassLoader().loadClass( clsname );
                Object obj = cls.newInstance();
                if( obj instanceof DriverManager )
                {
                    m_Instance = (DriverManager) obj;
                }
                else
                {
                    m_Logger.warning( "Specified DriverManager class '" + clsname + "' is not a subclass of " + DriverManager.class
                        .getName() + "." );
                    m_Instance = new DriverManagerImpl();
                }
            }
            catch( ClassNotFoundException e )
            {
                m_Logger.warning( "Specified DriverManager class '" + clsname + "' not found." );
                m_Instance = new DriverManagerImpl();
            }
            catch( InstantiationException e )
            {
                m_Logger.warning( "Specified DriverManager class '" + clsname + "' is not a concrete class." );
                m_Instance = new DriverManagerImpl();
            }
            catch( IllegalAccessException e )
            {
                m_Logger.warning( "Specified DriverManager class '" + clsname + "' does not have a public default contructor." );
                m_Instance = new DriverManagerImpl();
            }
        }
        return m_Instance;
    }

    public abstract String[] getAllDriverNames();

    public abstract SpectroDriver getDriver( String drivername );

    public abstract void registerDriver( Class cls )
        throws SpectroException;

    public abstract void registerDriver( String classname )
        throws SpectroException;

    public abstract void unregisterDriver( SpectroDriver driver )
        throws SpectroException;
}


