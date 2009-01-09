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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
