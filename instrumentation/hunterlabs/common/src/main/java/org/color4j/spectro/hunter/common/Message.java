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
 * The abstract Message class for the Hunter Labs ColorQuest XE
 * Communication Protocol.<BR/>
 * Message is based on the Hunter Labs ColorQuest XE Communication Protocol.<BR/>
 *
 * Each message sent from the host to the instrument has the following structure:<BR/>
 *
 * <PRE>
 * |      HEADER            |            PARAMETERS        |     CHECKSUM    |
 * </PRE>
 *
 * HEADER consists of 5 Bytes where: <BR/>
 * 1 Byte is allocated for Class; indicating the type of message <BR/>
 * 3 Bytes is allocated for Count; indicating the length of the paremeter block <BR/>
 * 1 Byte is allocated for MsgNo; indicating the message number <BR/>
 *
 *
 * PARAMER consists of at least 1 Byte, indicating the type of parameter, up to the length indicated by Count in the header
 *
 * CHECKSUM consists of 4 Bytes which is the sum modulo 16-bit characters in the header and parameter block
 */
public abstract class Message
{
    String m_Header = null;
    String m_Parameter = null;
    String m_Checksum = null;

    /**
     * Constructs a new message with the given parameter and ID
     *
     * @param ID        The ID number of the message
     * @param parameter The parameter of the message
     */
    public Message( int ID, String parameter )
    {
        m_Header = constructHeader( ID, parameter.length() );

        m_Parameter = parameter;

        m_Checksum = constructChecksum();
    }

    /**
     * Constructs a new message by parsing a string representation
     *
     * @param message The String representation of the message
     */
    public Message( String message )
    {

        m_Header = parseHeader( message );
        //System.out.println( "Header parsed" );
        //javax.swing.JOptionPane.showMessageDialog( null, "Message : " + message );
        m_Parameter = parseParameter( message );
        //System.out.println( "Parameter parsed" );
        m_Checksum = parseChecksum( message );

        //System.out.println( "Message constructed" );
    }

    /**
     * Returns the header of the message
     *
     * @return The String representation of the header
     */
    public String getHeader()
    {
        return m_Header;
    }

    /**
     * Returns the type or class of the message
     *
     * @returns The character representing the type or class
     */
    public char getType()
    {
        return m_Header.charAt( 0 );
    }

    /**
     * Return the length of the paremeter block
     *
     * @returns The integer length of the parameter block.
     */
    public int getLength()
    {
        return Hex.hexStringToInt( m_Header.substring( 1, 4 ) );
    }

    /**
     * Returns the ID of the message
     *
     * @returns The integer ID of the message.
     */
    public int getID()
    {
        return Hex.hexToInt( m_Header.charAt( 4 ) );
    }

    /**
     * Returns the paramter of the message
     *
     * @returns The String representation of the parameter
     */
    public String getParameter()
    {
        return m_Parameter;
    }

    /**
     * Returns the checksum block of the message
     *
     * @returns The String representation of the message
     */
    public String getChecksum()
    {
        return m_Checksum;
    }

    /**
     * Parses the header from a message
     *
     * @returns The parsed header of the message.
     */
    public static String parseHeader( String message )
    {
        //System.out.println( "Parsing header..." );
        return message.substring( 0, 5 );
    }

    /**
     * Parses the paramter from a message
     *
     * @returns The parsed parameter of the message.
     */
    public static String parseParameter( String message )
    {
        //System.out.println( "Parsing parameter..." );
        int length = Hex.hexStringToInt( message.substring( 1, 4 ) );

        StringBuffer buffer = new StringBuffer();

        StringBuffer mesg = new StringBuffer();

        mesg.append( "Message : " + message );
        mesg.append( "Length  : " + message.substring( 1, 4 ) );
        mesg.append( "Length(): " + length );

        //System.out.println( mesg.toString() );

        //javax.swing.JOptionPane.showMessageDialog( null, mesg.toString() );

        for( int i = 0; i < length; i++ )
        {
            buffer.append( message.charAt( 5 + i ) );
        }

        return buffer.toString();
    }

    /**
     * Parses the checksum from a message
     *
     * @returns The parsed checksum of the message
     */
    public static String parseChecksum( String message )
    {
        //System.out.println( "Parsing checksum..." );
        StringBuffer buffer = new StringBuffer();
        StringBuffer source = new StringBuffer( message );

        source = source.reverse();

        for( int i = 0; i < 4; i++ )
        {
            buffer.append( source.charAt( i ) );
        }

        return ( buffer.reverse() ).toString();
    }

    /**
     * Verifies the checksum block of the message
     *
     * @returns boolean true if the checksum is verified; false if not.
     */
    public boolean verifyChecksum()
    {
        String checksum = constructChecksum();

        if( m_Checksum.equalsIgnoreCase( checksum ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns the String representation of the message.
     *
     * @return String representation of the message.
     */
    public String toString()
    {
        return new String( m_Header + m_Parameter + m_Checksum );
    }

    /**
     * Since the only differences between the different types of messages is
     * only visible through its' headers; the header construction is left
     * abstract.
     */
    public abstract String constructHeader( int ID, int length );

    /**
     * Constructs and returns the checksum for the message.
     *
     * @returns The String representation of the checksum block for the message.
     */
    public String constructChecksum()
    {
        int checksum = 0;

        for( int i = 0; i < m_Header.length(); i++ )
        {
            checksum += (int) m_Header.charAt( i );
        }

        for( int i = 0; i < m_Parameter.length(); i++ )
        {
            checksum += (int) m_Parameter.charAt( i );
        }

        checksum = checksum % 65536;

        return Hex.intToHexString( checksum, 4 );
    }
}
