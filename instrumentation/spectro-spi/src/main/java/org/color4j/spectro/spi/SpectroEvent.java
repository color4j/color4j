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

package org.color4j.spectro.spi;

import java.util.EventObject;

/**
 */
public class SpectroEvent
    extends EventObject
{
    public static final String TOTAL_STANDARDS = "number of stored standards";

    private SpectroReading m_Reading;
    private SpectroStatus m_Status;

    private Object m_EventResult;

    public SpectroEvent( Object source )
    {
        super( source );
    }

    public SpectroEvent( Object source, SpectroStatus status )
    {
        super( source );
        m_Status = status;
        m_Reading = null;
    }

    public SpectroEvent( Object source, SpectroReading reading )
    {
        super( source );
        m_Reading = reading;
        m_Status = m_Reading.getStatus();
    }

    public SpectroEvent( Object source, SpectroStatus status, Object value )
    {
        super( source );
        m_Status = status;
        m_EventResult = value;
    }

    public SpectroReading getReading()
    {
        return m_Reading;
    }

    public SpectroStatus getStatus()
    {
        return m_Status;
    }

    public Object getEventResult()
    {
        return m_EventResult;
    }
}