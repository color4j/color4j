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
package org.color4j.exports;

import java.util.Map;
import java.util.TreeMap;
import org.color4j.exports.aco.ExporterACO;
import org.color4j.exports.ai.ExporterAI;
import org.color4j.exports.cxf.ExporterCXF;
import org.color4j.exports.delimited.ExporterCMYK;
import org.color4j.exports.delimited.ExporterSRGB;
import org.color4j.exports.delimited.ExporterXYZ_Lab;
import org.color4j.exports.qtx.ExporterBatchedQTX;
import org.color4j.exports.qtx.ExporterQTX;

public class TextFileReflectanceExporterManager
{
    private static TextFileReflectanceExporterManager m_Instance;

    private Map<String, TextFileReflectanceExporter> m_Exporters;

    private TextFileReflectanceExporterManager()
    {
        m_Exporters = new TreeMap<String, TextFileReflectanceExporter>();
        registerReflectanceExporter( new ExporterXYZ_Lab() );
        registerReflectanceExporter( new ExporterACO() );
        registerReflectanceExporter( new ExporterSRGB() );
        registerReflectanceExporter( new ExporterAI() );
        registerReflectanceExporter( new ExporterCXF() );
        registerReflectanceExporter( new ExporterBatchedQTX() );
        registerReflectanceExporter( new ExporterQTX() );
        registerReflectanceExporter( new ExporterCMYK() );
    }

    public static TextFileReflectanceExporterManager getInstance()
    {
        if( m_Instance == null )
        {
            synchronized( TextFileReflectanceExporterManager.class )
            {
                if( m_Instance == null )
                {
                    m_Instance = new TextFileReflectanceExporterManager();
                }
            }
        }
        return m_Instance;
    }

    public String[] getSupportedExtensions()
    {
        return m_Exporters.keySet().toArray( new String[ m_Exporters.keySet().size() ] );
    }

    public void registerReflectanceExporter( TextFileReflectanceExporter exporter )
    {
        m_Exporters.put( exporter.getName(), exporter );
    }

    public TextFileReflectanceExporter getReflectanceExporter( String extension )
    {
        return m_Exporters.get( extension );
    }
}
