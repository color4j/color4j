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
 * SNSCommand.java
 *
 * Created on October 14, 2002, 3:30 PM
 */

package org.color4j.spectro.minolta.cm2002;

import org.color4j.spectro.spi.SpectroCommand;
import org.color4j.spectro.spi.SpectroEvent;

/**
 */
public class SNSCommand implements SpectroCommand
{
    private String m_Specimen;

    /**
     * Creates a new instance of SNSCommand
     */
    public SNSCommand( String Specimen )
    {
        m_Specimen = Specimen;
    }

    public String construct()
    {
        return "SNS\r\n" + m_Specimen;
    }

    public String getName()
    {
        return "Speciment Number Set Command";
    }

    public org.color4j.spectro.spi.SpectroEvent interpret( byte[] values )
    {
        return new SpectroEvent( this, CM2002Status.create( new String( values ) ) );
    }
}
