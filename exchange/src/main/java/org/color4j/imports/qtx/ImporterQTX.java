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

package org.color4j.imports.qtx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.ReflectanceImpl;
import org.color4j.colorimetry.Spectrum;
import org.color4j.imports.AbstractTextFileReflectanceImporter;
import org.color4j.imports.ImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImporterQTX extends AbstractTextFileReflectanceImporter
{
    static private Map<String, Pair> m_translator;
    static private Map<String, Pair> m_translator2;
    static private Logger m_Logger;

    private String m_Name;

    static
    {
        m_Logger = LoggerFactory.getLogger( ImporterQTX.class );
        m_translator = new HashMap<String, Pair>( 11 );

        m_translator.put( "XUSAV", new Pair( Reflectance.CONDITION_APERTURE, "XUSAV" ) );
        m_translator.put( "USAV", new Pair( Reflectance.CONDITION_APERTURE, "USAV" ) );
        m_translator.put( "VSAV", new Pair( Reflectance.CONDITION_APERTURE, "VSAV" ) );
        m_translator.put( "SAV", new Pair( Reflectance.CONDITION_APERTURE, "SAV" ) );
        m_translator.put( "MAV", new Pair( Reflectance.CONDITION_APERTURE, "MAV" ) );
        m_translator.put( "LAV", new Pair( Reflectance.CONDITION_APERTURE, "LAV" ) );
        m_translator.put( "XLAV", new Pair( Reflectance.CONDITION_APERTURE, "XLAV" ) );
        m_translator.put( "SCI", new Pair( Reflectance.CONDITION_SPECULAR, "SCI" ) );
        m_translator.put( "SCE", new Pair( Reflectance.CONDITION_SPECULAR, "SCE" ) );
        m_translator.put( "%R", new Pair( Reflectance.CONDITION_MODE, "Reflectance" ) );
        m_translator.put( "%T", new Pair( Reflectance.CONDITION_MODE, "Transmitance" ) );

        // added by george
        m_translator.put( "UV", new Pair( Reflectance.CONDITION_LIGHTFILTER, "420 nm" ) );
        m_translator.put( "Inc", new Pair( Reflectance.CONDITION_LIGHTFILTER, "UV Inc" ) );
        m_translator.put( "Cal", new Pair( Reflectance.CONDITION_LIGHTFILTER, "UV Cal" ) );
        m_translator.put( "400", new Pair( Reflectance.CONDITION_LIGHTFILTER, "400 nm" ) );
        m_translator.put( "420", new Pair( Reflectance.CONDITION_LIGHTFILTER, "420 nm" ) );
        m_translator.put( "460", new Pair( Reflectance.CONDITION_LIGHTFILTER, "460 nm" ) );
        m_translator.put( "Custom", new Pair( Reflectance.CONDITION_LIGHTFILTER, "Custom" ) );
        m_translator.put( "UVAD1", new Pair( Reflectance.CONDITION_LIGHTFILTER, "UVAD1" ) );
        m_translator.put( "UVAD2", new Pair( Reflectance.CONDITION_LIGHTFILTER, "UVAD2" ) );
        m_translator.put( "P", new Pair( Reflectance.CONDITION_LIGHTFILTER, "P" ) );
        m_translator.put( "U", new Pair( Reflectance.CONDITION_LIGHTFILTER, "U" ) );
        m_translator.put( "D65", new Pair( Reflectance.CONDITION_LIGHTFILTER, "D65" ) );

        m_translator2 = new HashMap<String, Pair>( 7 );
        m_translator2.put( "XUSAV", new Pair( Reflectance.CONDITION_LENSPOSITION, "XUSAV" ) );
        m_translator2.put( "USAV", new Pair( Reflectance.CONDITION_LENSPOSITION, "USAV" ) );
        m_translator2.put( "VSAV", new Pair( Reflectance.CONDITION_LENSPOSITION, "VSAV" ) );
        m_translator2.put( "SAV", new Pair( Reflectance.CONDITION_LENSPOSITION, "SAV" ) );
        m_translator2.put( "MAV", new Pair( Reflectance.CONDITION_LENSPOSITION, "MAV" ) );
        m_translator2.put( "LAV", new Pair( Reflectance.CONDITION_LENSPOSITION, "LAV" ) );
        m_translator2.put( "VLAV", new Pair( Reflectance.CONDITION_LENSPOSITION, "VLAV" ) );
    }

    public ImporterQTX()
    {
        super( "QTX", "qtx" );
    }

    public Reflectance[] doImport( InputStream content, Map<String, String> attributes )
        throws ImportException
    {
        InputStreamReader in = null;
        try
        {
            in = new InputStreamReader( content );
            BufferedReader reader = new BufferedReader( in );
            ArrayList<Reflectance> refls = new ArrayList<Reflectance>( 1000 );
            HashMap<String, String> cachedKeys = new HashMap<String, String>( attributes );
            while( true )
            {
                Reflectance r = doSingleImport( reader, attributes, cachedKeys );
                if( r == null )
                {
                    break;
                }
                refls.add( r );
            }
            Reflectance[] result = new Reflectance[ refls.size() ];
            Iterator<Reflectance> list = refls.iterator();
            for( int i = 0; list.hasNext(); i++ )
            {
                result[ i ] = list.next();
            }
            m_Logger.debug( "Reflectances size " + result.length );
            return result;
        }
//        catch (UnsupportedEncodingException e)
//        {
//            throw new ImportException( e.getMessage(), e );
//        }
        finally
        {
            if( in != null )
            {
                try
                {
                    in.close();
                }
                catch( IOException e )
                {
                }
            }
        }
    }

    public Reflectance doSingleImport( BufferedReader reader,
                                       Map<String, String> attributes,
                                       Map<String, String> cachedKeys
    )
        throws ImportException
    {
        //if( m_Logger.isDebugEnabled() )
        //    m_Logger.debug( "Do a single color." );
        try
        {
            String line;
            String header;
            while( ( line = reader.readLine() ) != null )
            {
                line = line.trim();
                //if( m_Logger.isDebugEnabled() )
                //    m_Logger.debug( "parseQTX:          " + line );
                if( line.startsWith( "[" ) )
                {
                    //if( m_Logger.isDebugEnabled() )
                    //    m_Logger.debug( "Parse the Header." );
                    header = parseHeader( line ).toUpperCase();
                    if( header == null )
                    {
                        throw new ImportException( "Incorrectly formatted header: '" + line + "'." );
                    }
                }
                else
                {
                    // If the line does not start with "[", then assume it is not a valid line.
                    continue;
                }

                Reflectance sample;
                if( header.indexOf( "STANDARD" ) >= 0 )
                {
//                    if( m_Logger.isDebugEnabled() )
//                        m_Logger.debug( "Parse a Standard Data." );
                    sample = parseData( reader, "STD", attributes, cachedKeys );
                }
                else if( header.indexOf( "BATCH" ) >= 0 )
                {
//                    if( m_Logger.isDebugEnabled() )
//                        m_Logger.debug( "Parse a Batch Data." );
                    sample = parseData( reader, "BAT", attributes, cachedKeys );
                }
                else
                {
//                    if( m_Logger.isDebugEnabled() )
//                        m_Logger.debug( "Nothing to parse." );
                    // No more to parse
                    break;
                }
                if( sample == null )
                {
//                    if( m_Logger.isDebugEnabled() )
//                        m_Logger.debug( "Unable to parse the QTX section." );
                    continue;
                }
//                m_Logger.info( "Sample parsed successfully: " + sample.getName() );
                return sample;
            }
        }
        catch( IOException e )
        {
            m_Logger.warn( "", e );
        }
        finally
        {
            // Can not close the created streams, since it will close
            // the underlying inputstream which will be used again.
        }

//        if( m_Logger.isDebugEnabled() )
//            m_Logger.debug( "No more to parse." );
        return null;
    }

    private String parseHeader( String line )
    {
        int pos1 = line.indexOf( "[" );
        int pos2 = line.lastIndexOf( "]" );
        return line.substring( pos1 + 1, pos2 );
    }

    private Reflectance parseData( BufferedReader reader,
                                   String type,
                                   Map<String, String> template,
                                   Map<String, String> cachedKeys
    )
        throws ImportException, IOException
    //, ColorException, ProcessException
    {
        if( !reader.markSupported() )
        {
            throw new InternalError( "Reader MUST support markers." );
        }

        Map<String, ArrayList<String>> m = new HashMap<String, ArrayList<String>>();
        ArrayList<String> list = null;

        while( true )
        {
            reader.mark( 20000 );
            String line = reader.readLine();
            //if( m_Logger.isDebugEnabled() )
            //    m_Logger.debug( line );
            if( line == null )
            {
                break;
            }
            line = line.trim();
            if( line.startsWith( "[" ) )
            {
                break;
            }
            if( line.equals( "" ) )
            {
                break;
            }
            int pos = line.indexOf( '=' );
            if( pos < 0 )
            {
                throw new ImportException( "Invalid format:" + line );
            }
            String key = line.substring( 0, pos );
            String value = line.substring( pos + 1 );

            // johnathan - using list so that duplicate values (especially for attributes) can be handled. Jul 23, 2004
            if( m.get( key ) != null )
            {
                ( m.get( key ) ).add( value );
            }
            else
            {
                list = new ArrayList<String>( 2 );
                list.add( value );
                m.put( key, list );
            }

//            m_Logger.debug("\nThe key: " + key);
        }
        reader.reset();

        list = ( m.get( type + "_NAME" ) );
        if( list != null )
        {
            m_Name = list.get( 0 ); // there should only be one
        }

        // make condition map
        String cond = null;
        list = ( m.remove( type + "_VIEWING" ) );
        if( list != null )
        {
            cond = list.get( 0 );  // there should only be one
        }

        // johnathan May 12, 2004 - remove commas from the condition value        
        cond = cond.replace( ',', ' ' );
        Map<String, String> conditionMap = null;
        //String mode = null;
        boolean manual = false;
        if( cond != null )
        {
            conditionMap = new HashMap<String, String>();
            StringTokenizer st = new StringTokenizer( cond );
            while( st.hasMoreTokens() )
            {
                String str = st.nextToken();
                if( str.equals( "Keyboard" ) )
                {
                    manual = true;
                    continue;
                }
                else if( str.charAt( 0 ) == '%' )
                {
                    str = new String( str.getBytes(), 0, 2 );
                }
                // added by george
                else
                {
                    putMap( str, conditionMap );
                }
            }
        }
        //end of condition map

        // if not manual entry and some values are missing
        if( !manual )
        {
            conditionMap = revertToDefault( conditionMap );
        }

        String refLow = null;
        list = ( m.remove( type + "_REFLLOW" ) );
        if( list != null )
        {
            refLow = list.get( 0 );  // there should only be one
        }

        if( refLow == null )
        {
            throw new ImportException( "Required '" + type + "_REFLLOW' parameter was not present in QTX file for " + m_Name + "." );
        }
        refLow = trim( refLow );

        String refInterval = null;
        list = ( m.remove( type + "_REFLINTERVAL" ) );
        if( list != null )
        {
            refInterval = list.get( 0 );  // there should only be one
        }

        if( refInterval == null )
        {
            throw new ImportException( "Required '" + type + "_REFLINTERVAL' parameter was not present in QTX file for " + m_Name + "." );
        }
        refInterval = trim( refInterval );

        String reflpoints = null;
        list = ( m.remove( type + "_REFLPOINTS" ) );
        if( list != null )
        {
            reflpoints = list.get( 0 );  // there should only be one
        }

        if( reflpoints == null )
        {
            throw new ImportException( "Required '" + type + "_REFLPOINTS' parameter was not present in QTX file for " + m_Name + "." );
        }

        int nm = Integer.parseInt( refLow );
        int interval = Integer.parseInt( refInterval );

        String reflString = null;
        list = ( m.remove( type + "_R" ) );
        if( list != null )
        {
            reflString = list.get( 0 );  // there should only be one
        }
        StringTokenizer st = new StringTokenizer( reflString, " ,", false );
        int noOfTokens = st.countTokens();
        float[] values = new float[ noOfTokens ];
        for( int i = 0; i < noOfTokens; i++ )
        {
            String str = st.nextToken();
            try
            {
                float v = Float.parseFloat( str );
                if( v < 0 )
                {
                    values[ i ] = 0;
                }
                else
                {
                    values[ i ] = v / 100f;
                }
            }
            catch( NumberFormatException nfe )
            {
                //ignore
            }
        }
        Spectrum spectrum = Spectrum.create( nm, interval, values );
        Reflectance sample = ReflectanceImpl.create( spectrum, conditionMap );

        String name = null; // Get and reset
        list = ( m.remove( type + "_NAME" ) );
        if( list != null )
        {
            name = list.get( 0 );  // there should only be one
        }

        if( name == null )
        {
            name = "Unspecified Name";
        }
        while( name.startsWith( "." ) )  // an initial "." is indicating a public name, and not allowed.
        {
            name = name.substring( 1 );
        }

//        ((Entity)sample).setName( name );

        Collection attributes = new ArrayList();

//        Map props = sample.getProperties();
        //props.put( "Mode", mode );
        if( type.equals( "BAT" ) )
        {
            String standardName = null;
            list = ( m.remove( "STD_NAME" ) );
            if( list != null )
            {
                standardName = list.get( 0 );  // there should only be one
            }
//            props.put( "Standard Name", standardName );
//            EntityAttribute attr = EntityAttributeUtil.createAttribute( sample, "Standard Name", standardName, attributes, cachedKeys );
//            attributes.add( attr );
        }

        for( Map.Entry<String, ArrayList<String>> entry : m.entrySet() )
        {
            String key = entry.getKey();
            ArrayList<String> valueList = entry.getValue();
            for( String value : valueList )
            {
                //                EntityAttribute attr = EntityAttributeUtil.createAttribute( sample, key, value, attributes, cachedKeys );
//                attributes.add( attr );
            }
//            props.put( key, value );
        }
//        sample.setEntityAttributes( attributes );
        //if( m_Logger.isDebugEnabled() )
        //    m_Logger.debug( "Reflectance:" + sample.getName() );
        return sample;
    }

    private String trim( String text )
    {
        text = text.trim();
        if( text.endsWith( "," ) )
        {
            text = text.substring( 0, text.length() - 1 );
        }
        text = text.trim();
        return text;
    }

    private void putMap( String str, Map<String, String> map )
    {
        Pair pair = m_translator.get( str );
        if( pair != null )
        {
            map.put( pair.getKey(),
                     pair.getValue() );
        }
        // HACK need to use 2 maps cos same key should return two values - faizal
        Pair pair2 = m_translator2.get( str );
        if( pair2 != null )
        {
            map.put( pair2.getKey(), pair2.getValue() );
        }
    }

    private Map<String, String> revertToDefault( Map<String, String> map )
    {
        if( map == null )
        {
            map = new HashMap<String, String>();
        }
        if( map.get( Reflectance.CONDITION_APERTURE ) == null )
        {
            putMap( "LAV", map );
        }
        if( map.get( Reflectance.CONDITION_SPECULAR ) == null )
        {
            putMap( "SCI", map );
        }
        if( map.get( Reflectance.CONDITION_LIGHTFILTER ) == null )
        {
            putMap( "Inc", map );//putMap( "UV Inc", map );
        }
        return map;
    }
}
