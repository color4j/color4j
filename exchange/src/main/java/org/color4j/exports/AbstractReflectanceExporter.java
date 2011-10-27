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
package org.color4j.exports;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.color4j.colorimetry.Reflectance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReflectanceExporter implements ReflectanceExporter
{
    protected Logger m_Logger;
    private String m_Name;
    private String m_Extension;

    private Map m_Props;

    public AbstractReflectanceExporter( String name, String extension )
    {
        m_Name = name;
        m_Extension = extension;
        m_Logger = LoggerFactory.getLogger( getClass() );
    }

    public String getName()
    {
        return m_Name;
    }

    public void doExport( Reflectance[] colors, OutputStream out )
        throws IOException, ExportException
    {
        ArrayList<Reflectance> list = new ArrayList<Reflectance>( colors.length );
        Collections.addAll( list, colors );
        exportReflectances( list, out );
        resetState();
    }

    public void doExport( Collection<Reflectance> reflectances, OutputStream out )
        throws IOException, ExportException
    {
        exportReflectances( reflectances, out );
        resetState();
    }

    abstract public void exportReflectances( Collection<Reflectance> reflectances, OutputStream out )
        throws IOException, ExportException;

    /**
     * since the exporters are instantiated in a static manager, their state needs
     * to be reset after each use
     */
    abstract public void resetState();

    public String getDefaultExtension()
    {
        return m_Extension;
    }

    public Map getProperties()
    {
        return m_Props;
    }

    public void setProperties( Map properties )
    {
        m_Props = properties;
    }
}
