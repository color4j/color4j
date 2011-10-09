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

package org.color4j.spectro.spi.helpers;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * General purpose First-In First-Out buffer.
 * <p>This class is a standard thread-safe FIFO,
 * that blocks on getObject() if nothing in the
 * FIFO, and has a maximum size, set at construction.
 */
public class Fifo
{
    private static Logger m_Logger = Logger.getLogger( Fifo.class.getName() );

    private ArrayList m_Fifo;
    private int m_MaxSize;

    /**
     * Constructs the FIFO
     *
     * @param maxsize is the maximum number of objects that the
     *                FIFO can hold. A negative number indicates unlimited
     *                size. 0 (zero) means no objects, and always throws
     *                an exception, 1 means one object at a time and so on.
     */
    public Fifo( int maxsize )
    {
        m_MaxSize = maxsize;
        m_Fifo = new ArrayList();
    }

    /**
     * Places an object at the end of the FIFO buffer.
     * wakes current thread
     * <p>If the object can not be inserted due to maximum
     * size of the FIFO, a FifoFullException is thrown</p>.
     *
     * @param o the object to put in the Fifo.
     *
     * @throws FifoFullException if the Fifo is full and can't accept any more objects.
     */
    public void putObject( Object o )
        throws FifoFullException
    {
        if( o == null )
        {
            throw new IllegalArgumentException( "Null is not allowed." );
        }
        synchronized( this )
        {
            if( m_Fifo.size() >= m_MaxSize )
            {
                throw new FifoFullException();
            }
            m_Logger.finer( "object added to FIFO: " + o );//, new Exception() );
            m_Fifo.add( o );
            notifyAll();
        }
    }

    /**
     * removes object from FIFO buffer
     * pauses current thread
     * <p>If the thread is interrupted it will return null.</p>
     *
     * @return oldest object in queue
     */
    public Object removeNextObject()
    {
        synchronized( this )
        {
            try
            {
                while( true )
                {
                    if( m_Fifo.size() == 0 )
                    {
                        wait();
                    }

                    if( m_Fifo.size() > 0 )
                    {
                        return m_Fifo.remove( 0 );
                    }
                }
            }
            catch( InterruptedException e )
            {
                return null;
            }
        }
    }

    /**
     * does not remove object from FIFO buffer
     * pauses current thread
     * <p>If the thread is interrupted it will return null.</p>
     *
     * @return oldest object in queue
     */
    public Object getNextObject()
    {
        synchronized( this )
        {
            try
            {
                if( m_Fifo.size() == 0 )
                {
                    wait();
                }

                if( m_Fifo.size() > 0 )
                {
                    return m_Fifo.get( 0 );
                }
                else
                {
                    //Should not happen
                    return null;
                }
            }
            catch( InterruptedException e )
            {
                return null;
            }
        }
    }

    public int getMaximumSize()
    {
        return m_MaxSize;
    }

    /**
     * Sets the size of the FIFO.
     * <p>If the size is smaller than the currently number
     * of objects held, these objects are kept, and no new
     * objects can be added until the number of objects in the FIFO
     * drops below the max size.</p>
     *
     * @param size the maximum number of objects that the Fifo can hold.
     */
    public void setMaximumSize( int size )
    {
        m_MaxSize = size;
    }

    public boolean isFull()
    {
        return m_Fifo.size() >= m_MaxSize;
    }

    public boolean isEmpty()
    {
        return m_Fifo.isEmpty();
    }
}