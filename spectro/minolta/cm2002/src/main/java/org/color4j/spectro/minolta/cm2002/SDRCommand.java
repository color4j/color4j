/*
 * Copyright (c) 2011 Niclas Hedhman.
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
 * SDRCommand.java
 *
 * Created on October 14, 2002, 3:30 PM
 */

package org.color4j.spectro.minolta.cm2002;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author chc
 */
public class SDRCommand implements SpectroCommand
{
    private String m_Specimen;

    private String m_SpecimenNumber;
    private String[] m_Reflectance;
    private String m_DataID;
    private String m_Comment;
    private String[] m_Colormode;

    /**
     * Creates a new instance of SDRCommand
     */
    public SDRCommand( String Specimen )
    {
        m_Specimen = Specimen;
    }

    public String construct()
    {
        return "SDR\r\n" + m_Specimen;
    }

    public String getName()
    {
        return "Specimen Data Request Command";
    }

    public org.color4j.spectro.spi.SpectroEvent interpret( byte[] values )
    {
        String response = new String( values );
        StringTokenizer sTok = new StringTokenizer( response, "\r\n" );

        String[] result = new String[ sTok.countTokens() ];

        for( int i = 0; i < sTok.countTokens(); i++ )
        {
            result[ i ] = sTok.nextToken();
        }

        CM2002Status status = CM2002Status.create( result[ 0 ] );

        if( status.isSuccess() )
        {
            try
            {
                if( "S".equals( result[ 1 ] ) && "E".equals( result[ sTok.countTokens() ] ) )
                {
                    m_SpecimenNumber = result[ 2 ];

                    for( int i = 3; i < 34; i++ )
                    {
                        m_Reflectance[ i - 3 ] = result[ i ];
                    }

                    m_DataID = result[ 34 ];
                    m_Comment = result[ 35 ];

                    for( int i = 36; i < sTok.countTokens(); i++ )
                    {
                        m_Colormode[ i - 36 ] = result[ i ];
                    }
                }
                else
                {
                    CM2002Status errstatus = CM2002Status.create( "INVALID_DATA" );
                    return new SpectroEvent( this, errstatus );
                }
            }
            catch( NoSuchElementException exception )
            {
                CM2002Status errstatus = CM2002Status.create( "INVALID_RETURN" );
                return new SpectroEvent( this, errstatus );
            }
        }

        return new SpectroEvent( this, status );
    }

    public String getSpecimenNumber()
    {
        return m_SpecimenNumber;
    }

    public String[] getReflectance()
    {
        return m_Reflectance;
    }

    public String getDataID()
    {
        return m_DataID;
    }

    public String getComment()
    {
        return m_Comment;
    }

    public String[] getColormode()
    {
        return m_Colormode;
    }
}
