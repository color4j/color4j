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
 * XTFSectionParserFactory.java
 *
 * Created on May 20, 2003, 7:04 PM
 */

package org.color4j.imports.xtf;

import java.util.HashMap;
import java.util.Map;
import org.color4j.imports.ini.SectionParser;
import org.color4j.imports.ini.SectionParserFactory;

/**
 *
 */
public class XTFParserFactory implements SectionParserFactory
{

    private final static Map m_parsers = new HashMap();

    public static final String INIT = "INIT";
    public static final String STANDARD = "STANDARD";
    public static final String FILE_INFO = "FILE INFO";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String SAMPLE = "SAMPLE";
    public static final String ALT_STD = "ALT STANDARD";

    static
    {
        m_parsers.put( INIT, new InitialParser() );
        m_parsers.put( CUSTOMER, new CustomerParser() );
        m_parsers.put( FILE_INFO, new FileInfoParser() );
        m_parsers.put( CUSTOMER, new CustomerParser() );
        m_parsers.put( SAMPLE, new SampleParser() );
        m_parsers.put( STANDARD, new StandardParser() );
        m_parsers.put( ALT_STD, new AltStandardParser() );
    }

    /**
     * Creates a new instance of XTFSectionParserFactory
     */
    public XTFParserFactory()
    {
    }

    /**
     * gets the <code>SectionParser</code> mapped to the <code>String</code>
     *
     * @param parser of parser to be returned
     *
     * @return the Parser or <code>null</code> if no parser is found
     */
    public SectionParser getSectionParser( String parser )
    {
        return (SectionParser) m_parsers.get( parser );
    }
}
