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

package org.color4j.spectro.drivers.sp68;

public class Hex
{
    public static char intToHex( int value )
    {
        if( value > 15 )
        {
            return 'X';
        }
        else if( ( value >= 0 ) && ( value < 10 ) )
        {
            return ( Integer.toString( value ) ).charAt( 0 );
        }
        else
        {
            return (char) ( 'A' + ( value - 10 ) );
        }
    }

    public static int hexToInt( char c )
    {
        if( ( (int) c >= '0' ) && ( (int) c <= '9' ) )
        {
            return (int) c - (int) '0';
        }
        else if( ( (int) c >= 'A' ) && ( (int) c <= 'F' ) )
        {
            return (int) c - (int) 'A' + 10;
        }
        else
        {
            return -1;
        }
    }

    public static String intToHexString( int value )
    {
        StringBuffer buffer = new StringBuffer();

        while( value > 15 )
        {
            int remainder = value % 16;

            buffer.append( intToHex( remainder ) );

            value /= 16;
        }

        buffer.append( intToHex( value ) );

        return ( buffer.reverse() ).toString();
    }

    public static String intToHexString( int value, int length )
    {
        StringBuffer buffer = new StringBuffer( intToHexString( value ) );

        buffer = buffer.reverse();

        while( buffer.length() < length )
        {
            buffer.append( '0' );
        }

        return ( buffer.reverse() ).toString();
    }

    public static int hexStringToInt( String hexString )
    {
        StringBuffer buffer = new StringBuffer( hexString );
        int value = 0;

        buffer = buffer.reverse();

        for( int i = 0; i < buffer.length(); i++ )
        {
            value += hexToInt( buffer.charAt( i ) ) * (int) Math.pow( 16.0, (double) i );
        }

        return value;
    }

    public static void main( String args[] )
    {
        for( int i = 0; i < 17; i++ )
        {
            System.out.print( " " + intToHex( i ) );
        }
        System.out.println();

        for( int i = 0; i < 4; i++ )
        {
            int j = (int) ( Math.random() * 1000 );

            System.out.println( j + " " + intToHexString( j ) );
        }

        for( int i = 0; i < 4; i++ )
        {
            int j = (int) ( Math.random() * 1000 );

            System.out.println( j + " " + intToHexString( j, 8 ) );
        }

        System.out.println( "FFFF " + hexStringToInt( "FFFF" ) );
        System.out.println( "0001 " + hexStringToInt( "0001" ) );
        System.out.println( "0010 " + hexStringToInt( "0010" ) );
        System.out.println( "0100 " + hexStringToInt( "0100" ) );
        System.out.println( "1000 " + hexStringToInt( "1000" ) );
    }
}
