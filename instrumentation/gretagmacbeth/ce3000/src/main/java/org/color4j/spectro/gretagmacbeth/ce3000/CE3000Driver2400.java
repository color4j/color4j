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

/*
 * Created on Oct 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.color4j.spectro.gretagmacbeth.ce3000;

import org.color4j.spectro.spi.DriverManager;
import org.color4j.spectro.spi.Spectrophotometer;

public class CE3000Driver2400 extends CE3000Driver
{

    static
    {
        DriverManager manager = DriverManager.getInstance();
        try
        {
            manager.registerDriver( CE3000Driver2400.class );
        }
        catch( org.color4j.spectro.spi.SpectroException ex )
        {
            System.err.println( "Unable to register CE3000 Driver" );
            ex.printStackTrace( System.err );
        }
    }

    protected Spectrophotometer getSpectroPhotometer()
    {
        // TODO Auto-generated method stub
        return new CE3000Spectro2400();
    }

    public String getName()
    {
        return "CE3000 - 2400bps";
    }
}
