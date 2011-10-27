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

package org.color4j.exports.icc;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;

public class IccSupport
{
    private static ICC_ColorSpace m_ICCPrinterColorSpace;
    private static ICC_ColorSpace m_ICCDisplayColorSpace;

    public static void setICCMonitorProfile( String fileName )
        throws IOException
    {
        if( fileName != null && fileName.length() > 0 )
        {
            ICC_Profile profile = ICC_Profile.getInstance( fileName );
            m_ICCDisplayColorSpace = new ICC_ColorSpace( profile );
        }
    }

    public static void setICCPrintingProfile( String fileName )
        throws IOException
    {
        if( fileName != null && fileName.length() > 0 )
        {
            ICC_Profile profile = ICC_Profile.getInstance( fileName );
            m_ICCPrinterColorSpace = new ICC_ColorSpace( profile );
        }
    }

    public static ICC_ColorSpace getICCPrintingProfile()
    {
        return m_ICCPrinterColorSpace;
    }

    public static ICC_ColorSpace getICCMonitorProfile()
    {
        return m_ICCDisplayColorSpace;
    }
}
