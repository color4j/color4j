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
 * StandardParser.java
 *
 * Created on May 12, 2003, 3:40 PM
 */

package org.color4j.imports.mif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.color4j.imports.ImportException;
import org.color4j.imports.ini.AbstractSectionParser;
import org.color4j.imports.ini.ParserContext;

/**
 * <PRE>
 * <UL>
 * <LI> CUST This value must match the value for IDNT . Stored as attribute in Reflectance attribute </LI>
 * <LI> NAME This is the name of the standard. Use as the name of the reflectance</LI>
 * <LI> DESC This is the description of the standard. Used as the description of the reflectance</LI>
 * <LI> <P>TOLR This line ( and others that have the same TOLR beginning ) is the tolerance associated with
 * with the standard.</P>
 * <P> The first item is the angle: 15, 25, 45, 75, 110, In, Ex </P>
 * <P>The second item is the tolerance type: Lab, LCH, CMC, DE, MET, STR, YEL, WHI, BRT, OFFSET_LAB, DE_(FMC2), DEN, DIN6172, CIE94, DE94</P>
 * <P>
 * The third item is either the illuminant/observer ( primary and secondary - if any ) or the tolerance subtyoe (if any ):
 * ASTM_E313, ASTM_D1925, Apparent, Tristimulus, Chromatic, TAPPI_452, CIE ,STATUS_A ,STATUS_E,
 * STATUS_I, STATUS_T, SPECTRAL, SPECTRAL_X, HIFI, HEXACHROME.
 * These are followed by the tolerance values.
 * </P>
 * </LI>
 * <LI> NOTE This line (and others that have the same NOTE begining) is the note associated with the standard. Inclusion of the
 * notes is optional.</LI>
 * </UL>
 * </PRE>
 */
public class StandardParser extends AbstractSectionParser
{
    private static final Collection<String> m_headers = new ArrayList<String>();

    public static final String NAME = "NAME";
    public static final String CUST = "CUST";
    public static final String DESC = "DESC";

    private String m_customer;
    private String m_name;
    private String m_desc;
    private List<String> m_notes;
    private List<String> m_tol;

    static
    {
        m_headers.add( MIFParserFactory.MEASUREMENT );
    }

    /**
     * Creates a new instance of StandardParser
     */
    public StandardParser()
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
     * after all the lines are processed do something useful with it before moving to next section
     *
     * @param ctx
     * @param attributes
     */
    public void postProcess( ParserContext ctx, Map<String, String> attributes, Map cacheKeys )
        throws ImportException
    {
        // get the color info map from the ctx
        Map<String, String> m = ctx.getGlobals().get( "colorinfo" );
        if( m == null )
        {
            m = new HashMap<String, String>();
            ctx.getGlobals().put( "colorinfo", m );
        }
        m.clear();

        // add this info to the map
        m.put( NAME, m_name );
        m.put( DESC, m_desc );
        m.put( CUST, m_customer );

        // multiple notes
        duplicateKeyMapping( m, m_notes, "NOTE" );

        //multiple TOL tags
        duplicateKeyMapping( m, m_tol, "TOL" );

        reset();
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
        if( str == null ) //MeasurementParser should be the only parser allowed to read an EOF or null
        {
            throw new ImportException( "Expected a string, found null" );
        }

        if( str.equals( "" ) )
        {
            return;
        }

        char ch = str.charAt( 0 );
        if( ch == 'C' )
        {
            //set customer
            String[] pair = super.getAttribute( str, '=' );
            m_customer = pair[ 1 ];
        }
        if( ch == 'N' )
        {
            if( str.charAt( 1 ) == 'A' )
            {
                // set name
                String[] pair = super.getAttribute( str, '=' );
                m_name = pair[ 1 ] /* + "_" + MIFParserFactory.STANDARD */;
            }
            else
            {
                //set note
                String[] pair = super.getAttribute( str, '=' );
                if( m_notes == null )
                {
                    m_notes = new ArrayList<String>();
                }
                m_notes.add( pair[ 1 ] );
            }
        }
        if( ch == 'D' )
        {
            // set desc
            String[] pair = super.getAttribute( str, '=' );
            m_desc = pair[ 1 ];
        }

        if( ch == 'T' )
        {
            //set tolerance
            String[] pair = super.getAttribute( str, '=' );
            if( m_tol == null )
            {
                m_tol = new ArrayList<String>();
            }
            m_tol.add( pair[ 1 ] );
        }
    }

    //sets the member variables to null so that this instance can be reused.
    private void reset()
    {
        m_customer = null;
        m_desc = null;
        m_name = null;
        if( m_notes != null )
        {
            m_notes.clear();
        }
        if( m_tol != null )
        {
            m_tol.clear();
        }
    }

    protected void duplicateKeyMapping( Map<String,String> m, List<String> list, String tag )
    {
        if( list != null && list.size() > 0 )
        {
//            int noteSize = list.size();
            Iterator<String> it = list.iterator();
            //first note tag does not have suffix
            String value = it.next();
            if( value != null )
            {
                m.put( tag, value );
            }
            tag = tag.concat( "_" );
            for( int i = 1; it.hasNext(); i++ )
            {
                String value1 = it.next();
                m.put( tag.concat( String.valueOf( i ) ), value1 );
            }
        }
    }
}
