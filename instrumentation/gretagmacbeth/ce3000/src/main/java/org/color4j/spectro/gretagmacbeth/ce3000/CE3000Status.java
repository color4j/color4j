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
 * CE3000Status.java
 *
 * Created on September 9, 2003, 5:24 PM
 */

package org.color4j.spectro.gretagmacbeth.ce3000;

import java.util.ArrayList;
import org.color4j.spectro.spi.SpectroStatus;

/**
 * @author devteam
 */
public class CE3000Status implements SpectroStatus
{

    protected ArrayList m_Errors;
    protected ArrayList m_Warnings;
    protected ArrayList m_Messages;

    /**
     * Creates a new instance of CE3000Status
     */
    public CE3000Status()
    {
    }

    public CE3000Status( byte[] status )
    {
        if( status[ 0 ] == 'X' || status[ 0 ] == 'x' )
        {
            addError( "MSG_NOT_CALIBRATED" );
        }
    }

    public void addError( String str )
    {
        if( m_Errors == null )
        {
            m_Errors = new ArrayList();
        }

        m_Errors.add( str );
    }

    public void addMessage( String str )
    {
        if( m_Messages == null )
        {
            m_Messages = new ArrayList();
        }

        m_Messages.add( str );
    }

    public void addWarning( String str )
    {
        if( m_Warnings == null )
        {
            m_Warnings = new ArrayList();
        }

        m_Warnings.add( str );
    }

    public java.util.Collection getErrors()
    {
        return m_Errors;
    }

    public java.util.Collection getMessages()
    {
        return m_Messages;
    }

    public java.util.Collection getWarnings()
    {
        return m_Warnings;
    }

    public boolean isFailure()
    {
        if( m_Errors == null )
        {
            return false;
        }
        else
        {
            return ( m_Errors.size() > 0 );
        }
    }

    public boolean isSuccess()
    {
        if( m_Errors == null )
        {
            return true;
        }
        else
        {
            return ( m_Errors.size() <= 0 );
        }
    }
}