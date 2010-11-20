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
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.breakpoint.DefaultBreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.LocationBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.MethodBreakpoint;

/**
 * Console-mode implementation of
 * {@link com.bluemarsh.jswat.core.breakpoint.BreakpointFactory}.
 *
 * @author  Steve Yegge
 */
public class ConsoleBreakpointFactory extends DefaultBreakpointFactory {

    // It would be more elegant to create a Provider implementation for
    // instantiating *Breakpoint instances via the NetBeans Lookup mechanism.
    // But it would also be significantly more code for a feature of rather
    // questionable benefit outside the console implementation.

    @Override
    public LineBreakpoint instantiateLineBreakpoint() {
        return new ConsoleLineBreakpoint();
    }

    @Override
    public LocationBreakpoint instantiateLocationBreakpoint() {
        return new ConsoleLocationBreakpoint();
    }

    @Override
    public MethodBreakpoint instantiateMethodBreakpoint() {
        return new ConsoleMethodBreakpoint();
    }
}
