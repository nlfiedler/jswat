/*********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
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
 * PROJECT:     JSwat
 * MODULE:      JSwat
 * FILE:        ContextManager.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/25/99        Initial version
 *      nf      03/18/01        Combine change events into one when reasonable.
 *                              Set the location when thread or frame changes.
 *      nf      08/22/01        Fixed bug 173
 *      nf      12/24/02        Fixed bug 583
 *
 * $Id: ContextManager.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.event.ContextChangeEvent;
import com.bluemarsh.jswat.event.ContextListener;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.util.EventListenerList;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.LocatableEvent;

/**
 * Class ContextManager is responsible for holding the current context of
 * the debugger. It holds a reference to the current thread, the current
 * source line (if stepping), the current frame in the thread stack, etc.
 *
 * @author  Nathan Fiedler
 */
public class ContextManager implements Manager {
    /** Reference to the current thread. */
    private ThreadReference currentThread;
    /** Zero-based index of the current stack frame. */
    private int currentFrame;
    /** Location from the last time setLocation() was called. This is
     * used only for firing the appropriate type of change events. It
     * can become stale from hotswaps; getCurrentLocation() should be
     * used instead. */
    private Location currentLocation;
    /** List of context listeners. */
    private EventListenerList listeners;
    /** Count of the stack frames from the current thread. */
    private int latestFrameCount;

    /**
     * Constructs a new ContextManager object.
     */
    public ContextManager() {
        listeners = new EventListenerList();
    } // ContextManager

    /**
     * Called when the Session has activated. This occurs when the debuggee
     * has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
    } // activated

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
    } // closing

    /**
     * Add a context listener to this manager object.
     *
     * @param  listener  new listener to add notification list
     */
    public void addContextListener(ContextListener listener) {
        listeners.add(ContextListener.class, listener);
    } // addContextListener

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
        // Clear the context information.
        setCurrentLocation(null, false);
    } // deactivated

    /**
     * Let all the change listeners know of a recent change in the context.
     * This creates a ContextChangeEvent object and sends it out to the
     * listeners.
     *
     * @param  oldThrd   previous value for current thread
     * @param  oldLoc    previous value for current location
     * @param  oldFrame  previous value for current frame
     * @param  brief     true if change is brief in duration.
     */
    protected void fireChange(ThreadReference oldThrd,
                              Location oldLoc, int oldFrame,
                              boolean brief) {
        // Nothing to do if no listeners registered.
        if (listeners == null) {
            return;
        }

        // Figure out what changed from before.
        int types = 0;
        try {
            if ((currentThread != null)
                && (currentThread.frameCount() != latestFrameCount)) {
                // Thread's stack frame count differs from the previously
                // stored value. Sure, any time you switch threads this
                // will be true, so we fire off a couple of extra events,
                // big deal. At least now a frame change event occurs any
                // time the stack frame actually changes. Fixes bug #91.
                latestFrameCount = currentThread.frameCount();
                // Count this as a frame change.
                types |= ContextChangeEvent.TYPE_FRAME;
            }
        } catch (IncompatibleThreadStateException itse) {
            // so what
        }
        if (oldFrame != currentFrame) {
            types |= ContextChangeEvent.TYPE_FRAME;
        }
        if (oldLoc != currentLocation) {
            if ((oldLoc != null) && (currentLocation != null)) {
                if (!oldLoc.equals(currentLocation)) {
                    types |= ContextChangeEvent.TYPE_LOCATION;
                }
            } else {
                types |= ContextChangeEvent.TYPE_LOCATION;
            }
        }
        if (oldThrd != currentThread) {
            if ((oldThrd != null) && (currentThread != null)) {
                if (!oldThrd.equals(currentThread)) {
                    types |= ContextChangeEvent.TYPE_THREAD;
                }
            } else {
                types |= ContextChangeEvent.TYPE_THREAD;
            }
        }
        if (types == 0) {
            // nothing changed
            return;
        }

        // Create the change event.
        ContextChangeEvent cce = new ContextChangeEvent(this, types, brief);
        // Get the listener list as class/instance pairs.
        Object[] list = listeners.getListenerList();
        // Process the listeners last to first.
        // List is in pairs: class, instance
        for (int i = list.length - 2; i >= 0; i -= 2) {
            if (list[i] == ContextListener.class) {
                ContextListener cl = (ContextListener) list[i + 1];
                try {
                    cl.contextChanged(cce);
                } catch (VMDisconnectedException vmde) {
                    // yeah, this happens a lot
                    break;
                } catch (Exception e) {
                    // ignore it and carry on
                }
            }
        }
    } // fireChange

    /**
     * Returns the current thread's frame index. The returned value is
     * guaranteed to be a valid stack frame index.
     *
     * @return  Current thread's frame index.
     */
    public int getCurrentFrame() {
        return currentFrame;
    } // getCurrentFrame

    /**
     * Get the location that the debugger is presently stepping through.
     *
     * @return  Current location of debugger.
     */
    public Location getCurrentLocation() {
        try {
            // Always derive the location because Location and StackFrame
            // objects can become stale after hotswap operations.
            int frame = getCurrentFrame();
            StackFrame stack = currentThread.frame(frame);
            return stack.location();
        } catch (Exception e) {
            return null;
        }
    } // getCurrentLocation

    /**
     * Returns the current thread's stack frame.
     *
     * @return  Current thread's stack frame, or null if no current thread.
     * @throws  IncompatibleThreadStateException
     *          if current thread is not suspended.
     * @throws  IndexOutOfBoundsException
     *          if current frame is < 0 or >= frame count.
     * @throws  ObjectCollectedException
     *          if this object has been collected.
     */
    public StackFrame getCurrentStackFrame()
        throws IncompatibleThreadStateException,
               IndexOutOfBoundsException,
               ObjectCollectedException {
        if (currentThread != null) {
            return currentThread.frame(currentFrame);
        } else {
            return null;
        }
    } // getCurrentStackFrame

    /**
     * Returns the ThreadReference to the current thread object.
     *
     * @return  Current thread reference.
     */
    public ThreadReference getCurrentThread() {
        return currentThread;
    } // getCurrentThread

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
    } // opened

    /**
     * Remove a context listener from the listener list.
     *
     * @param  listener  listener to remove from notification list
     */
    public void removeContextListener(ContextListener listener) {
        listeners.remove(ContextListener.class, listener);
    } // removeContextListener

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Sets the current thread's stack frame. The thread must be suspended
     * before calling this method.
     *
     * @param  frame  New current stack frame. Must be between zero
     *                and the number of stack frames minus one, inclusive.
     * @throws  IncompatibleThreadStateException
     *          if current thread is not suspended.
     * @throws  IndexOutOfBoundsException
     *          if frame is < 0 or >= frame count.
     */
    public void setCurrentFrame(int frame)
        throws IncompatibleThreadStateException,
               IndexOutOfBoundsException {
        if (frame < 0) {
            throw new IndexOutOfBoundsException("frame < 0");
        }
        if (currentThread == null) {
            throw new IllegalStateException("current thread not set");
        }
        if (currentThread.isSuspended()) {
            if (frame >= currentThread.frameCount()) {
                throw new IndexOutOfBoundsException("frame > frame count");
            }
        } else {
            throw new IncompatibleThreadStateException
                ("thread must be suspended");
        }

        // Set the field variables only if the frame changed.
        if (currentFrame != frame) {
            Location oldLoc = currentLocation;
            int oldFrame = currentFrame;
            currentFrame = frame;
            currentLocation = getCurrentLocation();
            fireChange(currentThread, oldLoc, oldFrame, false);
        }
    } // setCurrentFrame

    /**
     * Set the location and thread that the debugger is presently
     * examining. If null is passed, the location and thread are reset to
     * null. The current frame is always reset to zero.
     *
     * @param  le     current location of debugger, or null to reset.
     * @param  brief  true if change is brief in duration.
     */
    public void setCurrentLocation(LocatableEvent le, boolean brief) {
        ThreadReference oldThrd = currentThread;
        Location oldLoc = currentLocation;
        int oldFrame = currentFrame;
        currentFrame = 0;
        if (le != null) {
            currentThread = le.thread();
            currentLocation = le.location();
        } else {
            currentThread = null;
            currentLocation = null;
        }
        fireChange(oldThrd, oldLoc, oldFrame, brief);
    } // setCurrentLocation

    /**
     * Sets the current thread to the one given. Resets the current frame
     * to zero (top stack frame).
     *
     * @param  thread  New current thread.
     */
    public void setCurrentThread(ThreadReference thread) {
        ThreadReference oldThrd = currentThread;
        Location oldLoc = currentLocation;
        int oldFrame = currentFrame;
        currentThread = thread;
        // If thread changes, stack frame is reset to zero.
        currentFrame = 0;
        // Location also changes with the frame.
        currentLocation = getCurrentLocation();
        fireChange(oldThrd, oldLoc, oldFrame, false);
    } // setCurrentThread

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended
} // ContextManager
