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
 * AbstractGenerator.java
 *
 * Created on October 31, 2002, 11:48 PM
 */

package org.color4j.exports;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * generic helpertool for creating XML documents
 */
public class XMLUtility
{

    private Document m_Document;
    private String m_RootTag;
    private Element m_RootElement;
    private Logger m_Logger;

    /**
     * Creates a new instance of XMLUtility
     *
     * @param rootTag
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     */
    public XMLUtility( String rootTag )
        throws ParserConfigurationException, FactoryConfigurationError
    {
        m_Logger = LoggerFactory.getLogger( getClass() );
        m_RootTag = rootTag;
        DocumentBuilder builder;

        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        m_Document = builder.newDocument();
        m_RootElement = m_Document.createElement( getRootTag() );
        m_Document.appendChild( m_RootElement );
    }

    private String getRootTag()
    {
        return m_RootTag;
    }

    public Document getDocument()
    {
        return m_Document;
    }

    public Element getRootElement()
    {
        return m_RootElement;
    }

    public long getTime( Date date )
    {
        long d = -1;
        if( date != null )
        {
            d = date.getTime();
        }
        return d;
    }

    public Element appendToParent( Element parent, String name, Object value )
        throws DOMException
    {
        Element element = createElement( name, value );
        parent.appendChild( element );
        return element;
    }

    public Element appendToParent( Element parent, String groupName, String name, Collection values )
        throws DOMException
    {
        Element element = createCollectionElement( groupName, name, values );
        parent.appendChild( element );
        return element;
    }

    public Element createCollectionElement( String groupName, String name, Collection values )
        throws DOMException
    {
        Element element = m_Document.createElement( groupName );
        if( values != null && values.size() > 0 )
        {
            for( Iterator itr = values.iterator(); itr.hasNext(); )
            {
                element.appendChild( createElement( name, itr.next().toString() ) );
            }
        }
        return element;
    }

    public Element createElement( String name, Object value )
        throws DOMException
    {
        Element element = m_Document.createElement( name );
        if( value != null )
        {
            Text text = m_Document.createTextNode( value.toString() );
            element.appendChild( text );
        }
        return element;
    }

    public Element appendMapAsElementsToParent( Element parent, String groupName, String name, Map map )
        throws DOMException
    {
        return appendMapAsElementsToParent( parent, groupName, name, map, "key", "value" );
    }

    public Element appendMapAsElementsToParent( Element parent,
                                                String groupName,
                                                String name,
                                                Map map,
                                                String keyName,
                                                String valueName
    )
        throws DOMException
    {
        Element group = m_Document.createElement( groupName );
        parent.appendChild( group );
        if( map != null )
        {
            Iterator entries = map.entrySet().iterator();
            while( entries.hasNext() )
            {
                Element element = m_Document.createElement( name );
                Map.Entry entry = (Map.Entry) entries.next();
                element.setAttribute( keyName, entry.getKey().toString() );
                if( entry.getValue() != null )
                {
                    element.setAttribute( valueName, entry.getValue().toString() );
                }
                group.appendChild( element );
            }
        }
        return group;
    }

    public String writeToStream( OutputStream os )
        throws XMLException
    {
        try
        {
            ByteArrayOutputStream output = null;
            try
            {
                Transformer t = TransformerFactory.newInstance().newTransformer();
                DOMSource source = new DOMSource( m_Document );
                output = new ByteArrayOutputStream();
                StreamResult stream = new StreamResult( new OutputStreamWriter( output, "UTF-8" ) );
                t.transform( source, stream );
                output.flush();
                output.writeTo( os );
                return output.toString();
            }
            finally
            {
                if( output != null )
                {
                    output.close();
                }
            }
        }
        catch( Exception e )
        {
            m_Logger.error( e.getMessage(), e );
            throw new XMLException( e.getMessage(), e );
        }
    }
}