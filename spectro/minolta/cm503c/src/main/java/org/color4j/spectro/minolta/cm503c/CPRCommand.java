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

package org.color4j.spectro.minolta.cm503c;

import java.util.StringTokenizer;
import java.util.Vector;
import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

public class CPRCommand
    implements SpectroCommand
{
    String command;

    public CPRCommand()
    {
        command = "CPR";
    }

    public String construct()
    {
        return command;
    }

    public String getName()
    {
        return "Request Measurement Paramaters Command";
    }

    public SpectroEvent interpret( byte[] values )
    {
        String response = new String( values );

        StringTokenizer sTok = new StringTokenizer( response, "\r\n" );

        if( sTok.countTokens() > 1 )
        {
            String statusString = sTok.nextToken();

            CM503cStatus status = CM503cStatus.create( statusString );

            StringTokenizer paramTok = new StringTokenizer( sTok.nextToken(), "," );

            if( paramTok.countTokens() > 12 )
            {
                Vector message = new Vector();

                for( int i = 0; i < 13; i++ )
                {
                    switch( i )
                    {
                    case 0:
                        parseDisplay( paramTok.nextToken(), message );
                        break;
                    case 1:
                        parseMode( paramTok.nextToken(), message );
                        break;
                    case 2:
                        parseIofCMC( paramTok.nextToken(), message );
                        break;
                    case 3:
                        parsecofCMC( paramTok.nextToken(), message );
                        break;
                    case 4:
                        parseAutoPrint( paramTok.nextToken(), message );
                        break;
                    case 5:
                        parseAutoAverage( paramTok.nextToken(), message );
                        break;
                    case 6:
                        parseDeleteOutlier( paramTok.nextToken(), message );
                        break;
                    case 7:
                        parseAutoSelect( paramTok.nextToken(), message );
                        break;
                    case 8:
                        parseBuzzer( paramTok.nextToken(), message );
                        break;
                    case 9:
                        parseObserver( paramTok.nextToken(), message );
                        break;
                    case 10:
                        parseIlluminant( paramTok.nextToken(), message, 1 );
                        break;
                    case 11:
                        parseIlluminant( paramTok.nextToken(), message, 2 );
                        break;
                    case 12:
                        message.add( "Target number " + paramTok.nextToken() );
                    default:
                        status.addError( "MSG_INVALID_RETURN" );
                        return new SpectroEvent( this, status );
                    }
                }

                return new SpectroEvent( this, status );
            }
            else
            {
                status.addError( "MSG_INVALID_RETURN" );
                return new SpectroEvent( this, status );
            }
        }
        else
        {
            CM503cStatus errstatus = CM503cStatus.create( "INVALID_RETURN" );
            return new SpectroEvent( this, errstatus );
        }
    }

    public void parseDisplay( String param, Vector message )
    {
        if( param.length() > 1 )
        {
            switch( param.charAt( 1 ) )
            {
            case '0':
                message.add( "DISPLAY DIFF & ABS" );
                break;
            case '1':
                message.add( "DISPLAY PASS/FAIL" );
                break;
            case '2':
                message.add( "DISPLAY COLOR GRAPH" );
                break;
            case '3':
                message.add( "DISPLAY DENSITY( A )" );
                break;
            case '4':
                message.add( "DISPLAY DENSITY( T )" );
                break;
            case '5':
                message.add( "DISPLAY SPECTRAL GRAPH" );
                break;
            default:
                message.add( "DISPLAY UNKNOWN" );
            }
        }
        else
        {
            message.add( "DISPLAY UNKNOWN" );
        }
    }

    public void parseMode( String param, Vector message )
    {
        if( param.length() > 1 )
        {
            param = param.replaceAll( "[a-zA-z]+", "" );
            param = param.trim();

            int mode = -1;

            try
            {
                mode = Integer.parseInt( param );
            }
            catch( NumberFormatException numEx )
            {
                mode = -1;
            }

            switch( mode )
            {
            case 0:
                message.add( "MODE L*a*b DE" );
                break;
            case 1:
                message.add( "MODE L*C*h DE" );
                break;
            case 2:
                message.add( "MODE L*C*h CMC" );
                break;
            case 3:
                message.add( "MODE HUNTER Lab" );
                break;
            case 4:
                message.add( "MODE Yxy DE" );
                break;
            case 5:
                message.add( "MODE MUNSELL" );
                break;
            case 6:
                message.add( "MODE Not Used" );
                break;
            case 7:
                message.add( "MODE Not Used" );
                break;
            case 8:
                message.add( "MODE XYZ DE*" );
                break;
            case 9:
                message.add( "MODE Not Used" );
                break;
            case 10:
                message.add( "MODE WI ASTM E313" );
                break;
            case 11:
                message.add( "MODE WI CIE" );
                break;
            case 12:
                message.add( "MODE YI ASTM E313" );
                break;
            case 13:
                message.add( "MODE YI ASTM D1925" );
                break;
            case 14:
                message.add( "MODE B ISO 2470" );
                break;
            default:
                message.add( "MODE UNKNOWN" );
            }
        }
        else
        {
            message.add( "MODE UNKNOWN" );
        }
    }

    public void parseIofCMC( String param, Vector message )
    {
        param = param.replaceAll( "[\\D&&[^.]]+", "" );
        param = param.trim();

        message.add( " I of CMC " + param );
    }

    public void parsecofCMC( String param, Vector message )
    {
        param = param.replaceAll( "[\\D&&[^.]]+", "" );
        param = param.trim();

        message.add( "c of CMC " + param );
    }

    public void parseAutoPrint( String param, Vector message )
    {
        if( param.length() > 0 )
        {
            switch( param.charAt( 0 ) )
            {
            case '0':
                message.add( "AUTO PRINT OFF" );
                return;
            case '1':
                message.add( "AUTO PRINT ON" );
                return;
            }
        }

        message.add( "AUTO PRINT UNKNOWN" );
    }

    public void parseAutoAverage( String param, Vector message )
    {
        param = param.trim();

        if( param.length() > 0 )
        {
            try
            {
                int average = Integer.parseInt( param );

                message.add( "AUTO AVERAGE " + average );
            }
            catch( NumberFormatException numEx )
            {
                message.add( "AUTO AVERAGE UNKNOWN" );
            }
        }
    }

    public void parseDeleteOutlier( String param, Vector message )
    {
        if( param.length() > 0 )
        {
            switch( param.charAt( 0 ) )
            {
            case '0':
                message.add( "DELETE OUTLIER OFF" );
                return;
            case '1':
                message.add( "DELETE OUTLIER ON" );
                return;
            }
        }

        message.add( "DELETE OUTLIER UNKNOWN" );
    }

    public void parseAutoSelect( String param, Vector message )
    {
        if( param.length() > 0 )
        {
            switch( param.charAt( 0 ) )
            {
            case '0':
                message.add( "AUTO SELECT OFF" );
                return;
            case '1':
                message.add( "AUTO SELECT ON" );
                return;
            }
        }
        message.add( "AUTO SELECTE UNKNOWN" );
    }

    public void parseBuzzer( String param, Vector message )
    {
        if( param.length() > 0 )
        {
            switch( param.charAt( 0 ) )
            {
            case '0':
                message.add( "BUZZER OFF" );
                return;
            case '1':
                message.add( "BUZZER ON" );
                return;
            }
        }

        message.add( "BUZZER UNKNOWN" );
    }

    public void parseObserver( String param, Vector message )
    {
        if( param.length() > 0 )
        {
            switch( param.charAt( 0 ) )
            {
            case '0':
                message.add( "OBSERVER 2 DEG" );
                return;
            case '1':
                message.add( "OBSERVER 10 DEG" );
                return;
            }
        }
    }

    public void parseIlluminant( String param, Vector message, int no )
    {
        param = param.replaceAll( "[a-zA-Z]+", "" );
        param = param.trim();

        int illuminant;

        try
        {
            illuminant = Integer.parseInt( param );
        }
        catch( NumberFormatException numEx )
        {
            illuminant = -1;
        }

        switch( illuminant )
        {
        case 0:
            message.add( "ILLUMINANT " + no + " D65" );
            return;
        case 1:
            message.add( "ILLUMINANT " + no + " D50" );
            return;
        case 2:
            message.add( "ILLUMINANT " + no + " C" );
            return;
        case 3:
            message.add( "ILLUMINANT " + no + " A" );
            return;
        case 4:
            message.add( "ILLUMINANT " + no + " F2" );
            return;
        case 5:
            message.add( "ILLUMINANT " + no + " F6" );
            return;
        case 6:
            message.add( "ILLUMINANT " + no + " F7" );
            return;
        case 7:
            message.add( "ILLUMINANT " + no + " F8" );
            return;
        case 8:
            message.add( "ILLUMINANT " + no + " F10" );
            return;
        case 9:
            message.add( "ILLUMINANT " + no + " F11" );
            return;
        case 10:
            message.add( "ILLUMINANT " + no + " F12" );
            return;
        case 11:
            message.add( "ILLUMINANT " + no + " ---" );
            return;
        }

        message.add( "ILLUMINANT " + no + " UNKNOWN" );
    }
}
