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
 * FileInfoParser.java
 *
 * Created on May 7, 2003, 2:59 PM
 */

package org.color4j.imports.xtf;

import java.util.ArrayList;
import java.util.Collection;
import org.color4j.imports.ini.AbstractGlobalsParser;

/**
 */
public class FileInfoParser extends AbstractGlobalsParser
{

    private static final Collection m_header = new ArrayList();

    //must instatiate HEADERS from abstract class
    static
    {
        m_header.add( XTFParserFactory.CUSTOMER );
    }

    /**
     * Creates a new instance of FileInfoParser
     */
    public FileInfoParser()
    {

    }

    public Collection getValidHeaders()
    {
        return m_header;
    }
}
