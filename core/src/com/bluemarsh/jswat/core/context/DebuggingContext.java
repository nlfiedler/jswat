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

import com.bluemarsh.jswat.core.context.ContextListener;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.LocatableEvent;

/**
 * A DebuggingContext is responsible for holding the current context of
 * the debugger. It holds a reference to the current thread, the current
 * location, and the current frame in the thread's call stack. Concrete
 * implementations of this interface are acquired from the
 * <code>ContextProvider</code> class.
 *
 * @author  Nathan Fiedler
 */
public interface DebuggingContext {

    /**
     * Add a context listener to this manager object.
     *
     * @param  listener  new listener to add notification list
     */
    void addContextListener(ContextListener listener);

    /**
     * Returns the current thread's frame index. The returned value is
     * guaranteed to be a valid stack frame index.
     *
     * @return  Current thread's frame index.
     */
    int getFrame();

    /**
     * Get the current location, derived from the current frame in the
     * current thread.
     *
     * @return  current location, or null if error.
     */
    Location getLocation();

    /**
     * Returns the current stack frame.
     *
     * @return  current thread's stack frame, or null if no current thread.
     * @throws  IncompatibleThreadStateException
     *          if current thread is not suspended.
     * @throws  IndexOutOfBoundsException
     *          if current frame is < 0 or >= frame count.
     * @throws  ObjectCollectedException
     *          if this object has been collected.
     */
    StackFrame getStackFrame()
        throws IncompatibleThreadStateException,
               IndexOutOfBoundsException,
               ObjectCollectedException;

    /**
     * Returns the current thread.
     *
     * @return  current thread reference.
     */
    ThreadReference getThread();

    /**
     * Remove a context listener from the listener list.
     *
     * @param  listener  listener to remove from notification list
     */
    void removeContextListener(ContextListener listener);

    /**
     * Clear the context in preparation for connecting or disconnecting
     * from the debuggee. This must not send out any context events.
     */
    void reset();

    /**
     * Sets the current thread's stack frame. The thread must be suspended
     * before calling this method.
     *
     * @param  frame  new current stack frame index. Must be between zero
     *                and the number of stack frames minus one, inclusive.
     * @throws  IncompatibleThreadStateException
     *          if current thread is not suspended.
     * @throws  IndexOutOfBoundsException
     *          if frame is < 0 or >= frame count.
     */
    void setFrame(int frame)
        throws IncompatibleThreadStateException,
               IndexOutOfBoundsException;

    /**
     * Set the location that the debugger is presently examining. If null
     * is passed, the location and thread are reset to null. The current
     * frame is always reset to zero.
     *
     * @param  le          locatable event, or null to reset.
     * @param  suspending  true if Session is suspending as a result of this.
     */
    void setLocation(LocatableEvent le, boolean suspending);

    /**
     * Sets the current thread to the one given. Resets the current frame
     * to zero (top stack frame).
     *
     * @param  thread      new current thread.
     * @param  suspending  true if Session is suspending as a result of this.
     */
    void setThread(ThreadReference thread, boolean suspending);
}
