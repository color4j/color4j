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

package org.color4j.exports.qtx;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import org.color4j.colorimetry.Reflectance;
import org.color4j.exports.AbstractReflectanceExporter;
import org.color4j.exports.ExportException;
import org.color4j.exports.TextFileReflectanceExporter;

public class ExporterQTX extends AbstractReflectanceExporter
    implements TextFileReflectanceExporter
{

    public ExporterQTX()
    {
        super( TextFileReflectanceExporter.QTX, "qtx" );
    }

    public boolean requiresPrimary()
    {
        return false;
    }

    public boolean supportsMultipleColors()
    {
        return true;
    }

    public void doExport( Reflectance[] colors, OutputStream out )
        throws IOException, ExportException
    {
        for( int i = 0; i < colors.length; i++ )
        {
            doExport( colors[ i ], out, i );
            out.write( 13 );
            out.write( 10 );
        }
    }

    private void doExport( Reflectance color, OutputStream out, int sequence )
        throws ExportException
    {
        String header = "STANDARD_DATA " + sequence;
        QTXSection section = new QTXSection( null, color, header );
        section.doExport( out );
    }

    public void exportReflectances( Collection<Reflectance> reflectances, OutputStream out )
        throws IOException, ExportException
    {
        Iterator iter = reflectances.iterator();
        int i = 0;
        while( iter.hasNext() )
        {
            Object o = iter.next();
            doExport( (Reflectance) o, out, i++ );

            out.write( 13 );
            out.write( 10 );
        }
    }

    public void resetState()
    {
        // TODO Auto-generated method stub

    }

    public boolean supportsAttributes()
    {
        return true;
    }
}
