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
* MeasurementParser.java
*
* Created on May 12, 2003, 12:46 PM
*/

package org.color4j.imports.mif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.ReflectanceImpl;
import org.color4j.colorimetry.Spectrum;
import org.color4j.imports.ImportException;
import org.color4j.imports.ini.AbstractGlobalsParser;
import org.color4j.imports.ini.AbstractSectionParser;
import org.color4j.imports.ini.ParserContext;

/**
 * <PRE>
 * This parses the measurement section. we are only interested in the following tags:
 * <UL>
 * <LI>TIME <i> unknown format</i></LI>
 * <LI>TYPE either S for sample or T for sample ( w or w/o formula) </LI>
 * <LI>AVGS This is the numner of readings </LI>
 * <LI>INST this is the instrument to do the measurement. It can be zero </LI>
 * <LI>ANGL </LI> This is the relfectance data. The first item is the angle followed by the % of reflectance for each
 * wavelength. This starts with a wavelength of 400nm and goes to 700nm in 10 nm increments.
 * In the case of a single angle instrument the data file would contain two ANGL entries, one for 'In' and one for 'Ex'.
 * In the case of multiple angle instrument the data file would contain three or five ANGL entries one for each angle. </LI>
 * <LI>TAGS Inclusion of tag(s) is optional </LI>
 * </UL>
 * Other keys are assumed to have zero as values.
 * </PRE>
 * <PRE>
 * Note this class can be used more than once to parse multiple measurement sections therefore the
 * member variables are cleared after usage.
 * </PRE>
 */
public class MeasurementParser extends AbstractSectionParser
{
    private static final Collection<String> m_headers = new ArrayList<String>();
    private static final Map<String, String> m_conditions = new HashMap<String, String>();

    public static final int REFLLOW = 400;
    public static final int INTERVAL = 10;
    public static final String ANGL = "ANGL";
    public static final String LAST_PARSER = "PREV";
    public static final String IN = "In";
    public static final String EX = "Ex";
    public static final String INEX = IN + EX;

    private String m_type;
    private String m_time;
    private String m_inst;
    private String m_avg;
    private List<String> m_samples;
    private List<String> m_tags;

    static
    {
        m_headers.add( MIFParserFactory.STANDARD );
        m_headers.add( MIFParserFactory.SAMPLE );
        m_headers.add( MIFParserFactory.ALT_STANDARD );

        m_conditions.put( Reflectance.CONDITION_MODE, "Reflectance" );
        m_conditions.put( Reflectance.CONDITION_LIGHTFILTER, "UV Inc" );
        m_conditions.put( Reflectance.CONDITION_APERTURE, "MAV" );
    }

    /**
     * Creates a new instance of MeasurementParser
     */
    public MeasurementParser()
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
        //make the reflectance data and store it into the context
        //duplicateKeyMapping( (Map)ctx.getGlobals().get( "colorinfo" ), m_tags, "TAG" );
        makeReflectance( ctx );
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
        if( str == null || str.equals( "" ) )
        {
            return;
        }
        //I think using charAt() is faster than trying to compare substrings?? FH May12-2003
        char ch = str.charAt( 0 );
        if( ch == 'I' )
        {
            //set instrument
            String[] pair = super.getAttribute( str, '=' );
            if( pair[ 1 ] != null )
            {
                m_inst = pair[ 1 ];
            }
        }
        if( ch == 'A' )
        {
            if( str.charAt( 1 ) == 'V' )
            {
                //set avg
                String[] pair = super.getAttribute( str, '=' );
                if( pair[ 1 ] != null )
                {
                    m_avg = pair[ 1 ];
                }
            }
            if( str.charAt( 1 ) == 'N' )
            {
                // set values
                String[] pair = super.getAttribute( str, '=' );
                if( pair[ 1 ] != null )
                {
                    getSamples().add( pair[ 1 ] );
                }
            }
        }
        if( ch == 'T' )
        {
            if( str.charAt( 1 ) == 'A' )
            {
                //set tags
                String[] pair = super.getAttribute( str, '=' );
                if( pair[ 1 ] != null )
                {
                    getTags().add( pair[ 1 ] );
                }
            }
            if( str.charAt( 1 ) == 'Y' )
            {
                //set type
                String[] pair = super.getAttribute( str, '=' );
                if( pair[ 1 ] != null )
                {
                    m_type = pair[ 1 ];
                }
            }

            if( str.charAt( 1 ) == 'I' )
            {
                //set time
                String[] pair = super.getAttribute( str, '=' );
                if( pair[ 1 ] != null )
                {
                    m_time = pair[ 1 ];
                }
            }
        }
    }

    //@todo reset the member variabels so that this instance can be used again.
    private void reset()
    {
        if( m_samples != null )
        {
            m_samples.clear();
        }
        if( m_tags != null )
        {
            m_tags.clear();
        }
        m_avg = null;
        m_inst = null;
        m_time = null;
        m_type = null;
    }

    private void makeReflectance( ParserContext ctx )
    {
        Reflectance ret;
        //for each entry in the refl list
        Map<String, String> m = (Map<String, String>) ctx.getGlobals().get( "colorinfo" );
        ctx.getLogger().info( "Measurement.makereflectance.colorinfo " + m );
        Map<String, String> common = (Map<String, String>) ctx.getGlobals().get( AbstractGlobalsParser.COMMON_MAP );
        String name = m.remove( StandardParser.NAME );
        if( name == null )
        {
            name = "Map " + ctx.getGlobals().toString();
        }
        String desc = m.remove( StandardParser.DESC );

        String angle = common.get( FileInfoParser.ANGLE );
        for( String refl : m_samples )
        {
            String[] values = refl.split( "\\s" );
            String newName = name /* + "_" + values[ 0 ] */;
            float[] readings = getReadings( values );
            Spectrum spec = Spectrum.create( REFLLOW, INTERVAL, readings );
            if( angle.equals( INEX ) && values[ 0 ].equals( IN ) )
            {
                m_conditions.put( Reflectance.CONDITION_SPECULAR, "SCI" );
            }
            else
            {
                m_conditions.put( Reflectance.CONDITION_SPECULAR, "SCE" );
            }

            ret = ReflectanceImpl.create( spec, m_conditions );
            m_conditions.remove( Reflectance.CONDITION_SPECULAR );
//            Entity e =(Entity)ret;
            ret.setName( newName );
//            if(desc != null )
//                e.setDescription( desc   );
//            Map prop = new HashMap();
//            if( common != null )
//                prop.putAll( common );
//            if( m != null )
//                prop.putAll( m );
//            prop.put( ANGL, values[ 0 ] );
//            e.setProperties( prop );
            ctx.getReflectances().add( ret );
        }
    }

    private float[] getReadings( String[] args )
    {
        int len = args.length;
        float[] ret = new float[ len - 1 ];
        //ignore the first reading
        float v;
        for( int i = 1; i < len; i++ )
        {
            try
            {
                v = Float.parseFloat( args[ i ] );
                if( v < 0 )
                {
                    ret[ i - 1 ] = 0;
                }
                else
                {
                    ret[ i - 1 ] = v / 100f;
                }
            }
            catch( NumberFormatException ne )
            {
                ret[ i - 1 ] = 0.0f;
            }
        }
        return ret;
    }

    protected void duplicateKeyMapping( Map<String, String> map, List<String> list, String tag )
    {
        if( list != null && list.size() > 0 )
        {
            Iterator<String> it = list.iterator();
            //first note tag does not have suffix
            String tmp = it.next();
            if( tmp != null )
            {
                map.put( tag, tmp );
            }
            tag = tag.concat( "_" );
            for( int i = 1; it.hasNext(); i++ )
            {
                String value1 = it.next();
                map.put( tag.concat( String.valueOf( i ) ), value1 );
            }
        }
    }

    private List<String> getTags()
    {
        if( m_tags == null )
        {
            m_tags = new ArrayList<String>();
        }
        return m_tags;
    }

    private List<String> getSamples()
    {
        if( m_samples == null )
        {
            m_samples = new ArrayList<String>();
        }
        return m_samples;
    }
}
