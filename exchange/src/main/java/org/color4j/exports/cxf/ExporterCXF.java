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

package org.color4j.exports.cxf;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.exports.AbstractReflectanceExporter;
import org.color4j.exports.ExportException;
import org.color4j.exports.TextFileReflectanceExporter;
import org.color4j.exports.XMLException;
import org.color4j.exports.XMLUtility;
import org.slf4j.Logger;
import org.w3c.dom.Element;

public class ExporterCXF extends AbstractReflectanceExporter
    implements CXFElementTagNames, TextFileReflectanceExporter
{
    public ExporterCXF()
    {
        super( ROOT_ELEMENT, "cxf" );
        resetState();
    }

    public boolean requiresPrimary()
    {
        return false;
    }

    public boolean supportsMultipleColors()
    {
        return true;
    }

    public void exportReflectances( Collection<Reflectance> reflectances, OutputStream out )
        throws ExportException
    {
        if( reflectances != null && reflectances.size() > 0 )
        {
            Element set = m_XMLGen.getDocument().createElement( SAMPLE_SET );
            m_XMLGen.appendToParent( set, NAME, SET_NAME );
            m_XMLGen.getRootElement().appendChild( set );
            for( Reflectance r : reflectances )
            {
                appendSample( set, r );
            }
        }

        try
        {
            m_XMLGen.writeToStream( out );
        }
        catch( XMLException e )
        {
            throw new ExportException( e.getMessage(), e );
        }
    }

    public void resetState()
    {
        try
        {
            m_XMLGen = new XMLUtility( ROOT_ELEMENT );
        }
        catch( ParserConfigurationException e )
        {
            m_Logger.error( e.getMessage(), e );
        }
        catch( FactoryConfigurationError e )
        {
            m_Logger.error( e.getMessage(), e );
        }

        m_XMLGen.appendToParent( m_XMLGen.getRootElement(), NAME, COLOR4J_DESCRIPTION );
        if( m_Conditions != null )
        {
            m_Conditions.clear();
        }

        m_Conditions = new HashMap<Map, String>();
        m_ConditionCounter = 0;
    }

    private void appendSample( Element set, Reflectance r )
    {
        Element sample = m_XMLGen.getDocument().createElement( SAMPLE );
        set.appendChild( sample );
        m_XMLGen.appendToParent( sample, NAME, r.getName() );
        Element sattr = m_XMLGen.getDocument().createElement( SAMPLE_ATTRIBUTE );
        sample.appendChild( sattr );
        Element spectrum = m_XMLGen.getDocument().createElement( SPECTRUM );
        sattr.appendChild( spectrum );

        StringBuilder sb = new StringBuilder( 300 );
        double[] vals = r.getSpectrum().getValues();
        int start = r.getSpectrum().getShortestWavelength();
        int interval = r.getSpectrum().getInterval();
//        for( int i = 0; i < vals.length; i++ )
//        {
//            Element val = m_XMLGen.createElement( VALUE, new Double( vals[i] ) );
//            spectrum.appendChild( val );
//            val.setAttribute( NAME, start+"" );
//            start += interval;
//        }

        // KH - Dec 15, 2004 : Macbeth's i1 color crap requires the wavelengths to be between
        //380nm and 730nm (36 pts), so must buffer our stuffs to accomodate 
        //as of this date, their i1 (free)software uses CIELab/Lch CDF

        // KH - Dec 15, 2004 : short wavelength buffer
        if( start > MIN_LIMITATION )
        {
            for( int i = MIN_LIMITATION; i < start; i += 10 )
            {
                Element val = m_XMLGen.createElement( VALUE, (double) 0 );
                spectrum.appendChild( val );
                val.setAttribute( NAME, i + "" );
            }
        }

        for( double value : vals )
        {
            if( start > MAX_LIMITATION )
            {
                break;
            }

            if( start >= MIN_LIMITATION )
            {
                Element element = m_XMLGen.createElement( VALUE, value );
                spectrum.appendChild( element );
                element.setAttribute( NAME, start + "" );
            }
            start += interval;
        }

        // KH - Dec 15, 2004 : long wavelength buffer
        if( start < MAX_LIMITATION )
        {
            for( int i = start; i <= MAX_LIMITATION; i += 10 )
            {
                Element val = m_XMLGen.createElement( VALUE, (double) 0 );
                spectrum.appendChild( val );
                val.setAttribute( NAME, i + "" );
            }
        }

        m_XMLGen.appendToParent( spectrum, SPECTRUM_DATA, sb.toString() );
        sb.setLength( 0 );

        spectrum.setAttribute( CONDITIONS, linkConditions( r ) );
    }

    /*
    * KH - Dec 14, 2004 :
    * does two things:
    * appends a conditions element to the root
    * returns the name for the condition
    * for now, conditions will not include illuminant and observer since spectral data is uber
    */
    private String linkConditions( Reflectance r )
    {
        Map conds = r.getConditions();
        // KH - Dec 15, 2004 : construct map of our conditions and macbeth's req'd conditions
        if( conds == null )
        {
            conds = createBasicCondition();
        }
        else
        {
            conds.putAll( createBasicCondition() );
        }

//        Map conds = createBasicCondition( r );
        // KH - Dec 15, 2004 : check our cache if these conditions exist
        String name = m_Conditions.get( conds );
        if( name == null )
        {
            // KH - Dec 14, 2004 : take care of the name
            StringBuffer sb = new StringBuffer( 20 );
            sb.append( CONDITION_NAME );
            sb.append( m_ConditionCounter++ );
            name = sb.toString();
            m_Conditions.put( conds, name );

            // KH - Dec 14, 2004 : handle the XML
            appendConditions( name, conds );
        }

        return name;
    }

    private void appendConditions( String uid, Map m )
    {
        Element cond = m_XMLGen.getDocument().createElement( CONDITIONS );
        m_XMLGen.getRootElement().appendChild( cond );
        m_XMLGen.appendToParent( cond, ID, uid );

        for( Iterator<Map.Entry> iter = m.entrySet().iterator(); iter.hasNext(); )
        {
            Map.Entry e = (Map.Entry) iter.next();
            Element attr = m_XMLGen.createElement( ATTRIBUTE, e.getValue().toString() );
            cond.appendChild( attr );
            attr.setAttribute( NAME, e.getKey().toString() );
        }
    }

    public boolean supportsAttributes()
    {
        return true;
    }

    private Map<String,String> createBasicCondition()
    {
        // KH - Dec 15, 2004 : Macbeth's i1 color crap requires the wavelengths to be between
        //380nm and 730nm (36 pts), so must buffer our stuffs to accomodate
        HashMap<String,String> m = new HashMap<String, String>( 4 );
        m.put( MAX_LAMBDA, MAX_LIMITATION + "nm" );
        m.put( MIN_LAMBDA, MIN_LIMITATION + "nm" );
        m.put( DATA_POINTS, "36" );
        m.put( FILTER_USED, "No" );   //TODO KH - Dec 15, 2004 : not sure what filter is
        return m;
    }

    private final static int MAX_LIMITATION = 730;
    private final static int MIN_LIMITATION = 380;
    private final static String CONDITION_NAME = "Condition";
    //key is the condition map, value is the uid
    private Map<Map, String> m_Conditions;
    private int m_ConditionCounter;
    private XMLUtility m_XMLGen;

    protected Logger m_Logger;

    /**
     * Q&D utility class to display xml in a more readable format
     */
    public static class PrettyXMLPrinter
    {

        private static final String SPACE = "    ";
        private static int m_Spaces = 0;
        private static StringBuffer m_Sb;

        public static String prettyPrintXML( String xml )
        {
            m_Sb = new StringBuffer( 10000 );
            String[] data = xml.split( "<" );
            ArrayList<String> al = new ArrayList<String>( data.length );
            al.addAll( Arrays.asList( data ).subList( 2, data.length ) );
            m_Sb.append( "<" );
            m_Sb.append( data[ 1 ] );
            printElement( al );
            String toRet = m_Sb.toString();
            reinit();
            return toRet;
        }

        private static void reinit()
        {
            m_Spaces = 0;
            m_Sb.setLength( 0 );
        }

        private static void printElement( ArrayList<String> al )
        {

            if( al.size() == 0 )
            {
                return;
            }

            String nextString = al.remove( 0 );
            m_Sb.append( "\n" );

            if( isStartofElement( nextString ) )
            {
                //<A>
                for( int i = 0; i < m_Spaces; i++ )
                {
                    m_Sb.append( SPACE );
                }
                m_Sb.append( "<" );
                m_Sb.append( nextString );
                m_Spaces++;
            }
            else if( isEndofElement( nextString ) )
            {
                //</A>
                if( m_Spaces > 0 )
                {
                    m_Spaces--;
                }
                for( int i = 0; i < m_Spaces; i++ )
                {
                    m_Sb.append( SPACE );
                }
                m_Sb.append( "<" );
                m_Sb.append( nextString );
            }
            else if( isSingleElement( nextString ) )
            {
                //<A/>
                for( int i = 0; i < m_Spaces; i++ )
                {
                    m_Sb.append( SPACE );
                }
                m_Sb.append( "<" );
                m_Sb.append( nextString );
            }
            else
            {
                //is start of attribute ... <A>sdsd
                for( int i = 0; i < m_Spaces; i++ )
                {
                    m_Sb.append( SPACE );
                }
                m_Sb.append( "<" );
                m_Sb.append( nextString );
                m_Sb.append( "<" );
                m_Sb.append( al.remove( 0 ) );
            }

            printElement( al );
        }

        private static boolean isSingleElement( String s )
        {
            //<A/>
            return s.charAt( s.length() - 2 ) == '/' || s.charAt( 0 ) == '?';
        }

        private static boolean isEndofElement( String s )
        {
            //</A>
            return s.charAt( 0 ) == '/' && s.charAt( 0 ) != '?';
        }

        private static boolean isStartofElement( String s )
        {
            //<A>
            return s.charAt( s.length() - 1 ) == '>' && s.charAt( 0 ) != '/' && s.charAt( s.length() - 2 ) != '/';
        }
    }
}