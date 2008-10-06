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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Stepper.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.stepping;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.StepRequest;

/**
 * A Stepper is provides the means for performing single-step operations.
 * Concrete implementations of this interface are acquired from the
 * <code>SteppingProvider</code> class.
 *
 * @author  Nathan Fiedler
 */
public interface Stepper {

    /**
     * Creates a single step request in the given thread. The caller must
     * enable the request and resume the Session for the request to take effect.
     *
     * @param  vm      virtual machine in which to create request.
     * @param  thread  thread in which to step.
     * @param  size    how much to step (one of the StepRequest constants).
     * @param  depth   how to step (one of the StepRequest constants).
     * @return  the new step request, initially disabled; null if error.
     */
    StepRequest step(VirtualMachine vm, ThreadReference thread, int size, int depth);

    /**
     * Performs a step operation in the Session associated with this instance.
     * The request will be enabled and the session resumed.
     *
     * @param  size   how much to step (one of the StepRequest constants).
     * @param  depth  how to step (one of the StepRequest constants).
     */
    void step(int size, int depth) throws SteppingException;

    /**
     * Creates a single-step-by-line request, stepping into method calls.
     * The request will be enabled and the session resumed.
     *
     * @throws  SteppingException
     *          if there is no current thread in the DebuggingContext.
     */
    void stepInto() throws SteppingException;

    /**
     * Creates a single-step-by-line request, stepping out of the current
     * method. The request will be enabled and the session resumed.
     *
     * @throws  SteppingException
     *          if there is no current thread in the DebuggingContext.
     */
    void stepOut() throws SteppingException;

    /**
     * Creates a single-step-by-line request, stepping over method calls.
     * The request will be enabled and the session resumed.
     *
     * @throws  SteppingException
     *          if there is no current thread in the DebuggingContext.
     */
    void stepOver() throws SteppingException;
}
