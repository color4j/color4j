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

package org.color4j.colorimetry.matching;

import org.color4j.colorimetry.ColorException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public abstract class ColorDifference
{
    public static final String DELTA_E = "DeltaE";  //NOI18N
    public static final String DELTA_L_s = "DeltaLStar";  //NOI18N
    public static final String DELTA_a_s = "DeltaaStar";  //NOI18N
    public static final String DELTA_b_s = "DeltabStar";  //NOI18N
    public static final String DELTA_c_s = "DeltaCStar";  //NOI18N
    public static final String DELTA_h_s = "DeltaHStar";  //NOI18N
    public static final String DELTA_u_s = "DeltauStar";  //NOI18N
    public static final String DELTA_v_s = "DeltavStar";  //NOI18N
    public static final String DELTA_L = "DeltaL";  //NOI18N
    public static final String DELTA_c = "DeltaC";  //NOI18N
    public static final String DELTA_h = "DeltaH";  //NOI18N

    private DifferenceAlgorithm m_Algorithm;

    protected ColorDifference( DifferenceAlgorithm algorithm )
    {
        m_Algorithm = algorithm;
    }

    public DifferenceAlgorithm getAlgorithm()
    {
        return m_Algorithm;
    }

    public String[] getAllValueNames()
    {
        ArrayList result = new ArrayList();
        Method[] methods = getClass().getMethods();
        for( int i = 0; i < methods.length; i++ )
        {
            if( methods[ i ].getName().startsWith( "get" ) &&     //NOI18N
                methods[ i ].getParameterTypes().length == 0 )
            {
                result.add( methods[ i ].getName().substring( 3 ) );
            }
        }
        String[] names = new String[ result.size() ];
        for( int i = 0; i < names.length; i++ )
        {
            names[ i ] = (String) result.get( i );
        }
        return names;
    }

    public double getValue( String name )
        throws ColorException
    {
        try
        {
            Method method = getClass().getMethod( "get" + name, null );     //NOI18N
            Object obj = method.invoke( this, null );
            return ( (Number) obj ).doubleValue();
        }
        catch( IllegalAccessException e )
        {
            throw new ColorException( "Method declared is not public.", e );
        }
        catch( NoSuchMethodException e )
        {
            throw new ColorException( "No such method available for this calculator.", e );
        }
        catch( InvocationTargetException e )
        {
            Throwable t = e.getTargetException();
            throw new ColorException( "Exception occured when calculating.", t );
        }
    }
}