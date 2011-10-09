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

package org.color4j.spectro.spi.helpers;

import java.util.EventObject;

public class CommDriverEvent extends EventObject
{
    private int m_status;
    public static int COMM_DRIVER_SENDING = 0;
    public static int COMM_DRIVER_SENT = 1;
    public static int COMM_DRIVER_RECEIVED = 2;
    public static int COMM_DRIVER_TIMEOUT = 3;

    public CommDriverEvent( Object source )
    {
        super( source );
    }

    public CommDriverEvent( Object source, int status )
    {
        super( source );
        m_status = status;
    }

    public int getStatus()
    {
        return m_status;
    }
}
