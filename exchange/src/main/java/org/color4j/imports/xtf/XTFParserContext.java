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
 * XTFParserContext.java
 *
 * Created on May 20, 2003, 7:13 PM
 */

package org.color4j.imports.xtf;

import org.color4j.imports.ini.AbstractParserContextImpl;
import org.color4j.imports.ini.SectionParserFactory;

/**
 * AbstractParserContextImpl implements ReflectanceImporter
 */
public class XTFParserContext extends AbstractParserContextImpl
{
    private static final SectionParserFactory m_factory = new XTFParserFactory();

    /**
     * Creates a new instance of XTFParserContext
     */
    public XTFParserContext()
    {
        super( "XTF", "xtf" );
    }

    /**
     * @return the <code>String</code> mapped to the first <code>SectionParser</code> to be called.
     */
    public String getInitialSectionParser()
    {
        return XTFParserFactory.INIT;
    }

    /**
     * @return <code>SectionParserFactory</code> implementation has to be returned here
     */
    public SectionParserFactory getSectionParserFactory()
    {
        return m_factory;
    }
}
