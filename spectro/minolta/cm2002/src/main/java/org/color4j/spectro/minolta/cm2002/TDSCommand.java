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
 * TDSCommand.java
 *
 * Created on October 14, 2002, 3:23 PM
 */

package org.color4j.spectro.minolta.cm2002;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 * @author chc
 */
public class TDSCommand implements SpectroCommand
{
    private String m_Spectral;
    private String m_DataID;
    private String m_Comment;

    /**
     * Creates a new instance of TDSCommand
     */
    public TDSCommand( String Spectral, String DataID, String Comment )
    {
        m_Spectral = Spectral;
        m_DataID = DataID;
        m_Comment = Comment;
    }

    public String construct()
    {
        return "TDS\r\n" + m_Spectral + m_DataID + m_Comment;
    }

    public String getName()
    {
        return "Target Data Set Command";
    }

    public org.color4j.spectro.spi.SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM2002Status.create( new String( values ) ) );
    }
}
