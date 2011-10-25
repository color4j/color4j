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
 * CustomerParser.java
 *
 * Created on May 7, 2003, 3:04 PM
 */

package org.color4j.imports.xtf;

import java.util.ArrayList;
import java.util.Collection;
import org.color4j.imports.ini.AbstractGlobalsParser;

/**
 */
public class CustomerParser extends AbstractGlobalsParser
{

    private static final Collection m_headers = new ArrayList();

    //must instatiate HEADERS from abstract class
    static
    {
        m_headers.add( XTFParserFactory.STANDARD );
    }

    /**
     * Creates a new instance of CustomerParser
     */
    public CustomerParser()
    {
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
