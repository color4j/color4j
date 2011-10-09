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
 * The Binary class models the behaviour or
 * binary behaviour and provide binary operations
 */
public class Binary
    extends Number
{
    /**
     * Exception thrown when the length of
     * one or more Binary class objects operated on are
     * of different lenghts.
     */
    public class LengthMismatchException
        extends Exception
    {
    }

    public class BinaryLengthException
        extends Exception
    {
    }

    /**
     * boolean array representing a binary number *
     */
    protected boolean[] bits;

    /**
     * Constructs a new hollow Binary object *
     */
    public Binary()
    {
        super();
    }

    /**
     * Constructs a new Binary object with the given value
     *
     * @param value The initial value of the Binary object.
     */
    public Binary( int value )
    {
        setValue( value );
    }

    /**
     * Constructs a new Binary object by parsing a string representation
     *
     * @param binaryBits A string representation of a binary number
     */
    public Binary( String binaryBits )
    {
        bits = new boolean[ binaryBits.length() ];

        for( int i = 0; i < binaryBits.length(); i++ )
        {
            if( binaryBits.charAt( i ) == '0' )
            {
                bits[ i ] = false;
            }
            else if( binaryBits.charAt( i ) == '1' )
            {
                bits[ i ] = true;
            }
        }
    }

    /**
     * Returns the double value of the Binary
     *
     * @returns The double value of the Binary
     */
    public double doubleValue()
    {
        double result = 0.0;

        for( int i = 0; i < bits.length; i++ )
        {
            if( bits[ i ] )
            {
                result += Math.pow( 2.0, (double) ( bits.length - 1 - i ) );
            }
        }

        return result;
    }

    /**
     * Returns the double value of the Binary by treating it
     * as a signed Binary number.
     *
     * @returns The double value of the signed Binary
     */
    public double signedDoubleValue()
    {
        if( bits[ 0 ] )
        {
            bits[ 0 ] = false;
            Binary comp = compliment();
            return ( -comp.doubleValue() );
        }
        else
        {
            return doubleValue();
        }
    }

    /**
     * Returns the float value of the Binary.
     *
     * @returns The float value of the Binary.
     */
    public float floatValue()
    {
        return (float) doubleValue();
    }

    /**
     * Returns the integer value of the Binary
     *
     * @returns The integer value of the Binary
     */
    public int intValue()
    {
        return (int) doubleValue();
    }

    /**
     * Returns the long value of the Binary
     *
     * @returns The long value of the Binary
     */
    public long longValue()
    {
        return (long) doubleValue();
    }

    /**
     * Returns the length of the Binary
     *
     * @returns The bit length of the Binary.
     */
    public int length()
    {
        return bits.length;
    }

    /**
     * Returns a bit value of the Binary
     *
     * @param position The position of the bit to be returned
     *
     * @returns The target bit value
     */
    public int getBit( int position )
    {
        if( position < length() )
        {
            return bits[ position ] ? 1 : 0;
        }
        else
        {
            return -1;
        }
    }

    /**
     * Extends the Binary by a given number of bits.
     * Extension is filled with 0s.
     *
     * @param length The number of bits to extend the Binary
     */
    public void extend( int length )
    {
        int newLength = length + bits.length;
        boolean[] newBits = new boolean[ newLength ];

        for( int i = 0; i < newBits.length; i++ )
        {
            if( i < length )
            {
                newBits[ i ] = false;
            }
            else
            {
                newBits[ i ] = bits[ i - length ];
            }
        }

        bits = newBits;
    }

    /**
     * Extends the Binary to the desired length.
     * Extension is filled with 0's
     *
     * @param length The desired length of the Binary
     */
    public void extendTo( int length )
    {
        if( length > bits.length )
        {
            extend( length - bits.length );
        }
    }

    public void trim()
    {
        double value = doubleValue();
        bits = null;
        setValue( (int) value );
    }

    /**
     * Compliments the Binary by inverting 1s to 0s and vice versa
     *
     * @returns The complimented Binary.
     */
    public Binary compliment()
    {
        Binary result = createBinary( length() );

        for( int i = 0; i < bits.length; i++ )
        {
            result.bits[ i ] = !bits[ i ];
        }

        return result;
    }

    /**
     * Shifts the Binary to the right by the intended steps.
     * Rightmost bits are truncated by the shift.
     *
     * @param step The number of bits to be shifted.
     *
     * @returns The right shifted Binary.
     */
    public Binary rightShiftTrunc( int step )
    {
        Binary result = createBinary( this.length() );

        for( int i = result.length() - 1; i >= 0; i-- )
        {
            if( ( i - step ) < 0 )
            {
                result.bits[ i ] = false;
            }
            else
            {
                result.bits[ i ] = bits[ i - step ];
            }
        }

        return result;
    }

    /**
     * Shifts the Binary to the left by the intended steps.
     * Leftmost bits are truncated by the shift.
     *
     * @param setp The number of bits to be shifted.
     *
     * @returns The left shifted Binary
     */
    public Binary leftShiftTrunc( int step )
    {
        Binary result = createBinary( this.length() );

        for( int i = 0; i < result.length(); i++ )
        {
            if( ( i + step ) >= result.length() )
            {
                result.bits[ i ] = false;
            }
            else
            {
                result.bits[ i ] = bits[ i + step ];
            }
        }

        return result;
    }

    /**
     * Performs an AND operation with another Binary
     * If length of the Binaries mismatches both are extended
     * to the longest length of the two Binaries.
     *
     * @param anotherBinary The target Binary to AND
     *
     * @returns The result of the AND operation.
     */
    public Binary and( Binary anotherBinary )
    {
        if( this.length() > anotherBinary.length() )
        {
            anotherBinary.extendTo( this.length() );
        }
        else
        {
            this.extendTo( anotherBinary.length() );
        }

        Binary result = createBinary( this.length() );

        for( int i = 0; i < bits.length; i++ )
        {
            result.bits[ i ] = bits[ i ] && anotherBinary.bits[ i ];
        }

        return result;
    }

    /**
     * Performs an OR operation with another Binary
     * If length of the Binaries mismatches both are extended
     * to the longest length of the two Binaries.
     *
     * @param anotherBinary The target Binary to OR
     *
     * @returns The result of the OR operation
     */
    public Binary or( Binary anotherBinary )
    {
        if( this.length() > anotherBinary.length() )
        {
            anotherBinary.extendTo( this.length() );
        }
        else
        {
            this.extendTo( anotherBinary.length() );
        }

        Binary result = createBinary( this.length() );

        for( int i = 0; i < bits.length; i++ )
        {
            result.bits[ i ] = bits[ i ] || anotherBinary.bits[ i ];
        }

        return result;
    }

    /**
     * Performs an XOR operation with another Binary
     * If length of the Binaries mismatches both are extended
     * to the longest length of the two Binaries.
     *
     * @param anotherBinary The target Binary to XOR
     *
     * @returns The result of the XOR operation.
     */
    public Binary xor( Binary anotherBinary )
    {
        if( this.length() > anotherBinary.length() )
        {
            anotherBinary.extendTo( this.length() );
        }
        else
        {
            this.extendTo( anotherBinary.length() );
        }

        Binary result = createBinary( this.length() );

        for( int i = 0; i < bits.length; i++ )
        {
            if( bits[ i ] && anotherBinary.bits[ i ] )
            {
                result.bits[ i ] = false;
            }
            else if( bits[ i ] || anotherBinary.bits[ i ] )
            {
                result.bits[ i ] = true;
            }
            else
            {
                result.bits[ i ] = false;
            }
        }

        return result;
    }

    /**
     * Returns the String representation of the Binary
     *
     * @returns The String representation.
     */
    public String toString()
    {
        StringBuffer buff = new StringBuffer();

        for( int i = 0; i < bits.length; i++ )
        {
            if( bits[ i ] )
            {
                buff.append( "1" );
            }
            else
            {
                buff.append( "0" );
            }
        }

        return buff.toString();
    }

    /**
     * Executable to verify the Binary class <BR/>
     *
     * -Instantiates various Binary objects and print out for verification.
     * -Performs various operations and print out for verification.
     */
    public static void main( String args[] )
    {
        Binary b = new Binary( 15 );
        System.out
            .println( b + "  " + b.doubleValue() + " " + b.floatValue() + " " + b.longValue() + " " + b.intValue() );
        b.extendTo( 8 );
        System.out.println( b );
        b.trim();
        System.out.println( b );
        b.extendTo( 16 );
        System.out.println( b );
        b.compliment();
        System.out.println( b );

        Binary b1 = new Binary( 9 );
        Binary b2 = new Binary( 8 );

        System.out.println( b1 + " AND " + b2 );
        System.out.println( b1.and( b2 ) + " " + ( b1.and( b2 ) ).doubleValue() );
        System.out.println( b1 + " OR " + b2 );
        System.out.println( b1.or( b2 ) + " " + ( b1.or( b2 ) ).doubleValue() );
        System.out.println( b1 + " XOR " + b2 );
        System.out.println( b1.xor( b2 ) + " " + ( b1.xor( b2 ) ).doubleValue() );
        System.out.println( b1 + " << 2 " );
        System.out.println( b1.leftShiftTrunc( 2 ) + "  " + ( b1.leftShiftTrunc( 2 ) ).doubleValue() );
        System.out.println( b2 + " >> 2 " );
        System.out.println( b2.rightShiftTrunc( 2 ) + " " + ( b2.rightShiftTrunc( 2 ) ).doubleValue() );

        Binary b3 = new Binary( -2 );
        System.out.println( b3 + " " + b3.doubleValue() + " " + b3.signedDoubleValue() );

        Binary b4 = new Binary( -16 );
        System.out.println( b4 + " " + b4.doubleValue() + " " + b4.signedDoubleValue() );

        Binary b5 = new Binary( -17 );
        System.out.println( b5 + " " + b5.doubleValue() + " " + b5.signedDoubleValue() );

        Binary b6 = new Binary( "10111011" );
        System.out.println( b6 + " " + b6.doubleValue() + " " + b6.signedDoubleValue() );
    }

    /**
     * Internal static method for creation of a new Binary
     * of an intended length.
     *
     * @param length The intended length of the Binary
     *
     * @returns The new Binary.
     */
    protected static Binary createBinary( int length )
    {
        Binary result = new Binary();

        result.bits = new boolean[ length ];

        for( int i = 0; i < result.bits.length; i++ )
        {
            result.bits[ i ] = false;
        }

        return result;
    }

    /**
     * Sets the value of the Binary given an integer
     *
     * @param value The intended value of the Binary
     */
    protected void setValue( int value )
        throws NumberFormatException
    {
        StringBuffer buff = new StringBuffer();
        int result = value;

        if( value < 0 )
        {
            result = -result - 1;
        }

        while( result > 1 )
        {
            if( ( result % 2 ) == 1 )
            {
                buff.append( "1" );
            }
            else
            {
                buff.append( "0" );
            }

            result /= 2;
        }

        if( result == 0 )
        {
            buff.append( "0" );
        }
        else
        {
            buff.append( "1" );
        }

        if( bits == null )
        {
            bits = new boolean[ buff.length() ];
            for( int i = 0; i < buff.length(); i++ )
            {
                char bit = buff.charAt( buff.length() - 1 - i );

                if( bit == '0' )
                {
                    bits[ i ] = false;
                }
                else if( bit == '1' )
                {
                    bits[ i ] = true;
                }
            }
        }
        else
        {
            if( bits.length >= buff.length() )
            {
                for( int i = 0; i < buff.length(); i++ )
                {
                    if( buff.charAt( i ) == '1' )
                    {
                        bits[ bits.length - 1 - i ] = true;
                    }
                    else if( buff.charAt( i ) == '0' )
                    {
                        bits[ bits.length - 1 - i ] = false;
                    }
                    else
                    {
                        throw new NumberFormatException();
                    }
                }
            }
        }

        if( value < 0 )
        {
            extendTo( length() + 1 );
            bits[ 0 ] = true;
        }
    }
}
