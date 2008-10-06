/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 1999-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultDebuggingContext.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.context;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.LocatableEvent;

/**
 * Class DefaultDebuggingContext provides a simple implementation of a
 * DebuggingContext.
 *
 * @author  Nathan Fiedler
 */
public class DefaultDebuggingContext extends AbstractDebuggingContext {
    /** Reference to the current thread. */
    private volatile ThreadReference currentThread;
    /** Zero-based index of the current stack frame. */
    private volatile int currentFrame;

    /**
     * Constructs a new DebuggingContext object.
     */
    public DefaultDebuggingContext() {
    }

    public synchronized int getFrame() {
        return currentFrame;
    }

    public synchronized Location getLocation() {
        Location loc = null;
        try {
            if (currentThread != null) {
                // Always derive the location because Location and StackFrame
                // objects can become stale after hotswap operations.
                loc = currentThread.frame(currentFrame).location();
            }
            //
            // Catch the specific exceptions that we know can occur, and
            // leave the unexpected exceptions for someone else to catch.
            //
        } catch (IncompatibleThreadStateException itse) {
            return null;
        } catch (IndexOutOfBoundsException ioobe) {
            return null;
        } catch (InvalidStackFrameException isfe) {
            return null;
        }
        return loc;
    }

    public synchronized StackFrame getStackFrame() throws
            IncompatibleThreadStateException, IndexOutOfBoundsException,
            ObjectCollectedException {
        if (currentThread != null) {
            return currentThread.frame(currentFrame);
        } else {
            return null;
        }
    }

    public synchronized ThreadReference getThread() {
        return currentThread;
    }

    public synchronized void reset() {
        currentThread = null;
        currentFrame = 0;
    }

    public synchronized void setFrame(int frame) throws
            IncompatibleThreadStateException, IndexOutOfBoundsException {
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
            currentFrame = frame;
            fireChange(ContextEvent.Type.FRAME, false);
        }
    }

    public synchronized void setLocation(LocatableEvent le, boolean suspending) {
        currentFrame = 0;
        if (le != null) {
            currentThread = le.thread();
        } else {
            currentThread = null;
        }
        fireChange(ContextEvent.Type.LOCATION, suspending);
    }

    public synchronized void setThread(ThreadReference thread, boolean suspending) {
        currentThread = thread;
        // If thread changes, stack frame is reset to zero.
        currentFrame = 0;
        fireChange(ContextEvent.Type.THREAD, suspending);
    }
}
