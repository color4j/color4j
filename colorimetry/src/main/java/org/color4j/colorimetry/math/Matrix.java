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

package org.color4j.colorimetry.math;

/*
 * taken from matrix class in ch.idx3d.engine3d.Matrix
 *
 */
public class Matrix
{
    // M A T R I X   D A T A

    public final float m00, m01, m02, m03;
    public final float m10, m11, m12, m13;
    public final float m20, m21, m22, m23;
    public final float m30, m31, m32, m33;

    private Matrix cachedInverted;

    public Matrix( float[][] a )
    {
        m00 = a[ 0 ][ 0 ];
        m01 = a[ 0 ][ 1 ];
        m02 = a[ 0 ][ 2 ];
        m03 = a[ 0 ][ 3 ];
        m10 = a[ 1 ][ 0 ];
        m11 = a[ 1 ][ 1 ];
        m12 = a[ 1 ][ 2 ];
        m13 = a[ 1 ][ 3 ];
        m20 = a[ 2 ][ 0 ];
        m21 = a[ 2 ][ 1 ];
        m22 = a[ 2 ][ 2 ];
        m23 = a[ 2 ][ 3 ];
        m30 = a[ 3 ][ 0 ];
        m31 = a[ 3 ][ 1 ];
        m32 = a[ 3 ][ 2 ];
        m33 = a[ 3 ][ 3 ];
    }

    public Matrix inverse()
    {
        if( cachedInverted == null )
        {
            float q1 = m12;
            float q6 = m10 * m01;
            float q7 = m10 * m21;
            float q8 = m02;
            float q13 = m20 * m01;
            float q14 = m20 * m11;
            float q21 = m02 * m21;
            float q22 = m03 * m21;
            float q25 = m01 * m12;
            float q26 = m01 * m13;
            float q27 = m02 * m11;
            float q28 = m03 * m11;
            float q29 = m10 * m22;
            float q30 = m10 * m23;
            float q31 = m20 * m12;
            float q32 = m20 * m13;
            float q35 = m00 * m22;
            float q36 = m00 * m23;
            float q37 = m20 * m02;
            float q38 = m20 * m03;
            float q41 = m00 * m12;
            float q42 = m00 * m13;
            float q43 = m10 * m02;
            float q44 = m10 * m03;
            float q45 = m00 * m11;
            float q48 = m00 * m21;
            float q49 = q45 * m22 - q48 * q1 - q6 * m22 + q7 * q8;
            float q50 = q13 * q1 - q14 * q8;
            float q51 = 1 / ( q49 + q50 );

            float[][] im = new float[ 4 ][ 4 ];
            im[ 0 ][ 0 ] = ( m11 * m22 * m33 - m11 * m23 * m32 - m21 * m12 * m33 + m21 * m13 * m32 + m31 * m12 * m23 - m31 * m13 * m22 ) * q51;
            im[ 0 ][ 1 ] = -( m01 * m22 * m33 - m01 * m23 * m32 - q21 * m33 + q22 * m32 ) * q51;
            im[ 0 ][ 2 ] = ( q25 * m33 - q26 * m32 - q27 * m33 + q28 * m32 ) * q51;
            im[ 0 ][ 3 ] = -( q25 * m23 - q26 * m22 - q27 * m23 + q28 * m22 + q21 * m13 - q22 * m12 ) * q51;
            im[ 1 ][ 0 ] = -( q29 * m33 - q30 * m32 - q31 * m33 + q32 * m32 ) * q51;
            im[ 1 ][ 1 ] = ( q35 * m33 - q36 * m32 - q37 * m33 + q38 * m32 ) * q51;
            im[ 1 ][ 2 ] = -( q41 * m33 - q42 * m32 - q43 * m33 + q44 * m32 ) * q51;
            im[ 1 ][ 3 ] = ( q41 * m23 - q42 * m22 - q43 * m23 + q44 * m22 + q37 * m13 - q38 * m12 ) * q51;
            im[ 2 ][ 0 ] = ( q7 * m33 - q30 * m31 - q14 * m33 + q32 * m31 ) * q51;
            im[ 2 ][ 1 ] = -( q48 * m33 - q36 * m31 - q13 * m33 + q38 * m31 ) * q51;
            im[ 2 ][ 2 ] = ( q45 * m33 - q42 * m31 - q6 * m33 + q44 * m31 ) * q51;
            im[ 2 ][ 3 ] = -( q45 * m23 - q42 * m21 - q6 * m23 + q44 * m21 + q13 * m13 - q38 * m11 ) * q51;
            im[ 3 ][ 0 ] = 0.0f;
            im[ 3 ][ 1 ] = 0.0f;
            im[ 3 ][ 2 ] = 0.0f;
            im[ 3 ][ 3 ] = 1.0f;
            cachedInverted = new Matrix( im );
        }
        return cachedInverted;
    }
}
