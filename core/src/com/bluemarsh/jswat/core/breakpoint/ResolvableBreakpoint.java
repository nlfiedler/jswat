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
 * $Id: ResolvableBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * A ResolvableBreakpoint represents a breakpoint that requires
 * resolution against a specific class in the debuggee VM.
 *
 * @author  Nathan Fiedler
 */
public interface ResolvableBreakpoint extends Breakpoint {
    /** Name of the 'className' property. */
    public static final String PROP_CLASSNAME = "className";

    /**
     * Returns the class name this breakpoint is to resolve against.
     *
     * @return  class name, possibly with a leading or trailing wildcard (*).
     */
    public String getClassName();

    /**
     * Sets the class name this breakpoint is to resolve against. The
     * name may have a wildcard prefix or suffix of an asterisk (*).
     * This allows matching any number of characters at the beginning
     * or end of the class name. For example, "*.String" would match
     * java.lang.String and mycom.pkg.String; likewise "java.lang.*"
     * would match all classes in the java.lang package.
     *
     * @param  name  class name, possibly with a wildcard (*).
     * @throws  MalformedClassNameException
     *          if class name is not a valid identifier.
     */
    public void setClassName(String name) throws MalformedClassNameException;
}
