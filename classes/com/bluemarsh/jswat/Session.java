/*********************************************************************
 *
 *      Copyright (C) 2003-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: Session.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;

/**
 * A Session is responsible for managing the debugging session, as well
 * as the debuggee connection. It provides methods for activating,
 * deactivating, and closing the session. Some of the important objects
 * in the program are accessed via a concrete implementation of this
 * interface.
 *
 * @author  Nathan Fiedler
 */
public interface Session {

    /**
     * Activates the Session now that a connection has been made. This
     * method should not be called directly. Instead, the
     * <code>attachDebuggee()</code> or <code>launchDebuggee()</code>
     * methods of <code>VMConnection</code> should be called.
     *
     * @param  connection  VMConnection used to represent the debuggee
     *                     VM connection.
     * @param  showSource  true to show the source for the main class.
     * @param  source      the object invoking this method (will be the
     *                     source of the new session event).
     */
    void activate(VMConnection connection, boolean showSource, Object source);

    /**
     * Adds a SessionListener to this session. This method calls the
     * <code>init()</code> method on the listener. It may also call the
     * <code>activate()</code> method if the Session is already active.
     *
     * @param  listener  SessionListener to add to this session.
     */
    void addListener(SessionListener listener);

    /**
     * Shutdown the session permanently. This will close all the
     * listeners, clear all lists and set all references to null.
     *
     * @param  source  the object invoking this method (will be the
     *                 source of the new session event).
     */
    void close(Object source);

    /**
     * Deactivates this session. If the debuggee VM was lanched, it is
     * terminated. If the debuggee was remote, the session disconnects
     * but leaves the remote debuggee running, unless the 'forceExit'
     * parameter is set to true.
     *
     * @param  forceExit  true to close debuggee VM forcibly in all
     *                    cases; false to leave remote debuggee running.
     * @param  source     the object invoking this method (will be the
     *                    source of the new session event).
     * @see #activate
     */
    void deactivate(boolean forceExit, Object source);

    /**
     * The debuggee has unexpectedly disconnected without sending a
     * proper VMDisconnectEvent. This method is to be invoked by the
     * event dispatcher when a VMDisconnectedException occurs.
     *
     * @param  source  the object invoking this method (will be the
     *                 source of the session event).
     */
    void disconnected(Object source);

    /**
     * Returns the VMConnection object for this Session, if any.
     *
     * @return  VMConnection, or null if none has been made yet.
     */
    VMConnection getConnection();

    /**
     * Retrieve an instance of a manager of the given class. If one does
     * not exist, an instance will be created.
     *
     * @param  managerClass  Class of Manager to retrieve.
     * @return  Manager object, or null if unlikely things happened.
     */
    Manager getManager(Class managerClass);

    /**
     * Searches for the property with the specified key in this property
     * list. The method returns null if the property is not found.
     *
     * @param  key  the property key.
     * @return  the value in this property list with the specified key value.
     */
    String getProperty(String key);

    /**
     * Returns the set of session property names.
     *
     * @return  set of session property names, or null if error.
     */
    String[] getPropertyKeys();

    /**
     * Returns a reference to the Log object that receives messages.
     *
     * @return  Log for writing messages to.
     */
    Log getStatusLog();

    /**
     * Returns a reference to the virtual machine associated with this
     * session. If the Session is not active, this will return null.
     *
     * @return  Java virtual machine for this session, or null if
     *          not currently connected.
     */
    VirtualMachine getVM();

    /**
     * Returns a reference to the interface adapter associated with this
     * session.
     *
     * @return  interface adapter for this session.
     */
    UIAdapter getUIAdapter();

    /**
     * Do the usual thing when a debug event has occurred. This involves
     * displaying an indication of the type of event that occurred, such
     * as setting the status indicator. If the event is locatable, this
     * method tries to show the location of the event that occurred, set
     * the current context of the event in the ContextManager, and show
     * the source code for the event location.
     *
     * @param  event  debug event.
     */
    void handleDebugEvent(Event event);

    /**
     * Initialize the Session.
     *
     * @param  uiadapter  interface adapter.
     */
    void init(UIAdapter uiadapter);

    /**
     * Any further operations to be performed after the UI adapter and
     * session have both been initialized is done here.
     */
    void initComplete();

    /**
     * Returns true if this session is active.
     *
     * @return  true if active, false otherwise.
     * @see #activate
     * @see #deactivate
     */
    boolean isActive();

    /**
     * Removes a Session listener from this session. This calls the
     * <code>close()</code> method of the listener.
     *
     * @param  listener  SessionListener to remove from this session.
     */
    void removeListener(SessionListener listener);

    /**
     * Resume execution of the debuggee VM, from a suspended state.
     *
     * @param  source  object making this request.
     * @param  brief   true if caller expects this new state to last
     *                 only a brief amount of time.
     * @param  quiet   true to be silent (do not print anything).
     */
    void resumeVM(Object source, boolean brief, boolean quiet);

    /**
     * Stores the given value in the properties list with the given key
     * as a reference. If the value is null, then the key and value will
     * be removed from the properties.
     *
     * @param  key    the key to be placed into this property list.
     * @param  value  the value corresponding to key, or null to remove
     *                the key and value from the properties.
     * @return  previously stored value, if any.
     */
    String setProperty(String key, String value);

    /**
     * Suspend execution of the debuggee VM.
     *
     * @param  source  object making this request.
     */
    void suspendVM(Object source);
} // Session
