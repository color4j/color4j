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
 * Created on Nov 4, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.color4j.spectro.drivers.xr;

import java.util.ArrayList;
import java.util.Collection;
import org.color4j.spectro.spi.SpectroStatus;

/**
 */
public class XR8000Status implements SpectroStatus
{
    ArrayList m_Messages;
    ArrayList m_Warnings;
    ArrayList m_Errors;

    public XR8000Status()
    {
        m_Messages = new ArrayList();
        m_Warnings = new ArrayList();
        m_Errors = new ArrayList();
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroStatus#isSuccess()
      */
    public boolean isSuccess()
    {
        if( m_Errors.size() == 0 )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroStatus#isFailure()
      */
    public boolean isFailure()
    {
        if( m_Errors.size() > 0 )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroStatus#getWarnings()
      */
    public Collection getWarnings()
    {
        return (Collection) m_Warnings.clone();
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroStatus#addWarning(java.lang.String)
      */
    public void addWarning( String warning )
    {
        m_Warnings.add( warning );
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroStatus#getErrors()
      */
    public Collection getErrors()
    {
        return (Collection) m_Errors.clone();
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroStatus#addError(java.lang.String)
      */
    public void addError( String error )
    {
        m_Errors.add( error );
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroStatus#getMessages()
      */
    public Collection getMessages()
    {
        return (Collection) m_Messages.clone();
    }

    /* (non-Javadoc)
      * @see org.color4j.spectro.spi.SpectroStatus#addMessage(java.lang.String)
      */
    public void addMessage( String message )
    {
        m_Messages.add( message );
    }
}
