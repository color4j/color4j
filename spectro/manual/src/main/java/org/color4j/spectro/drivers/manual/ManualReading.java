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

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import org.color4j.spectro.spi.SpectroReading;
import org.color4j.spectro.spi.SpectroSettings;
import org.color4j.spectro.spi.SpectroStatus;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public class ManualReading
    implements SpectroReading, ManualReadingListener
{
    //Frame that pops up for user to enter manual readings
    /*protected ManualReadingFrame m_ReadingFrame;*/

    private static transient WeakReference ref;

    private static Logger m_Logger = Logger.getLogger( ManualReading.class.getName() );
    protected ManualStatus m_Status;
    protected SpectroSettings m_Settings;
    protected Map m_Values;
    protected ManualReaderFrame m_Reader;

    protected ManualReader m_temp;

    protected Vector m_Listeners;

    /**
     * The constructor uses a pop-up frame for user to manually enter readings.
     * The ManualReading object constructor instantiates a ManualReadingFrame for
     * users to manually enter reflectance values for the reading
     *
     * @param settings The settings of the manual reading
     */
    public ManualReading( SpectroSettings settings )
    {
        m_Listeners = new Vector();
        m_Settings = settings;
        // JFrame implementation
        //m_Reader = new ManualReaderFrame();
        //m_Reader.addManualReadingListener( this );
        //m_Reader.show();

        // TopComponent implementation
        m_temp = new ManualReader();
        m_temp.addManualReadingListener( this );
        final TopComponent editor = m_temp;

        /*SwingUtilities.invokeLater( new Runnable()
       {
           public void run()
           {
               Workspace ws = WindowManager.getDefault().getCurrentWorkspace();
               Mode mode = ws.findMode( "ManualReader" );
               if( mode != null )
               {
                   mode.dockInto( m_editor );
               }
               else
               {
                   m_Logger.warn( "Unable to find mode ManualReader." );
               }
               m_editor.open();
           }
       } );*/

        if( ref != null )
        {
            TopComponent tc = (TopComponent) ref.get();
            if( tc != null )
            {
                if( !tc.isOpened() )
                {
                    tc.open();
                }
                tc.requestFocusInWindow();

                m_Logger.info( "Using existing TopComponenet" );

                return;
            }
        }

        Mode mode = WindowManager.getDefault().getCurrentWorkspace().findMode( "ManualReader" );

        if( mode != null )
        {
            mode.dockInto( editor );
            // create mode
        }
        editor.open();
        ref = new WeakReference( editor );
        m_Logger.info( "Creating a new TopComponenet" );
    }

    public SpectroStatus getStatus()
    {
        return m_Status;
    }

    public Map getValues()
    {
        return m_Values;
    }

    public SpectroSettings getSettings()
    {
        return m_Settings;
    }

    public void addManualReadingListener( ManualReadingListener l )
    {
        m_Listeners.add( l );
    }

    public void removeManualReadingListener( ManualReadingListener l )
    {
        m_Listeners.remove( l );
    }

    public void manualReadingCreated( ManualReadingEvent evt )
    {
        if( evt == null )
        {
            notifyCreation( null );
            return;
        }

        Map values = evt.getValues();

        if( values == null )
        {
            m_Status = new ManualStatus( true );

            m_Status.addWarning( "No values returned" );
        }

        m_Values = values;
        m_Status = new ManualStatus( false );

        notifyCreation( new ManualReadingEvent( this ) );
    }

    public void notifyCreation( ManualReadingEvent evt )
    {
        Iterator list = m_Listeners.iterator();

        while( list.hasNext() )
        {
            ( (ManualReadingListener) list.next() ).manualReadingCreated( evt );
        }
    }

    /*
   public void readingGenerated( Map values )
   {
       if( values == null )
       {
           m_Status = new ManualStatus( true );

       }

       m_Values = values;
       m_Status = new ManualStatus( false );
       m_Reader.exitForm();

       synchronized( this )
       {
           notifyAll();
       }
   }
    */
}
