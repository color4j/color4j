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

package org.color4j.colorimetry.weights;

import org.color4j.colorimetry.CalcFunc;
import org.color4j.colorimetry.ColorException;
import org.color4j.colorimetry.Weights;
import org.color4j.colorimetry.entities.Illuminant;
import org.color4j.colorimetry.entities.Observer;
import org.color4j.colorimetry.illuminants.IlluminantImpl;
import org.color4j.colorimetry.interpolation.Lagrange;
import org.color4j.colorimetry.observers.ObserverImpl;
import java.text.NumberFormat;

/**
 * if input illuminant is not 10nm interval with standard(like A D65 C TL84)
 * that store in WeightCache object, then this class is the responsibility for WeightsImpl
 * to generate the weights table for 20 10 5 or 2 or 1 nm interval weight table
 */
public class WeightsImpl
    implements Weights
{
    double[] m_WX;
    double[] m_WY;
    double[] m_WZ;

    int m_Interval;
    int m_Starting;
    int m_Ending;
    boolean m_Bandpass;

    /**
     * call constructor that create D65 1964 observer 10 nm weights
     */
    static public Weights create()
        throws ColorException
    {
        return new WeightsImpl();
    }

    /**
     * call constructor that create illuminant & observer with 10 nm weights
     */
    static public Weights create( Illuminant illuminant, Observer observer )
        throws ColorException
    {
        return new WeightsImpl( illuminant, observer );
    }

    /**
     * call constructor that create illuminant, observer with interval specify
     */
    static public Weights create( Illuminant illuminant, Observer observer, int interval )
        throws ColorException
    {
        return new WeightsImpl( illuminant, observer, interval );
    }

    /**
     * default constructor
     *
     * creates a D65/10 degree
     */
    private WeightsImpl()
        throws ColorException
    {
        Illuminant d65 = IlluminantImpl.create( "D65" );        //NOI18N
        Observer obs = ObserverImpl.create( Observer.NAME_CIE1964 );
        init( d65, obs, 10, false );
    }

    /**
     * constructor
     * create weight with illuminant & observer given
     */

    private WeightsImpl( Illuminant illuminant, Observer observer )
        throws ColorException
    {
        init( illuminant, observer, 10, false );
    }

    /**
     * constructor
     * create weight with illuminant, observer & interval given
     */
    private WeightsImpl( Illuminant illuminant, Observer observer, int interval )
        throws ColorException
    {
        init( illuminant, observer, interval, false );
    }

//     /** constructor
//    * create weight with illuminant, observer, interval & bandpass given
//    */
//    private WeightsImpl(Illuminant illuminant, Observer observer, int interval ,boolean bandpass)
//        throws ColorException
//    {
//        init( illuminant, observer, interval,bandpass );
//    }

    /**
     * all constructor call this method
     * bandpass is the correction of weight
     */
    private void init( Illuminant illuminant,
                       Observer observer,
                       int interval, boolean bandpass
    )
        throws ColorException
    {

//        double[] xyz;
        //int o_S=observer.getShortestWavelength();
        int o_S = 360;
        //int o_E=observer.getLongestWavelength();
        int o_E = 830;
        int i_S = illuminant.getSpectrum().getShortestWavelength();
        int i_E = illuminant.getSpectrum().getLongestWavelength();
        setInterval( interval );
        m_Bandpass = bandpass;

        int os_offset, oe_offset, is_offset, ie_offset;

        if( i_S < o_S )
        {
            is_offset = o_S - i_S;
            os_offset = 0;
            m_Starting = o_S;
        }
        else
        {
            is_offset = 0;
            os_offset = i_S - o_S;
            m_Starting = i_S;
        }
        if( i_E < o_E )
        {
            oe_offset = o_E - i_E;
            ie_offset = 0;
            m_Ending = i_E;
        }
        else
        {
            oe_offset = 0;
            ie_offset = i_E - o_E;
            m_Ending = o_E;
        }
        double[] ill_final = CalcFunc.getSameIntervalR( illuminant.getSpectrum().getValues(), is_offset, ie_offset );
        double[] obs_y_final = CalcFunc.getSameIntervalR( observer.get_y(), os_offset, oe_offset );
        double k = computeK( ill_final, obs_y_final );
        m_WY = compute( ill_final, obs_y_final, interval, k );
        obs_y_final = CalcFunc.getSameIntervalR( observer.get_x(), os_offset, oe_offset );

        m_WX = compute( ill_final, obs_y_final, interval, k );

        obs_y_final = CalcFunc.getSameIntervalR( observer.get_z(), os_offset, oe_offset );
        //System.arraycopy( observer.get_z(), startpos, xyz, 0, length );
        m_WZ = compute( ill_final, obs_y_final, interval, k );
    }

    //to see the interval
    public int getInterval()
    {
        return m_Interval;
    }

    /*whether the output need bandpass correct
     *
     */
    public boolean getBandpass()
    {
        return m_Bandpass;
    }

    public int getShortestWavelength()
    {
        return m_Starting;
    }

    public int getLongestWavelength()
    {
        return m_Ending;
    }

    /**
     * K coefficient to calculate weight value
     */
    private double computeK( double[] s, double[] y )
        throws ColorException
    {
        if( s.length != y.length )
        {
            throw new ColorException( "Internal Error!!!  Spectrum length=" + s.length + " and y.length=" + y.length ); //NOI18N
        }

        double sum = 0.0;
        for( int i = 0; i < s.length; i++ )
        {
            sum += s[ i ] * y[ i ];
        }

        sum = 100.0f / sum;

        return sum;
    }

    /**
     * get X weight factors
     */
    public double[] getWeightsX()
    {
        return m_WX;
    }

    public double[] getWeightsY()
    {
        return m_WY;
    }

    public double[] getWeightsZ()
    {
        return m_WZ;
    }

    /**
     * the first parameter is SPD,the second you may input x[] or y[] or z[]
     * if input x[], you will get weights factor for X, etc...
     * the forth parameter is the k coeffficient get from computeK
     */
    private double[] compute( double[] s, double[] xyz, int interval, double kcoe )
        throws ColorException
    {
        if( s.length != xyz.length )
        {
            throw new ColorException( "Internal Error!!!" );        //NOI18N
        }

        int k = s.length / interval;

        double w[] = new double[ k + 1 ];

        for( int index = 3; index <= k - 3; index++ )
        {
            w[ index ] = 0.0f;

            for( int i = 1; i < interval; i++ )
            {
                w[ index ] += s[ ( index - 2 ) * interval + i ] * xyz[ ( index - 2 ) * interval + i ] * Lagrange.CUBIC_L3[ 20 * i / interval - 1 ]
                              + s[ ( index - 1 ) * interval + i ] * xyz[ ( index - 1 ) * interval + i ] * Lagrange.CUBIC_L2[ 20 * i / interval - 1 ]
                              + s[ index * interval + i ] * xyz[ index * interval + i ] * Lagrange.CUBIC_L1[ 20 * i / interval - 1 ]
                              + s[ ( index + 1 ) * interval + i ] * xyz[ ( index + 1 ) * interval + i ] * Lagrange.CUBIC_L0[ 20 * i / interval - 1 ];
            }
            w[ index ] = ( w[ index ] + s[ index * interval ] * xyz[ index * interval ] ) * kcoe;
        }
        //the first three & the last three difficuit to put in a loop
        //w[0]:the first weights factor, etc

        for( int i = 1; i < interval; i++ )
        {
            w[ 0 ] += s[ i ] * xyz[ i ] * Lagrange.QUADRATIC_L0[ 20 * i / interval - 1 ]
                      + s[ interval + i ] * xyz[ interval + i ] * Lagrange.CUBIC_L0[ 20 * i / interval - 1 ];

            w[ k ] += s[ ( k - 1 ) * interval + i ] * xyz[ ( k - 1 ) * interval + i ] * Lagrange.QUADRATIC_L0[ 19 - 20 * i / interval ]
                      + s[ ( k - 2 ) * interval + i ] * xyz[ ( k - 2 ) * interval + i ] * Lagrange.CUBIC_L0[ 19 - 20 * i / interval ];

            w[ 1 ] += s[ i ] * xyz[ i ] * Lagrange.QUADRATIC_L1[ 20 * i / interval - 1 ]
                      + s[ interval + i ] * xyz[ interval + i ] * Lagrange.CUBIC_L1[ 20 * i / interval - 1 ]
                      + s[ 2 * interval + i ] * xyz[ 2 * interval + i ] * Lagrange.CUBIC_L0[ 20 * i / interval - 1 ];

            w[ k - 1 ] += s[ ( k - 1 ) * interval + i ] * xyz[ ( k - 1 ) * interval + i ] * Lagrange.QUADRATIC_L1[ 19 - 20 * i / interval ]
                          + s[ ( k - 2 ) * interval + i ] * xyz[ ( k - 2 ) * interval + i ] * Lagrange.CUBIC_L1[ 19 - 20 * i / interval ]
                          + s[ ( k - 3 ) * interval + i ] * xyz[ ( k - 3 ) * interval + i ] * Lagrange.CUBIC_L0[ 19 - 20 * i / interval ];

            w[ 2 ] += s[ i ] * xyz[ i ] * Lagrange.QUADRATIC_L2[ 20 * i / interval - 1 ]
                      + s[ interval + i ] * xyz[ interval + i ] * Lagrange.CUBIC_L2[ 20 * i / interval - 1 ]
                      + s[ 2 * interval + i ] * xyz[ 2 * interval + i ] * Lagrange.CUBIC_L1[ 20 * i / interval - 1 ]
                      + s[ 3 * interval + i ] * xyz[ 3 * interval + i ] * Lagrange.CUBIC_L0[ 20 * i / interval - 1 ];

            w[ k - 2 ] += s[ ( k - 1 ) * interval + i ] * xyz[ ( k - 1 ) * interval + i ] * Lagrange.QUADRATIC_L2[ 19 - 20 * i / interval ]
                          + s[ ( k - 2 ) * interval + i ] * xyz[ ( k - 2 ) * interval + i ] * Lagrange.CUBIC_L2[ 19 - 20 * i / interval ]
                          + s[ ( k - 3 ) * interval + i ] * xyz[ ( k - 3 ) * interval + i ] * Lagrange.CUBIC_L1[ 19 - 20 * i / interval ]
                          + s[ ( k - 4 ) * interval + i ] * xyz[ ( k - 4 ) * interval + i ] * Lagrange.CUBIC_L0[ 19 - 20 * i / interval ];
        }

        for( int i = 0; i < 3; i++ )
        {
            w[ i ] = ( w[ i ] + s[ i * interval ] * xyz[ i * interval ] ) * kcoe;
            w[ k - i ] = ( w[ k - i ] + s[ ( k - i ) * interval ] * xyz[ ( k - i ) * interval ] ) * kcoe;
        }

        // is need bandpass correct
        if( m_Bandpass )
        {
            double ww[] = new double[ w.length ];
            for( int i = 0; i < w.length; i++ )
            {
                ww[ i ] = w[ i ];
            }

            for( int i = 1; i < w.length - 1; i++ )
            {
                w[ i ] = -0.083 * ww[ i - 1 ] + 1.166 * ww[ i ] - 0.083 * ww[ i + 1 ];
            }
        }

        return w;
    }

    //you may change you interval here
    private void setInterval( int nm )
        throws IllegalArgumentException
    {
        if( nm != 2 &&
            nm != 4 &&
            nm != 5 &&
            nm != 10 &&
            nm != 20
            )
        {
            throw new IllegalArgumentException( "Interval " + nm + " is not supported. Only 2nm, 4nm, 5nm, 10nm and 20nm intervals are supported." );     //NOI18N
        }
        m_Interval = nm;
    }

    /**
     * print out the weights factor table
     */
    public String toString()
    {
        double[] x = getWeightsX();
        double[] y = getWeightsY();
        double[] z = getWeightsZ();

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( 3 );
        nf.setMinimumFractionDigits( 3 );
        String str = "";    //NOI18N
        if( x != null && y != null && z != null )
        {
            for( int i = 0; i < x.length; i++ )
            {
                if( i != 0 )
                {
                    str = str + ", ";   //NOI18N
                }
                str = str + ( m_Starting + i * m_Interval ) + "[" + nf.format( x[ i ] ) + ", " + nf.format( y[ i ] ) + ", " + nf
                    .format( z[ i ] ) + "]";      //NOI18N
            }
        }
        return str;
    }
}
