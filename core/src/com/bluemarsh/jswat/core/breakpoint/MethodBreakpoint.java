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
package com.bluemarsh.jswat.core.breakpoint;

import java.util.List;

/**
 * A MethodBreakpoint stops when a particular method in a class is entered.
 *
 * @author Nathan Fiedler
 */
public interface MethodBreakpoint extends ResolvableBreakpoint {

    /** Name of the 'methodName' property. */
    String PROP_METHODNAME = "methodName";
    /** Name of the 'methodParameters' property. */
    String PROP_METHODPARAMETERS = "methodParameters";

    /**
     * Retrieve the parameters to the method at which this breakpoint
     * is set. The returned list is unmodifiable.
     *
     * @return  list of method parameters.
     */
    List<String> getMethodParameters();

    /**
     * Retrieve the method name associated with this breakpoint.
     *
     * @return  name of method this breakpoint is set to.
     */
    String getMethodName();

    /**
     * Set the method name associated with this breakpoint. If name is the
     * empty string, the breakpoint will resolve against all methods of the
     * assigned class.
     *
     * @param  name  name of method this breakpoint is set to.
     * @throws  MalformedMemberNameException
     *          if the method name is invalid.
     */
    void setMethodName(String name) throws MalformedMemberNameException;

    /**
     * Set the list of parameters for the method at which this breakpoint
     * is set. The list may be empty to indicate matching against all methods
     * by the assigned name.
     *
     * @param  args  method parameter list.
     */
    void setMethodParameters(List<String> args);
}
