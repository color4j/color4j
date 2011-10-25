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

package org.color4j.exports;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import org.color4j.colorimetry.entities.Reflectance;

public interface ReflectanceExporter
{
    String PROP_ILLUMINANT = "illuminant";
    String PROP_OBSERVER = "observer";
    String PROP_ICCPRINTER = "printerprofile";
    String PROP_ICCMONITOR = "monitorprofile";

    String getName();

    String getDefaultExtension();

    void doExport( Reflectance[] colors, OutputStream out )
        throws IOException, ExportException;

    void doExport( Collection<Reflectance> reflectances, OutputStream out )
        throws IOException, ExportException;

    /**
     * returns true the export format supports storing entity attributes
     */
    boolean supportsAttributes();

    /**
     * Returns true if the exporter requires a primary
     * colour to be chosen.
     */
    boolean requiresPrimary();

    /**
     * Returns true if multiple colors can be stored in the
     * same file.
     */
    boolean supportsMultipleColors();

    // KH - Dec 14, 2004 : newly added to support exporting of conditions and other data
    void setProperties( Map<String,String> properties );

    Map<String,String> getProperties();
}
