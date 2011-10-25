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

package org.color4j.imports.mdb;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import org.color4j.imports.ImportException;

/**
 */
public interface ColorDBExtractor
{
    void refresh();

    String[] getStandardIDs()
        throws ImportException;

    String getStandardName( String id )
        throws ImportException;

    Date getStandardCreationDate( String id )
        throws ImportException;

    String getStandardDescription( String id )
        throws ImportException;

    Map getStandardAttributes( String id )
        throws ImportException;

    TreeMap getStandardSpectrum( String id )
        throws ImportException;

    String getStandardAperture( String id )
        throws ImportException;

    String getStandardLightFilter( String id )
        throws ImportException;

    String getStandardSpecular( String id )
        throws ImportException;

    String[] getBatchIDs()
        throws ImportException;

    String getBatchName( String id )
        throws ImportException;

    Date getBatchCreationDate( String id )
        throws ImportException;

    String getBatchDescription( String id )
        throws ImportException;

    Map getBatchAttributes( String id )
        throws ImportException;

    TreeMap getBatchSpectrum( String id )
        throws ImportException;

    String getBatchAperture( String id )
        throws ImportException;

    String getBatchLightFilter( String id )
        throws ImportException;

    String getBatchSpecular( String id )
        throws ImportException;
}
