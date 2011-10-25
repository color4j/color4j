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

package org.color4j.colorimetry.entities;

import java.util.SortedMap;

public interface Observer
{
    public final String NAME_10_DEGREES = "10";   //NOI18N
    public final String NAME_2_DEGREES = "2";     //NOI18N

    public final String NAME_CIE1964 = "10";      //NOI18N
    public final String NAME_CIE1931 = "2";       //NOI18N

    /**
     * Returns the Name of the Observer
     */

    String getName();

    /**
     * Returns the tristimulus sensitivity of the observer.
     * <p>The <code>SortedMap</code> has the wavelength in the
     * key represented as a <code>Number</code> and a XYZ
     * object as the value.
     */

    SortedMap getTristimulus();

    /**
     * Returns the array/function of tristimulus x.
     * <p>The array starts at 360nm and its interval is every 1nm.</p>
     */

    double[] get_x();

    /**
     * Returns the array/function of tristimulus y.
     * <p>The array starts at 360nm and its interval is every 1nm.</p>
     */

    double[] get_y();

    /**
     * Returns the array/function of tristimulus z.
     * <p>The array starts at 360nm and its interval is every 1nm.</p>
     */

    double[] get_z();

    /**
     * Returns the viewing angle of the Standard Observer
     */

    double getAngle();
}
