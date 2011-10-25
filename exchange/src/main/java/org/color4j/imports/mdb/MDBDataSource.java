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

package org.color4j.imports.mdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 */
public class MDBDataSource
{
    protected Connection m_conn;

    private static Throwable m_thr;

    static
    {
        try
        {
            Class.forName( "sun.jdbc.odbc.JdbcOdbcDriver" );
        }
        catch( ClassNotFoundException e )
        {
            m_thr = e;
        }
    }

    protected String buildURL( String dbpath )
    {
        return "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + dbpath;
    }

    public MDBDataSource( String dbpath )
        throws ClassNotFoundException, SQLException
    {
        if( m_thr != null )
        {
            throw (ClassNotFoundException) m_thr;
        }

        m_conn = DriverManager.getConnection( buildURL( dbpath ) );
        m_conn.setReadOnly( true );
    }

    public MDBDataSource( String dbpath, Properties info )
        throws ClassNotFoundException, SQLException
    {
        if( m_thr != null )
        {
            throw (ClassNotFoundException) m_thr;
        }

        m_conn = DriverManager.getConnection( buildURL( dbpath ), info );
        m_conn.setReadOnly( true );
    }

    public MDBDataSource( String dbpath, String username, String password )
        throws ClassNotFoundException, SQLException
    {
        if( m_thr != null )
        {
            throw (ClassNotFoundException) m_thr;
        }

        m_conn = DriverManager.getConnection( buildURL( dbpath ), username, password );
        m_conn.setReadOnly( true );
    }

    public ColorDBExtractor getColorDBExtractor()
    {
        return new SimpleColorDBExtractor( m_conn );
    }

    public Connection getConnection()
    {
        return m_conn;
    }

    public void close()
    {
        try
        {
            if( m_conn != null && !m_conn.isClosed() )
            {
                m_conn.close();
            }
        }
        catch( SQLException e )
        {
        }
    }
}
