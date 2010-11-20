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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
     * @throws  SteppingException  if the step operation failed.
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
