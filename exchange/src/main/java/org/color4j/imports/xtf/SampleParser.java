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
 * SampleParser.java
 *
 * Created on May 8, 2003, 11:50 AM
 */

package org.color4j.imports.xtf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.color4j.imports.ImportException;
import org.color4j.imports.ini.AbstractSectionParser;
import org.color4j.imports.ini.ParserContext;

/**
 */
public class SampleParser extends AbstractSectionParser
{
    private static final Collection m_headers = new ArrayList();
    private Map m_standards;        //key is a unique name generated below, value is an initial integer followed by 31 float values
    private Map m_attributes;
    private int m_noteCounter;      //counts Notes created
    private int m_sampleCounter;    //counts times a sample reflectance is created

    static
    {
        m_headers.add( XTFParserFactory.SAMPLE );
    }

    /**
     * Creates a new instance of SampleParser
     */
    public SampleParser()
    {
        initialize();
    }

    private void initialize()
    {
        m_standards = new HashMap();
        m_attributes = new HashMap();
        m_noteCounter = 1;
        m_sampleCounter = 1;
    }

    private void reset()
    {
        if( m_attributes != null )
        {
            m_attributes.clear();
        }
        if( m_standards != null )
        {
            m_standards.clear();
        }

        m_noteCounter = 1;
        m_sampleCounter++;
    }

    /**
     * after all the lines are processed do something useful with it before moving to next section
     */
    public void postProcess( ParserContext ctx, Map<String, String> attributes, Map cacheKeys )
        throws ImportException
    {

        /**StandardParser.setupReflectance() will take the map of standards, find if specular is included or excluded 
         * and then add it to conditions.  when setup is complete, will complete reflectance and addit to the context's collection
         **/
        StandardParser.createReflectance( ctx, m_standards, m_attributes, attributes, cacheKeys );
        reset();   //reinitialize for future use
    }

    /**
     * <PRE>
     * the logic of parsing this states body of key and values
     * </PRE>
     *
     * @throws ImportException if unexpected format found
     */
    public void processLine( String str, ParserContext ctx )
        throws ImportException
    {
        if( str == null )
        {
            //EOF:  Sample Parser is the last expected parser
            m_sampleCounter = 0;
            return;
        }

        if( str.equals( "" ) )
        {
            return;
        }
        int pos = str.indexOf( '=' );
        if( pos < 0 )
        {
            throw new ImportException( "Invalid format:" + str );
        }
        String key = str.substring( 0, pos );
        String value = null;
        if( str.endsWith( "=" ) )
        {
            value = "N/A";
        }
        else
        {
            value = str.substring( pos + 1 );
        }

        //process multiple REFL ... need to rename samples 
//        Map common = (Map)ctx.getGlobals().get( AbstractGlobalsParser.COMMON_MAP );        
//        String angles = (String)common.get( StandardParser.ANGLES_KEY );        

        if( key.equals( StandardParser.REFL_KEY ) )
        {
            StandardParser.processReflectanceName( value, ctx, m_attributes, m_standards, XTFParserFactory.SAMPLE, m_sampleCounter );
        }
        else if( key.equals( StandardParser.NOTES_KEY ) && m_noteCounter++ > 1 )
        {
            m_attributes.put( StandardParser.NOTES_KEY + ( m_noteCounter - 1 ), value );
        }
        else
        {
            m_attributes.put( key, value );
        }
    }

    /**
     * gets the <code>Collection</code> of <code>String</code>s of the next expected headers. Does not convert for caps
     * will look for indicated header, or proceed to EOF
     *
     * @return next expected String. no <b>null</b>
     */
    public Collection getValidHeaders()
    {
        return m_headers;
    }
}
