/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 1999-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
