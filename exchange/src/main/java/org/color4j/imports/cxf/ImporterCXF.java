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
package org.color4j.imports.cxf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.color4j.colorimetry.Reflectance;
import org.color4j.imports.AbstractTextFileReflectanceImporter;
import org.color4j.imports.ImportException;
import org.color4j.imports.XMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ImporterCXF extends AbstractTextFileReflectanceImporter
{
    static private Logger m_Logger = LoggerFactory.getLogger( ImporterCXF.class );

    private Element m_Root;

    public ImporterCXF()
    {
        super( "CXF", "cxf" );
    }

    public Reflectance[] doImport( InputStream stream, Map<String, String> attributes )
        throws IOException, ImportException
    {
        return null;
    }

    private void parseXML( InputStream is )
        throws XMLException, DOMException
    {
        if( is != null )
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                java.io.InputStreamReader isr = new java.io.InputStreamReader(
                    is, "UTF-8" );
                org.xml.sax.InputSource isc = new org.xml.sax.InputSource( isr );
                Document doc = factory.newDocumentBuilder().parse( isc );
                m_Root = doc.getDocumentElement();
            }
            catch( Throwable e )
            {
                m_Logger.error( e.getMessage(), e );
                if( e instanceof DOMException )
                {
                    throw (DOMException) e;
                }
                throw new XMLException( e.getMessage(), e );
            }
            finally
            {
                try
                {
                    is.close();
                }
                catch( Exception e )
                {
                    m_Logger.error( e.getMessage(), e );
                }
            }
        }
    }
}
