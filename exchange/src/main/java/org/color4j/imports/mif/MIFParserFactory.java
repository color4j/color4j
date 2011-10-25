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
 * MIFPaserFactory.java
 *
 * Created on May 8, 2003, 3:46 PM
 */

package org.color4j.imports.mif;

import java.util.HashMap;
import java.util.Map;
import org.color4j.imports.ini.AbstractSectionParser;
import org.color4j.imports.ini.SectionParser;

/**
 *
 */
public class MIFParserFactory implements org.color4j.imports.ini.SectionParserFactory
{

    public final static String INIT = "INIT";
    /*
     * valid tag names in an .MIF file
     */
    public final static String FILE_INFO = "FILE INFO";
    public final static String COLORANT = "COLORANT";
    public final static String CUSTOMER = "CUSTOMER";
    public final static String STANDARD = "STANDARD";
    public final static String ALT_STANDARD = "ALT STANDARD";
    public final static String SAMPLE = "SAMPLE";
    public final static String MEASUREMENT = "MEASUREMENT";

    private final static Map<String, SectionParser> m_parsers = new HashMap<String, SectionParser>();

    static
    {
        StandardParser o = new StandardParser();

        m_parsers.put( FILE_INFO, new FileInfoParser() );
        m_parsers.put( CUSTOMER, new CustomerParser() );
        m_parsers.put( COLORANT, new ColorantParser() );
        m_parsers.put( INIT, new InitParser() );
        m_parsers.put( MEASUREMENT, new MeasurementParser() );
        m_parsers.put( STANDARD, o );
        m_parsers.put( ALT_STANDARD, o );
        m_parsers.put( SAMPLE, new SampleParser() );
    }

    /**
     * Creates a new instance of MIFPaserFactory
     */
    public MIFParserFactory()
    {
    }

    /**
     * gets the <code>SectionParser</code> mapped to the <code>String</code>
     *
     * @param parser name of parser to be returned
     *
     * @return the Parser or <code>null</code> if no parser is found
     */
    public SectionParser getSectionParser( String parser )
    {
        return m_parsers.get( parser );
    }
}
