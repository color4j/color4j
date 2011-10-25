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
 * LineParserImpl.java
 *
 * Created on May 6, 2003, 11:09 AM
 */

package org.color4j.imports.ini;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.color4j.imports.ImportException;

/**
 *
 */
//TODO: Clean up. ML - 05/08/2003
public abstract class AbstractSectionParser
    implements SectionParser
{
//    /**the extending class needs to instantiate the static values of the header
//     * ie, for file info, static{ HEADERS.add( <header> ) }
//     */
//    private final static Collection HEADERS = new ArrayList();
//    

    /**
     * Creates a new instance of LineParserImpl
     */
    public AbstractSectionParser()
    {
    }

    /**
     * <PRE>
     * called by the <code>ParserContext</code> to execute this parser
     * </PRE>
     *
     * <PRE>
     * This class can set which is the next <code>SectionParser</code> to be executed
     * </PRE>
     * <PRE>
     * Also checks what is the next expected String header.
     * </PRE>
     *
     * <PRE>
     * Calls <code>processLine()</code>
     * </PRE>
     *
     * @param context    is the <code>ParserContext</code> to pass to this class.
     * @param reader     is the <code>Reader</code> containing the character stream to be parsed
     * @param attributes
     *
     * @return <i>false</i> if the parsing has reached the end state. ie finished parsing document or an error occurred.
     *         Otherwise return <i>true</i>.
     */
    public boolean canProcess( ParserContext context,
                               BufferedReader reader,
                               Map<String, String> attributes,
                               Map cachedKeys
    )
        throws ImportException, IOException
    {
        boolean ret = false;
        String line;
        while( ( line = reader.readLine() ) != null )
        {
            line = line.trim();
            if( line.length() > 0 )
            {
                if( line.charAt( 0 ) == '[' )
                {
                    //checks that the next header is STD
                    if( !this.isValidHeader( line, getValidHeaders() ) )
                    {
                        throw new ImportException( "Wrong format: " + line + " expected: " + expectedHeaders( getValidHeaders() ) );
                    }
                    context.setCurrentSectionParser( getTagName( line ) );
                    ret = true;
                    break;
                }
                processLine( line, context );
            }
        }
        if( line == null )
        {
            processLine( line, context );  //send EOF to parser
        }
        postProcess( context, attributes, cachedKeys );

        return ret;
    }

    //attribute consists of key and value seprated by a token
    // @paran str is the String that contains the key and value
    // @param c is the char that separates the key and value
    // @return an array of length 2. the first element in the array contains the string value of the key
    // the second element contains the string value of the value 
    protected String[] getAttribute( String str, char c )
        throws ImportException
    {
        int index = str.indexOf( c );
        if( index <= 0 )
        {
            throw new ImportException( "Can not find key value" );
        }
        //
        String[] ret = new String[ 2 ];
        ret[ 0 ] = str.substring( 0, index );
        ret[ 1 ] = str.substring( index + 1 );
        return ret;
    }

    //@param str is of the form [ name ]
    //@return name
    protected String getTagName( String str )
    {
        int pos1 = str.indexOf( "[" );
        int pos2 = str.lastIndexOf( "]" );
        return str.substring( pos1 + 1, pos2 );
    }

    boolean isValidHeader( String str, Collection headerType )
    {
        boolean ret = false;
        if( headerType.contains( getTagName( str ) ) )
        {
            ret = true;
        }
        return ret;
    }

    private String expectedHeaders( Collection<String> headers )
    {
        StringBuilder sb = new StringBuilder();
        for( String header : headers )
        {
            sb.append( header );
            sb.append( " | " );
        }
        return sb.toString();
    }

    /**
     * gets the <code>Collection</code> of <code>String</code>s of the next expected headers. Does not convert for caps
     * will look for indicated header, or proceed to EOF
     *
     * @return next expected String. no <b>null</b>
     */
    public abstract Collection getValidHeaders();

    /**
     *
     * gets the next SectionParser to be set by the <code>ParserContext</code>.
     *
     *@return a <code>SectionParser</code> not allowed to return a <b>null</b>
     **/
//    public SectionParser getSectionParser()
//    {
//        //@todo need to change this currently return null to allow to compile.
//        return null;
//    }
//    

    /**
     * <PRE>
     * the logic of parsing this states body of key and values
     * </PRE>
     *
     * @param str
     * @param ctx
     * @throws ImportException if unexpected format found
     */
    public abstract void processLine( String str, ParserContext ctx )
        throws ImportException;

    /**
     * after all the lines are processed do something useful with it before moving to next section
     * @param ctx
     * @param attributes
     * @param cacheKeys
     * @throws org.color4j.imports.ImportException
     */
    public abstract void postProcess( ParserContext ctx, Map<String, String> attributes, Map cacheKeys )
        throws ImportException;

    protected void duplicateKeyMapping( Map<String,String> m, List<String> list, String tag )
    {
        if( list != null && list.size() > 0 )
        {
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
