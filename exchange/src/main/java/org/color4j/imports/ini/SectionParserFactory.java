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
 * SectionParserFactory.java
 *
 * Created on May 8, 2003, 2:25 PM
 */

package org.color4j.imports.ini;

/**
 *
 */
public interface SectionParserFactory
{
    public final static SectionParser ERRORPARSER = new ErrorParserImpl();

    /**
     * gets the <code>SectionParser</code> mapped to the <code>String</code>
     *
     * @param parser of parser to be returned
     *
     * @return the Parser or <code>null</code> if no parser is found
     */
    public SectionParser getSectionParser( String parser );
}
