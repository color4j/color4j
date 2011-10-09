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

import java.util.EventListener;

/**
 * Listener for CommDriver events.
 * <p>The CommStatusListener will receive events upon changes
 * to the signals (RTS, CTS, DTR, DSR, CD and RI) and on error
 * conditions. The CommDriverListener interface provides
 * events for the send/receive functionality.</p>
 *
 * @see org.color4j.spectro.helpers.CommDriverListener
 * @see org.color4j.spectro.helpers.CommDriverEvent
 */
public interface CommStatusListener extends EventListener
{
    void errorInSend( CommStatusEvent event );

    void errorInReceive( CommStatusEvent event );

    void signalChange( CommStatusEvent event );
}



