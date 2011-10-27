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
package org.color4j.exports.ai;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Date;
import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.Illuminant;
import org.color4j.colorimetry.Observer;
import org.color4j.colorimetry.Weights;
import org.color4j.colorimetry.encodings.DefaultEncodingFactory;
import org.color4j.colorimetry.encodings.EncodingFactory;
import org.color4j.colorimetry.weights.WeightsCache;
import org.color4j.colorimetry.encodings.RGB;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.observers.ObserverImpl;
import org.color4j.exports.AbstractReflectanceExporter;
import org.color4j.exports.TextFileReflectanceExporter;

public class ExporterAI extends AbstractReflectanceExporter implements TextFileReflectanceExporter
{
    private Illuminant m_Ill;
    private Observer m_Obs;
    private EncodingFactory factory = new DefaultEncodingFactory();

    public ExporterAI()
    {
        super( TextFileReflectanceExporter.AI, "ai" );
    }

    public void exportReflectances( Collection<Reflectance> reflectances, OutputStream out )
        throws IOException
    {
        if( reflectances != null && reflectances.size() > 0 )
        {
            XYZ wp = getWhitePoint();
            if( wp != null )
            {
                StringBuilder sb = new StringBuilder( 500 );
                sb.append( constructHeader() );
                DecimalFormat df = new DecimalFormat( "#.#####" );
                RGB rgb;
                for( Reflectance r : reflectances )
                {
                    // KH - Dec 16, 2004 : color format
                    // 0.84477 0.88692 0.91833 Xa
                    // (Blue 0.01) Pc
                    try
                    {
                        rgb = factory.createXYZ( m_Ill, r, m_Obs ).toRGB( wp );
                        sb.append( df.format( rgb.getR() ) );
                        sb.append( " " );
                        sb.append( df.format( rgb.getG() ) );
                        sb.append( " " );
                        sb.append( df.format( rgb.getB() ) );
                        sb.append( " " );
                        sb.append( " Xa\n" );
                    }
                    catch( ColorException e )
                    {
                        m_Logger.error( e.getMessage(), e );
                    }

                    sb.append( "(" );
                    sb.append( r.getName() );
                    sb.append( ") Pc\n" );
                }

                sb.append( constructFooter() );
                out.write( sb.toString().getBytes() );
                sb.setLength( 0 );
            }
        }
    }

    // KH - Dec 16, 2004 : initializes the illuminant, observer, and whitepoint to use
    private XYZ getWhitePoint()
    {
        try
        {
            m_Ill = IlluminantImpl.create( getProperties().get( PROP_ILLUMINANT ).toString() );
            m_Obs = ObserverImpl.create( getProperties().get( PROP_OBSERVER ).toString() );
            Weights weights = WeightsCache.getInstance().getWeights( m_Ill, m_Obs );
            return weights.toWhitePoint();
        }
        catch( ColorException e )
        {
            m_Logger.error( e.getMessage(), e );
            throw e;
        }
    }

    public void resetState()
    {
    }

    public boolean requiresPrimary()
    {
        return false;
    }

    public boolean supportsMultipleColors()
    {
        return true;
    }

    private String constructFooter()
    {
        StringBuilder sb = new StringBuilder( 100 );
        try
        {
            sb.append( "PB\n" );
            sb.append( "%AI5_EndPalette\n" );
            sb.append( "%%EndSetup\n" );
            sb.append( "%%EOF\n" );
            return sb.toString();
        }
        finally
        {
            sb.setLength( 0 );
        }
    }

    private String constructHeader()
    {
        StringBuilder sb = new StringBuilder( 200 );
        try
        {
            sb.append( "%!PS-Adobe-3.0\n" );
            sb.append( "%%Creator : Color4j.org\n" );
            sb.append( "%%For: No one\n" );
            sb.append( "%%Title: Illustrator- Palette\n" );
            sb.append( "%%CreationDate: " );

            //format is day.month.year
            Date d = new Date();
            sb.append( d.getDate() );
            sb.append( "." );
            sb.append( d.getMonth() );
            sb.append( "." );
            sb.append( d.getYear() );
            sb.append( "\n" );

            sb.append( "%%BoundingBox: 12 303 547 685\n" );
            sb.append( "%AI5_FileFormat 3\n" );
            sb.append( "%%EndComments\n" );
            sb.append( "%%EndProlog\n" );
            sb.append( "%%BeginSetup\n" );
            sb.append( "%AI5_BeginPalette\n" );
            sb.append( "0 0 Pb\n" );
            return sb.toString();
        }
        finally
        {
            sb.setLength( 0 );
        }
    }

    public boolean supportsAttributes()
    {
        return false;
    }
}
