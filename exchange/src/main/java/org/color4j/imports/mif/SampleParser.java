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
 * SampleParser.java
 *
 * Created on May 12, 2003, 4:25 PM
 */

package org.color4j.imports.mif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.color4j.imports.ImportException;
import org.color4j.imports.ini.AbstractSectionParser;
import org.color4j.imports.ini.ParserContext;

/**
 * <PRE>
 * Notes on tags to be found in the SAMPLE section of a MIF file.
 * <UL>
 * <LI>CUST This value must match the value for IDNT adn is shwn in the Edit Customer dialog in Ink Master.
 * Stored as a reflectance attrib.</LI>
 * <LI> NAME This is the name of the <b>standard</b></LI>
 * <LI> LOT This is the LOT ID of the sample</LI>
 * <LI> RJCT This is 2 for accepted, 1 for rejected and 0 for undetermined</LI>
 * <LI> CTYPE This is T for trial, M for match</LI>
 * <LI> SUBST This is the substrate used</LI>
 * <LI> ITEM A database reference of the inks or colorants used</LI>
 * <LI> PNAME The first instance of this is the calibration set used</LI>
 * <LI> FPERC The first instance of this is the percent per volume of extender in the formula.</LI>
 * <LI>  PNAME This is the first ink name used</LI>
 * <LI> FPERC This is the percent per volume of the above listed ink </LI>
 * <LI> Etc. Colorants, vehicles and resins are presented by the percents per weight.
 * <LI> This line (and others that have the same NOTE beginning) is the note associated with the sample.
 * Inclusion of the notes in the file is optional. </LI>
 * </UL>
 * </PRE>
 */
public class SampleParser extends AbstractSectionParser
{

    public static final char ENTRY_SEPARATOR = '=';
    // list of tags specific to SAMPLE
    public static final String LOT = "LOT";
    public static final String RJCT = "RJCT";
    public static final String CTYPE = "CTYPE";
    public static final String ITEM = "ITEM";
    public static final String SUBST = "SUBST";
    public static final String PNAME = "PNAME";
    public static final String FPERC = "FPERC";
    public static final String NOTE = "NOTE";
    public static final String TAG = "TAG";
    public static final String NAME = "NAME";
    public static final String CUSTOMER = "CUSTOMER";

    private static final Collection<String> m_headers = new ArrayList<String>();

    private String m_name;
    private String m_customer;
    private String m_lot;
    private String m_reject;
    private String m_ctype;
    private String m_subst;
    private String m_item;
    private List<String> m_notes;
    //FNAME and PFERC tags
    private Map<String, String> m_entries;
    // used to suffix tags
    private int m_index = 0;
    private int m_sampleCounter = 0;

    static
    {
        m_headers.add( MIFParserFactory.MEASUREMENT );
    }

    /**
     * Creates a new instance of SampleParser
     */
    public SampleParser()
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
     */
    public void postProcess( ParserContext ctx, Map<String, String> attributes, Map cacheKeys )
        throws ImportException
    {

        Map<String, String> m = getMapFrom( "colorinfo", ctx );
        // clears the colorinfo map
        // cmmon information that will be used in all reflectances
        // should be kept in the common map
        m.clear();
        fillMap( m );

        // multiple notes
        duplicateKeyMapping( m, m_notes, "NOTE" );
        // ctx.getLogger().info( "After change SampleParser.postPrcoess.colorinfo " + m.toString() );
        if( m_entries != null && m_entries.size() > 0 )
        {
            m.putAll( m_entries );
        }
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
        if( str == null )
        {
            //means EOF
            m_sampleCounter = 0;
            return;
        }
        if( str.equals( "" ) )
        {
            return;
        }

        char ch = str.charAt( 0 );
        if( ch == 'L' )
        {
            m_lot = super.getAttribute( str, ENTRY_SEPARATOR )[ 1 ];
        }
        if( ch == 'R' )
        {
            m_reject = super.getAttribute( str, ENTRY_SEPARATOR )[ 1 ];
        }
        if( ch == 'S' )
        {
            m_subst = super.getAttribute( str, ENTRY_SEPARATOR )[ 1 ];
        }
        if( ch == 'I' )
        {
            m_item = super.getAttribute( str, ENTRY_SEPARATOR )[ 1 ];
        }
        if( ch == 'P' )
        {
            String key = PNAME;
            String value = super.getAttribute( str, ENTRY_SEPARATOR )[ 1 ];
            handleTags( key, value, m_index );
        }
        if( ch == 'F' )
        {
            String key = FPERC;
            String value = super.getAttribute( str, ENTRY_SEPARATOR )[ 1 ];
            handleTags( key, value, m_index );
            m_index++;
        }

        if( ch == 'C' )
        {
            if( str.charAt( 1 ) == 'U' )
            {
                m_customer = super.getAttribute( str, ENTRY_SEPARATOR )[ 1 ];
            }
            else
            {
                m_ctype = super.getAttribute( str, ENTRY_SEPARATOR )[ 1 ];
            }
        }
        if( ch == 'N' )
        {
            if( str.charAt( 1 ) == 'A' )
            {
                m_name = super.getAttribute( str, ENTRY_SEPARATOR )[ 1 ] + "_" + MIFParserFactory.SAMPLE + "_" + m_sampleCounter;
            }
            else
            {
                //add to notes
                getNotes().add( super.getAttribute( str, ENTRY_SEPARATOR )[ 1 ] );
            }
        }
    }

    private synchronized List<String> getNotes()
    {
        if( m_notes == null )
        {
            m_notes = new ArrayList<String>();
        }
        return m_notes;
    }

    private synchronized Map<String, String> getEntries()
    {
        if( m_entries == null )
        {
            m_entries = new TreeMap<String, String>();
        }
        return m_entries;
    }

    private void reset()
    {
        m_name = null;
        m_customer = null;
        m_lot = null;
        m_reject = null;
        m_ctype = null;
        m_subst = null;
        m_item = null;
        m_index = 0;
        if( m_notes != null )
        {
            m_notes.clear();
        }
        if( m_entries != null )
        {
            m_entries.clear();
        }
        m_sampleCounter++;
    }

    // returns a map fromthe context.getGlboal() method
    // if no map exists, create one and put it in
    private Map<String, String> getMapFrom( String mapName, ParserContext ctx )
    {
        Map<String, String> m = ctx.getGlobals().get( mapName );
        //ctx.getLogger().info( "SampleParser.postPrcoess.colorinfo " +m.toString() );
        if( m == null )
        {
            m = new HashMap<String, String>();
            ctx.getGlobals().put( mapName, m );
        }
        return m;
    }

    private void fillMap( Map<String, String> m )
    {
        if( m_name != null )
        {
            m.put( NAME, m_name + "_" );
        }
        if( m_ctype != null )
        {
            m.put( CTYPE, m_ctype );
        }
        if( m_customer != null )
        {
            m.put( CUSTOMER, m_customer );
        }
        if( m_item != null )
        {
            m.put( ITEM, m_item );
        }
    }

    private void handleTags( String key, String value, int index )
    {
        if( value != null )
        {
            if( index > 0 )
            {
                key = key + "_" + index;
            }
            getEntries().put( key, value );
        }
    }
}
