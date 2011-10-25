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

package org.color4j.colorimetry;

/**
 * Interface for an N-dimensional point object.
 *
 */
public interface PointND
{
    public void setDim( int dimIdx, double val );

    public void setNumDims( int dims );

    public int getNumDims();

    public double getDim( int dimIdx );

    public double[] getDims();

    public void setName( String name );

    public String getName();

    public double distFromOrigin();
}
