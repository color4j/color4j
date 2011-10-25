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

package org.color4j.imports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import org.color4j.colorimetry.entities.Reflectance;

/**
 */
public abstract class AbstractTextFileReflectanceImporter
    implements TextFileReflectanceImporter
{
    private String m_Name;
    private String m_Extension;

    public AbstractTextFileReflectanceImporter( String name, String extension )
    {
        m_Name = name;
        m_Extension = extension;
    }

    public String getName()
    {
        return m_Name;
    }

    public String getDefaultExtension()
    {
        return m_Extension;
    }

    public Reflectance[] doImport( File fileName, Map<String, String> attributes )
        throws IOException, ImportException
    {
        FileInputStream fis = new FileInputStream( fileName );
        return doImport( fis, attributes );
    }
}
