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
 * are Copyright (C) 2009-2010. All Rights Reserved.
 *
 * Contributor(s): Steve Yegge, Nathan L. Fiedler.
 *
 * $Id: ConsoleBreakpointFactory.java 137 2009-04-29 00:05:39Z nathanfiedler $
 */
package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.breakpoint.DefaultLocationBreakpoint;
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.Location;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import org.openide.util.NbBundle;

/**
 * Console-mode implementation of
 * {@link com.bluemarsh.jswat.core.breakpoint.LocationBreakpoint}.
 * Emits JDB-compatible output if requested.
 *
 * @author  Steve Yegge
 */
public class ConsoleLocationBreakpoint extends DefaultLocationBreakpoint {

    @Override
    public String describe(Event e) {
        Location location = getLocation();
        String cname = location.declaringType().name();
        String mname = location.method().name();
        String msig = location.method().signature();
        String index = String.valueOf(location.codeIndex());
        String line = String.valueOf(location.lineNumber());
        String thread = Threads.getIdentifier(((LocatableEvent) e).thread());

        if (Main.emulateJDB()) {
            return NbBundle.getMessage(ConsoleLocationBreakpoint.class,
                    "Location.description.stop.jdb",
                    new String[]{thread, cname, mname, line});
        }
        return NbBundle.getMessage(ConsoleLocationBreakpoint.class,
                "Location.description.stop",
                new String[]{cname, mname, msig, index, thread});
    }

    @Override
    public String getDescription() {
        Location location = getLocation();
        String[] params = new String[]{
            location.declaringType().name(),
            location.method().name(),
            location.method().signature(),
            String.valueOf(Main.emulateJDB() ? location.lineNumber()
            : location.codeIndex())
        };
        return NbBundle.getMessage(ConsoleLocationBreakpoint.class,
                "Location.description", params);
    }
}
