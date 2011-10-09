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

package org.color4j.spectro.drivers.manual;

import java.util.Collection;
import java.util.Vector;
import org.color4j.spectro.spi.SpectroStatus;

public class ManualStatus
    implements SpectroStatus
{
    protected boolean m_Error;
    protected Vector m_Warnings;
    protected Vector m_Messages;
    protected Vector m_ErrorMessages;

    public ManualStatus( boolean error )
    {
        m_Error = error;
        m_Warnings = new Vector();
        m_Messages = new Vector();
        m_ErrorMessages = new Vector();
    }

    public boolean isSuccess()
    {
        return !m_Error;
    }

    public boolean isFailure()
    {
        return m_Error;
    }

    public Collection getWarnings()
    {
        return m_Warnings;
    }

    public void addWarning( String warning )
    {
        m_Warnings.add( warning );
    }

    public Collection getErrors()
    {
        return m_ErrorMessages;
    }

    public void addError( String error )
    {
        m_ErrorMessages.add( error );
    }

    public Collection getMessages()
    {
        return m_Messages;
    }

    public void addMessage( String message )
    {
        m_Messages.add( message );
    }
}