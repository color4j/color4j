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
 * CDSCommand.java
 *
 * Created on October 14, 2002, 3:07 PM
 */

package org.color4j.spectro.minolta.cm3700d;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.color4j.spectro.spi.LensPosition;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class CDSCommand implements SpectroCommand
{
    private boolean m_Specular;
    private LensPosition m_MeasureArea;
    //private TreeMap m_values;
    private String m_values;

    /**
     * Creates a new instance of CDSCommand
     */
    public CDSCommand( Boolean Specular, LensPosition MeasureArea, String values )
    {
        m_Specular = Specular.booleanValue();
        m_MeasureArea = MeasureArea;
        //m_values = new TreeMap ( values );
        m_values = values;
    }

    public String construct()
    {
        String command = "CDS,";
        String m_valuesresult = "";

        if( m_Specular == true )
        {
            command = command + "0";
        }
        else if( m_Specular = false )
        {
            command = command + "1";
        }

        // Construct the map
        StringTokenizer sTok = new StringTokenizer( m_values, "," );

        try
        {
            for( int i = 0; i < 29; i++ )
            {
                m_valuesresult = m_valuesresult + sTok.nextToken() + ",";
            }

            m_valuesresult = m_valuesresult + sTok.nextToken() + "\r\n";

            for( int i = 0; i < 8; i++ )
            {
                m_valuesresult = m_valuesresult + sTok.nextToken() + ",";
            }

            m_valuesresult = m_valuesresult + sTok.nextToken() + "\r\n";
        }
        catch( NoSuchElementException exp )
        {

        }

        if( m_MeasureArea instanceof LargeAreaView )
        {
            command = command + "0," + m_valuesresult;
        }
        else if( m_MeasureArea instanceof MediumAreaView )
        {
            command = command + "1," + m_valuesresult;
        }
        else if( m_MeasureArea instanceof SmallAreaView )
        {
            command = command + "2," + m_valuesresult;
        }

        // first line for the CDS command
        /*for ( int i = 360; i <= 650; i = i + 10 )
        {
            Double result = (Double) m_values.get ( new Integer ( i ) );
         
            if ( i == 650 )
            {
                if ( result == null )
                    command = command + "000.000,";
                else
                    command = command + result.toString ()+",";
            }
            else
            {
                if ( result == null )
                    command = command + "000.000\r\n";
                else
                    command = command + result.toString ()+"\r\n";
            }
        }
         
        // second line for the CDS command
        for ( int i = 660; i <= 740; i = i + 10 )
        {
            Double result = (Double) m_values.get ( new Integer ( i ) );
         
            if ( i == 650 )
            {
                if ( result == null )
                    command = command + "000.000,";
                else
                    command = command + result.toString ()+",";
            }
            else
            {
                if ( result == null )
                    command = command + "000.000";
                else
                    command = command + result.toString ();
            }
        }*/

        return command;
    }

    public String getName()
    {
        return "White Calibration Data Set Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        String response = new String( values );
        StringTokenizer sTok = new StringTokenizer( response, "\r\n" );

        if( sTok.countTokens() == 2 )
        {
            String line1 = sTok.nextToken();
            String line2 = sTok.nextToken();

            CM3700dStatus status = CM3700dStatus.create( line2 );

            if( line1.equals( "&" ) )
            {
                return new SpectroEvent( this, status );
            }
            else
            {
                status.addError( "MSG_UNKNOWN_STRING" );
                return new SpectroEvent( this, status );
            }
        }
        else
        {
            CM3700dStatus errstatus = CM3700dStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }
}
