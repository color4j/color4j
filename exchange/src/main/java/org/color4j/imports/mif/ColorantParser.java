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
 * ColorantParser.java
 *
 * Created on May 16, 2003, 6:01 PM
 */

package org.color4j.imports.mif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.color4j.imports.ImportException;
import org.color4j.imports.ini.AbstractGlobalsParser;
import org.color4j.imports.ini.AbstractSectionParser;
import org.color4j.imports.ini.ParserContext;

/**
 * <PRE>The line with the 'V' indicate thr calibration set used.
 * The following lines with the 'P' represent the innkds or colorants used in the formula.
 * First item of each line is the database reference of the calibration set, ink or colorant used.
 * The second item is the value used by the database to identify the company. The next item is the colorant type.
 * It is always 'C' for the calibration set, 'P' for the colorants, 'B' for the black colorant, 'N' for the white colorant,
 * 'J' for the waste colorant. The last item is the description of the calibration set, ink or colorant used.
 * </PRE>
 */
public class ColorantParser extends AbstractSectionParser
{

    private String m_V;
    private List<String> m_P;

    private final static Collection<String> m_headers = new ArrayList<String>();
    private final static String V = "v";

    static
    {
        m_headers.add( MIFParserFactory.CUSTOMER );
    }

    /**
     * Creates a new instance of ColorantParser
     */
    public ColorantParser()
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
            throw new ImportException( "Expected values got null" );
        }
        if( str.charAt( 0 ) == 'V' )
        {
            m_V = this.getAttribute( str, '=' )[ 1 ];
        }
        if( str.charAt( 0 ) == 'P' )
        {
            getList().add( this.getAttribute( str, '=' )[ 1 ] );
        }
    }

    private void reset()
    {
        m_V = null;
        if( m_P != null )
        {
            m_P.clear();
        }
    }

    private List<String> getList()
    {
        if( m_P == null )
        {
            m_P = new ArrayList<String>();
        }
        return m_P;
    }

    /**
     * after all the lines are processed do something useful with it before moving to next section
     */
    public void postProcess( ParserContext ctx, Map<String, String> attributes, Map cacheKeys )
        throws ImportException
    {
        Map<String, String> m = (Map<String, String>) ctx.getGlobals().get( AbstractGlobalsParser.COMMON_MAP );
        m.put( V, m_V );
        duplicateKeyMapping( m, m_P, "P" );
        reset();
    }
}
