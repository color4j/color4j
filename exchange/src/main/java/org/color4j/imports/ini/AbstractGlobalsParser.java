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
 * AbstractGlobalsParser.java
 *
 * Created on May 8, 2003, 4:41 PM
 */

package org.color4j.imports.ini;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.color4j.imports.ImportException;

/**
 * <PRE>
 * This class will read a Section Body and copy the key values into the Global map of a the
 * <code>PaserContext</code>. The map can be referenced by this.COMMON_MAP
 * </PRE>
 */
public abstract class AbstractGlobalsParser extends AbstractSectionParser
{

    public static final String COMMON_MAP = "common_map";

    /**
     * gets the <code>Collection</code> of <code>String</code>s of the next expected headers. Does not convert for caps
     * will look for indicated header, or proceed to EOF
     *
     * @return next expected String. no <b>null</b>
     */
    public abstract Collection getValidHeaders();

    /**
     * after all the lines are processed do something useful with it before moving to next section
     */
    public void postProcess( ParserContext ctx, Map<String, String> attributes, Map cacheKeys )
        throws ImportException
    {
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
        if( str == null || str.equals( "" ) )
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
        getCommonMap( ctx ).put( key, value );
    }

    // In the ctx there can or should be a Map in the global Map which keys key values for all 
    // common key values not related to colour details name, desc. ie these common values will be put int the 
    // properties of the Reflectance.
    //rationale originally it was thought that global context will handle this but the globla map now 
    // also stores non String as well.
    private Map<String,String> getCommonMap( ParserContext ctx )
    {
        Map<String,String> ret = (Map<String,String>) ctx.getGlobals().get( COMMON_MAP );
        if( ret == null )
        {
            ret = new HashMap();
            ctx.getGlobals().put( COMMON_MAP, ret );
        }
        return ret;
    }
}
