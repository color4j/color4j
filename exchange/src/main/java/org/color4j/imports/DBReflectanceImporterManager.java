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

package org.color4j.imports;

import java.util.Map;
import java.util.TreeMap;
import org.color4j.imports.mdb.MDBImporter;

/**
 */
public class DBReflectanceImporterManager
{
    private static DBReflectanceImporterManager m_Instance;

    private Map m_Importers;

    private DBReflectanceImporterManager()
    {
        m_Importers = new TreeMap();
        DBReflectanceImporter[] importers = new DBReflectanceImporter[]
            {
                new MDBImporter()
            };
        for( int i = 0; i < importers.length; i++ )
        {
            registerReflectanceImporter( importers[ i ] );
        }
    }

    public static DBReflectanceImporterManager getInstance()
    {
        if( m_Instance == null )
        {
            synchronized( DBReflectanceImporterManager.class )
            {
                if( m_Instance == null )
                {
                    m_Instance = new DBReflectanceImporterManager();
                }
            }
        }
        return m_Instance;
    }

    public String[] getSupportedExtensions()
    {
        if( m_Importers.size() > 0 )
        {
            return (String[]) m_Importers.keySet().toArray( new String[ 0 ] );
        }
        return null;
    }

    public void registerReflectanceImporter( DBReflectanceImporter importer )
    {
        m_Importers.put( importer.getDefaultExtension().toLowerCase(), importer );
    }

    public DBReflectanceImporter getReflectanceImporter( String extension )
    {
        return (DBReflectanceImporter) m_Importers.get( extension.toLowerCase() );
    }
}
