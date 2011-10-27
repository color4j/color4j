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
 * ParserContext.java
 *
 * Created on May 5, 2003, 12:48 PM
 */

package org.color4j.imports.ini;

import java.util.Collection;
import java.util.Map;
import org.color4j.colorimetry.Reflectance;
import org.slf4j.Logger;

/**
 * This holds the data to be passed to the <code>LineParser</code>s
 */
public interface ParserContext
{

    /**
     * <PRE>
     * gets the <code>Map</code> of variables that will be used in all instances
     * of the colours to be created.
     * </PRE>
     * <PRE>
     * The map can be used to get variables or set variables into the map
     * </PRE>
     *
     * @return a <code>Map</code> containing key value pairs of <code>String</code>
     */
    public Map<String,Map<String,String>> getGlobals();

    /**
     * <PRE>
     * sets the <code>Map</code>. Will replace existing <code>Map</code>.
     * </PRE>
     * <PRE>
     * To add to an existing <code>Map</code> use the <code>getGlobals()</code> and
     * add key values to that map.
     * </PRE>
     *
     * @param m <code>Map</code> containing <code>String</code>s.
     */
    public void setGlobals( Map<String,Map<String,String>> m );

    /**
     * get the <CODE>Collection</CODE> of parsed <CODE>Reflectance</CODE> objects.
     *
     * @return contains <code>Reflectance</code> objects
     */
    public Collection<Reflectance> getReflectances();

    /**
     * adds a <CODE>Collection</CODE> of <CODE>Reflectance</CODE> objects.
     * data.
     *
     * @param reflectances The reflectances created from the parsed data.
     */
    public void setReflectances( Collection<Reflectance> reflectances );

    /**
     * Adds a <CODE>Reflectance</CODE> to the collection of reflectance object.
     *
     * @param refl The reflectance object.
     */
    public void setReflectance( Reflectance refl );

    /**
     * Sets the next <code>LineParser</CODE> to be used by the
     * <CODE>ParserContext</CODE>.
     * Used as callback method by <CODE>LineParser</CODE>
     *
     * @param parser sets the next current LineParser to be used.
     *
     * @ deprecated use the <code>setCurrentSectionParser9 String str )</code> instead
     */
    public void setCurrentSectionParser( SectionParser parser );

    /**
     * <PRE>
     * Sets the active <code>SectionParser</code> to be used by the <code>ParserContext</code>
     * </PRE>
     */
    public void setCurrentSectionParser( String str );

    /**
     * @param fact the <code>SectionFactory</code> to be used
     */
    public void setSectionParserFactory( SectionParserFactory fact );

    /**
     * @return a log4j logger
     */
    public Logger getLogger();
}
