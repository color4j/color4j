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

/*
 * ParserContextImpl.java
 *
 * Created on May 5, 2003, 3:37 PM
 */

package org.color4j.imports.ini;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.imports.AbstractTextFileReflectanceImporter;
import org.color4j.imports.ImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ParserContextImpl extends AbstractTextFileReflectanceImporter
    implements ParserContext
{

    private Map<String,Map<String,String>> m_globals = new HashMap<String,Map<String,String>>();
    private Collection<Reflectance> m_reflectances = new ArrayList<Reflectance>();
    public SectionParser m_currentParser = null;
    private SectionParserFactory m_factory = null;
    private static Logger m_Logger;

    static
    {
        m_Logger = LoggerFactory.getLogger( ParserContextImpl.class );
    }

    /**
     * @return the logger
     */
    public Logger getLogger()
    {
        return m_Logger;
    }

    /**
     * Creates a new instance of ParserContextImpl
     */
    public ParserContextImpl()
    {
        super( "XTF", "xtf" );
    }

    public Reflectance[] doImport( InputStream stream, Map<String, String> attributes )
        throws IOException, ImportException
    {
        //@todo set initial state
        InputStreamReader in = new InputStreamReader( stream );
        BufferedReader reader = new BufferedReader( in );

        HashMap<String, String> cachedKeys = new HashMap<String, String>( attributes );
        while( true )
        {
            if( !( m_currentParser.canProcess( this, reader, attributes, cachedKeys ) ) )
            {
                break;
            }
        }
        reader.close();

        if( m_currentParser instanceof InitialParser )
        {
            throw new ImportException( "Entire document read and invalid" );
        }
        //
        Reflectance[] ret = new Reflectance[ m_reflectances.size() ];
        m_reflectances.toArray( ret );
        return ret;
    }

    /**
     * <PRE>
     * gets the <code>Map</code> of variables that will be used in all instances
     * of the colours to be created.
     * </PRE>
     * <PRE>
     * The map can be used to get variables or set variables into the map
     * </PRE>
     *
     * @return a <code>Map</code> containing key value pairs of <code>String</code>
     */
    public Map<String,Map<String,String>> getGlobals()
    {
        return m_globals;
    }

    public Collection getReflectances()
    {
        return m_reflectances;
    }

    public void setCurrentSectionParser( SectionParser parser )
    {
        m_currentParser = parser;
        //   m_currentParser = (LineParser)m_parsers.get( str );
    }

    /**
     * <PRE>
     * sets the <code>Map</code>. Will replace existing <code>Map</code>.
     * </PRE>
     * <PRE>
     * To add to an existing <code>Map</code> use the <code>getGlobals()</code> and
     * add key values to that map.
     * </PRE>
     *
     * @param m <code>Map</code> containing <code>String</code>s.
     */
    public void setGlobals( Map<String,Map<String,String>> m )
    {
        m_globals = m;
    }

    public void setReflectance( Reflectance refl )
    {
        m_reflectances.add( refl );
    }

    public void setReflectances( Collection<Reflectance> reflectances )
    {
        m_reflectances = reflectances;
    }

    /**
     * <PRE>
     * Sets the active <code>SectionParser</code> to be used by the <code>ParserContext</code>
     * </PRE>
     */
    public void setCurrentSectionParser( String str )
    {
        SectionParser sp = m_factory.getSectionParser( str );
        if( sp != null )
        {
            m_currentParser = sp;
        }
        //@todo need a default handler if the section parser is null
    }

    public void setSectionParserFactory( SectionParserFactory fact )
    {
        m_factory = fact;
    }
}
