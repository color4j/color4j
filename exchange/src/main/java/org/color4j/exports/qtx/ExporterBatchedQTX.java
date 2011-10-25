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
import org.color4j.colorimetry.entities.Reflectance;
import org.color4j.colorimetry.entities.ReflectanceSet;
import org.color4j.exports.AbstractReflectanceExporter;
import org.color4j.exports.ExportException;
import org.color4j.exports.TextFileReflectanceExporter;

/**
 * Creates QTX files with a single Standard from the first
 * Reflectance in the array and all following Reflectances
 * are marked Batch
 */
public class ExporterBatchedQTX extends AbstractReflectanceExporter
    implements TextFileReflectanceExporter
{

    public ExporterBatchedQTX()
    {
        super( TextFileReflectanceExporter.QTX_BATCH, "qtx" );
    }

    public boolean requiresPrimary()
    {
        return true;
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
            doExport( colors[ 0 ].getName(), colors[ i ], out, i );
            out.write( 13 );
            out.write( 10 );
        }
    }

//    /*
//     * overrides super.doExport
//     */
//    public void doExport( Reflectance[] colors, Map metaData, OutputStream out )
//        throws IOException, ExportException
//    {
//        final String EQUAL = "=";
//        final String NEW_LINE = "\n";
//        
//        StringBuffer sb = new StringBuffer(1024);
//        for ( Iterator itr = metaData.entrySet().iterator(); itr.hasNext(); )
//        {
//            Map.Entry me = (Map.Entry) itr.next();
//            sb.append( (String) me.getKey() ).append( EQUAL ).append( me.getValue() );
//            sb.append( NEW_LINE );
//        }
//        
//        for( int i=0 ; i < colors.length ; i++ )
//        {
//            doExport( colors[0].getName(), colors[i], out, i );
//            //out.write( 13 );
//            //out.write( 10 );
//            
//            if ( i > 0 )
//            {
//                PrintWriter pw = new PrintWriter( out, true );
//                pw.print( sb.toString() );
//                pw.flush();
//            }
//        }        
//    }

    private void doExport( String standard, Reflectance color, OutputStream out, int sequence )
        throws ExportException
    {
        String header = getHeader( sequence );
        QTXSection section = new QTXSection( standard, color, header );
        section.doExport( out );
    }

//    private void doExport( String standard, AttributedReflectance refl, OutputStream out, int sequence )
//        throws ExportException
//    {
//        String header = getHeader( sequence );
//        QTXSection section = new QTXSection( standard, refl, header );
//        section.doExport( out );
//    }

    private String getHeader( int sequence )
    {
        StringBuilder header = new StringBuilder();
        if( sequence == 0 )
        {
            header.append( QTXSection.STANDARD_DATA_INIT );
        }
        else
        {
            header.append( QTXSection.BATCH_DATA );
            header.append( " " );
            header.append( sequence - 1 );
        }
        return header.toString();
    }

    public void exportReflectances( Collection<Reflectance> reflectances, OutputStream out )
        throws IOException, ExportException
    {
        Iterator<Reflectance> iterator = reflectances.iterator();
        Reflectance standardObject = reflectances.iterator().next();
        String standard = standardObject.getName();

        int i = 0;
        while( iterator.hasNext() )
        {
            Reflectance reflectance = iterator.next();
            doExport( standard, reflectance, out, i++ );
            out.write( 13 );
            out.write( 10 );
        }
    }

    public void exportReflectanceSets( Collection<ReflectanceSet> reflectancesets, OutputStream out )
        throws IOException, ExportException
    {
        for( ReflectanceSet reflectanceset : reflectancesets )
        {
            String standardName = reflectanceset.getStandard().getName();
            int i = 0;
            for( Reflectance reflectance : reflectanceset.getBatches() )
            {
                doExport( standardName, reflectance, out, i );
                i++;
            }
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
