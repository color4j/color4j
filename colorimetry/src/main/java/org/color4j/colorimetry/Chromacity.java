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

import org.color4j.colorimetry.encodings.XYZ;

/**
 * This class represent chromacity coordinates
 * <p> Chromacity is dimensions of a color stimilus expressed in terms of hue
 * and saturation.</p>
 */
public class Chromacity
{
    private double x;
    private double y;

    static Chromacity create( double x0, double y0, double z0 )
    {
        double x = x0;
        double y = y0;
        double z = z0;
        double sum = x + y + z;
        if( sum == 0.0 )
        {
            x = 0.0;
            y = 0.0;
        }
        else
        {
            x = x / sum;
            y = y / sum;
        }
        return new Chromacity( x, y );
    }

    static Chromacity create( XYZ xyz )
    {
        return create( xyz.getX(), xyz.getY(), xyz.getZ() );
    }

    static Chromacity create( double x, double y )
    {
        return new Chromacity( x, y );
    }

    private Chromacity( double x, double y )
    {
        this.x = x;
        this.y = y;
    }

    public double get_x()
    {
        return this.x;
    }

    public double get_y()
    {
        return this.y;
    }

    /**
     * Converts the Chromacity coordinates into XYZ tristimulus values
     */
    public XYZ getTristimulus( double Y )
    {

        double X = x;
        double Y2 = y;
        double Y3 = Y;

        double sum;
        if( Y2 == 0.0 )
        {
            sum = 0.0;
        }
        else
        {
            sum = Y3 / Y2;
        }

        double X2 = X * sum;
        double Z = ( 1.0 - X - Y2 ) * sum;

        // y value is not needed in Dr.John's program
        return new XYZ( X2, Y2, Z );
    }
}
