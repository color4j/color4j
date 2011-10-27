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
package org.color4j.exports.aco;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.Illuminant;
import org.color4j.colorimetry.Observer;
import org.color4j.colorimetry.Reflectance;
import org.color4j.colorimetry.Weights;
import org.color4j.colorimetry.encodings.DefaultEncodingFactory;
import org.color4j.colorimetry.encodings.EncodingFactory;
import org.color4j.colorimetry.weights.WeightsCache;
import org.color4j.colorimetry.encodings.RGB;
import org.color4j.colorimetry.encodings.XYZ;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.observers.ObserverImpl;
import org.color4j.exports.AbstractReflectanceExporter;
import org.color4j.exports.TextFileReflectanceExporter;

/**
 * this code assumes characters are 8-bit; *.aco files need 16-bit characters
 */
public class ExporterACO extends AbstractReflectanceExporter implements TextFileReflectanceExporter
{

    private Illuminant m_Ill;
    private Observer m_Obs;
    private int m_noColors;
    private EncodingFactory factory = new DefaultEncodingFactory();

    public ExporterACO()
    {
        super( TextFileReflectanceExporter.ACO, "aco" );
    }

    public void exportReflectances( Collection<Reflectance> reflectances, OutputStream out )
        throws IOException
    {
        if( reflectances != null && reflectances.size() > 0 )
        {
            XYZ wp = getWhitePoint();

            if( wp != null )
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream( 500 );
                try
                {
                    // KH - Dec 17, 2004 : number of colors, max is 65535
                    // (16-bit)
                    // not checking
                    m_noColors = reflectances.size();
                    constructHeader( baos );
                    RGB rgb;
                    ByteArrayOutputStream colorSpecs = new ByteArrayOutputStream( 500 );
                    ByteArrayOutputStream colorNames = new ByteArrayOutputStream( 500 );
                    for( Reflectance r : reflectances )
                    {
                        try
                        {
                            rgb = factory.createXYZ( m_Ill, r, m_Obs ).toRGB( wp );
                            getColorSpecs( rgb, colorSpecs );
                            getColorName( r.getName(), rgb, colorNames );
                        }
                        catch( ColorException e )
                        {
                            m_Logger.error( e.getMessage(), e );
                        }
                    }

                    colorSpecs.writeTo( baos );
                    newSection( baos );
                    colorNames.writeTo( baos );
                    baos.flush();
                    baos.writeTo( out );
                }
                finally
                {
                    baos.close();
                }
            }
        }
    }

    // KH - Dec 16, 2004 : initializes the illuminant, observer, and whitepoint
    // to use
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

    private void getColorName( String name, RGB rgb, ByteArrayOutputStream baos )
    {
        // KH - Dec 17, 2004 : pattern is as follows;
        // empty word
        baos.write( 0 );
        baos.write( 0 );

//      KH - Dec 17, 2004 : R repeated twice
        int color = (int) ( rgb.getR() * 255.0 );
        baos.write( color );
        baos.write( color );

//      KH - Dec 17, 2004 : G repeated twice
        color = (int) ( rgb.getG() * 255.0 );
        baos.write( color );
        baos.write( color );

        // KH - Dec 17, 2004 : B repeated twice
        color = (int) ( rgb.getB() * 255.0 );
        baos.write( color );
        baos.write( color );

        // empty words
        baos.write( 0 );
        baos.write( 0 );
        baos.write( 0 );
        baos.write( 0 );

        // KH - Dec 17, 2004 : length of the name + 1; must split the number
        // intovtwo bytes; shift right dumps first 8 bits into the bit-bucket and
        // gets the first byte, shift left will create a mask to get the second byte
        int one = ( name.length() + 1 ) >> 8;
        int two = ( name.length() + 1 ) ^ ( one << 8 );
        baos.write( one );
        baos.write( two );

        // KH - Dec 17, 2004 : name in 16-bit format
        char[] charName = name.toCharArray();
        for( char ch : charName )
        {
            //split into two bytes
            int first = ch >> 8;
            int second = ch ^ ( first << 8 );
            baos.write( first );
            baos.write( second );
        }

        // empty word:
        baos.write( 0 );
        baos.write( 0 );
    }

    private void getColorSpecs( RGB rgb, ByteArrayOutputStream baos )
    {
        baos.write( 0 );
        baos.write( 0 );

        int color = (int) ( rgb.getR() * 255.0 );
        baos.write( color );
        baos.write( color );

        color = (int) ( rgb.getG() * 255.0 );
        baos.write( color );
        baos.write( color );

        color = (int) ( rgb.getB() * 255.0 );
        baos.write( color );
        baos.write( color );

        baos.write( 0 );
        baos.write( 0 );
    }

    private void newSection( ByteArrayOutputStream baos )
    {
        //section 2
        baos.write( 0 );
        baos.write( 2 );
        // number of colors; must split into two bytes
        int one = m_noColors >> 8;
        int two = m_noColors ^ ( one << 8 );
        baos.write( one );
        baos.write( two );
    }

    private void constructHeader( ByteArrayOutputStream baos )
    {
//      KH - Dec 17, 2004 : version type; 0 is version 1, 1 is version 2
        //or means section 1
        baos.write( 0 );
        baos.write( 1 );

        // KH - Dec 17, 2004 : number of colors, max is 65535 (16-bit)
        // not checking... who will export 65000 colors? :P
        // must split into two bytes
        int one = m_noColors >> 8;
        int two = m_noColors ^ ( one << 8 );
        baos.write( one );
        baos.write( two );
    }

    public boolean supportsAttributes()
    {
        return false;
    }
}