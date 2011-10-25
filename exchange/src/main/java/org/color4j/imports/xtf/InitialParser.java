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
 * GlobalsParser.java
 *
 * Created on May 6, 2003, 1:57 PM
 */

package org.color4j.imports.xtf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.color4j.imports.ini.AbstractSectionParser;
import org.color4j.imports.ini.ParserContext;

/**
 * This class just checks that the first tag is FILE INFO and sets the next parser
 */
public class InitialParser extends AbstractSectionParser
{

    private static final Collection m_header = new ArrayList();

    //must instatiate HEADERS from abstract class
    static
    {
        m_header.add( XTFParserFactory.FILE_INFO );
    }

    /**
     * Creates a new instance of GlobalsParser
     */
    public InitialParser()
    {
    }

    //does not do any processing of non bracket lines
    public void processLine( String str, ParserContext ctx )
    {
    }

    /**
     * after all the lines are processed do something useful with it before moving to next section
     */
    public void postProcess( ParserContext ctx, Map<String, String> attributes, Map cacheKeys )
    {
        //does nothing after reading lines
    }

    /**
     * gets the <code>Collection</code> of <code>String</code>s of the next expected headers. Does not convert for caps
     * will look for indicated header, or proceed to EOF
     *
     * @return next expected String. no <b>null</b>
     */
    public Collection getValidHeaders()
    {
        return m_header;
    }
}
