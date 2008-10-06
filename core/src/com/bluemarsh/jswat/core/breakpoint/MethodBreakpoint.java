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
 * $Id: MethodBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
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
    public static final String PROP_METHODNAME = "methodName";
    /** Name of the 'methodParameters' property. */
    public static final String PROP_METHODPARAMETERS = "methodParameters";

    /**
     * Retrieve the parameters to the method at which this breakpoint
     * is set. The returned list is unmodifiable.
     *
     * @return  list of method parameters.
     */
    public List<String> getMethodParameters();

    /**
     * Retrieve the method name associated with this breakpoint.
     *
     * @return  name of method this breakpoint is set to.
     */
    public String getMethodName();

    /**
     * Set the method name associated with this breakpoint. If name is the
     * empty string, the breakpoint will resolve against all methods of the
     * assigned class.
     *
     * @param  name  name of method this breakpoint is set to.
     * @throws  MalformedMemberNameException
     *          if the method name is invalid.
     */
    public void setMethodName(String name) throws MalformedMemberNameException;

    /**
     * Set the list of parameters for the method at which this breakpoint
     * is set. The list may be empty to indicate matching against all methods
     * by the assigned name.
     *
     * @param  args  method parameter list.
     */
    public void setMethodParameters(List<String> args);
}
