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

package org.color4j.spectro.hunter.common;

/**
 * The Data Control Message are command messages sent by
 * the host/driver software to the Hunter Labs ColorQuest XE
 * The Data Control Message is of SOH class typically identified
 * in the header with 0x01.
 */
public class DataControlMessage
    extends Message
{
    /**
     * Constructs a Data Control message by parsing a string
     *
     * @param message The string representation of the message.
     */
    public DataControlMessage( String message )
    {
        super( message );
    }

    /**
     * Constructs a Data Control message given the paramter block
     * and message ID.
     *
     * @param ID        The message ID for the message.
     * @param parameter The parameter block for the message.
     */
    public DataControlMessage( int ID, String parameter )
    {
        super( ID, parameter );
    }

    /**
     * Constructs a Data Control Message header
     * This is characterised by the class byte as 0x01.
     *
     * @param ID     The message ID for the message.
     * @param length The length of the parameter block.
     *
     * @returns String representation of the header.
     */
    public String constructHeader( int ID, int length )
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( (char) 0x01 );

        buffer.append( Hex.intToHexString( length, 3 ) );

        buffer.append( Hex.intToHex( ID ) );

        return buffer.toString();
    }

    /**
     * Main method used to test the Data Control Message object
     * -Attempt to construct Data Control Message given ID and parameter block.
     * -Prints out the string representation for verification.
     *
     * -Attempt to construct Data Control Message by parsing a given string.
     * -The given string contains a valid checksum.
     * -Prints out the string representation for verification.
     * -Checks that the parsed message contains a valid checksum
     *
     * -Attempt to construct Data Control Message by parsing a given string.
     * -The given string contains an invalid checksum.
     * -Prints out the string representation for verification.
     * -Checks that the parsed message contains a valid checksum
     */
    public static void main( String args[] )
    {
        DataControlMessage msg = new DataControlMessage( 1, "Z" );

        System.out.println( "Constructed message : " + msg );

        msg = new DataControlMessage( (char) 0x01 + "0011H010B" );

        System.out.println( "Parsed message : " + msg );

        if( msg.verifyChecksum() )
        {
            System.out.println( "Checksum OK" );
        }
        else
        {
            System.out.println( "Checksum error" );
        }

        msg = new DataControlMessage( (char) 0x01 + "0011H010A" );

        System.out.println( "Parsed message : " + msg );

        if( msg.verifyChecksum() )
        {
            System.out.println( "Checksum OK" );
        }
        else
        {
            System.out.println( "Checksum error" );
        }
    }
}
