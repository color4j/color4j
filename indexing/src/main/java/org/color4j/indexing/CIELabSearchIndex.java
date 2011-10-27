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

/*
 * CIELabSearchIndex.java
 *
 * Created on June 20, 2002, 3:06 PM
 */

package org.color4j.indexing;

import org.color4j.colorimetry.Reflectance;

/**
 * Reflectance colorspace search index for CIELab colorspace
 * <pre>
 *  This class represents a point and radius that define a circle in a particular
 *  colour space. The set of points enclosed by this circle will contain all the
 *  CIELab values for a particular reflectance under different illuminants and observers.
 *  When a query is made with a given CIELab value and delta E, the set of points defined
 * by the query will be compared to this set of points and if they overlap then there
 * is a possibility that this reflectance will meet the search criteria.
 *  </pre>
 */
public interface CIELabSearchIndex
{
    /**
     * The Luminance value of this point
     *
     * @return the Luminance value
     */
    public Double getL();

    /**
     * @return the a coordinate
     */
    public Double geta();

    /**
     * @return the b coordinate
     */
    public Double getb();

    /**
     * @return the radius
     */
    public Double getRadius();

    /* the reflectance on which this searchindex is refering to */
    public Reflectance getReflectance();

    public void setL( Double value );

    public void seta( Double value );

    public void setb( Double value );

    public void setRadius( Double value );

    public void setReflectance( Reflectance refl );
}
