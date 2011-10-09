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
 * ManualReadingEvent.java
 *
 * Created on May 24, 2002, 2:52 PM
 */

package org.color4j.spectro.drivers.manual;

import java.util.EventObject;
import java.util.Map;

/**
 * @author hywong
 */
public class ManualReadingEvent
    extends EventObject
{
    Map m_Values = null;

    /**
     * Creates a new instance of ManualReadingEvent
     */
    public ManualReadingEvent( Object source )
    {
        super( source );
    }

    public ManualReadingEvent( Object source, Map values )
    {
        super( source );
        m_Values = values;
    }

    public Map getValues()
    {
        return m_Values;
    }
}
