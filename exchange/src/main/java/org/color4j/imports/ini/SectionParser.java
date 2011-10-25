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
 * LineParser.java
 *
 * Created on May 5, 2003, 2:11 PM
 */

package org.color4j.imports.ini;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import org.color4j.imports.ImportException;

/**
 */
public interface SectionParser
{

    /**
     * <PRE>
     * called by the <code>ParserContext</code> to execute this parser
     * </PRE>
     *
     * <PRE>
     * This class can set which is the next <code>LineParser</code> to be executed
     * </PRE>
     *
     * @param context    is the <code>ParserContext</code> to pass to this class.
     * @param reader     is the <code>Reader</code> containing the character stream to be parsed
     * @param attributes
     *
     * @param cachedKeys
     * @return <i>false</i> if the parsing has reached the end state. ie finished parsing document or an error occured.
     *         Otherwise return <i>true</i>.
     * @throws java.io.IOException
     * @throws org.color4j.imports.ImportException
     */
    //WARNING: can potentially create infinite loop if not handled properly. pls refer to the ParserContext
    // implementation.
    public boolean canProcess( ParserContext context,
                               BufferedReader reader,
                               Map<String, String> attributes,
                               Map cachedKeys
    )
        throws ImportException, IOException;
}
