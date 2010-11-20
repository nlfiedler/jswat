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
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.breakpoint.DefaultMethodBreakpoint;
import com.bluemarsh.jswat.core.util.Strings;
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.Location;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Console-mode implementation of
 * {@link com.bluemarsh.jswat.core.breakpoint.MethodBreakpoint}.
 * Emits JDB-compatible output if requested.
 *
 * @author  Steve Yegge
 */
public class ConsoleMethodBreakpoint extends DefaultMethodBreakpoint {

    @Override
    public String describe(Event e) {
        // Unfortunately for JDB compatibility we have to omit the arg types, so
        // that Emacs gud-jdb can find the source location.  But JDB does pass
        // along the line number (the format is identical to Line breakpoints),
        // so if we populate that it'll be able to find the source even for
        // overloaded methods.
        LocatableEvent event = (LocatableEvent) e;
        Location loc = event.location();
        String cname = loc.declaringType().name();
        String mname = loc.method().name();
        String line = String.valueOf(loc.lineNumber());
        String tname = Threads.getIdentifier(event.thread());
        String[] params = new String[]{tname, cname, mname, line};
        return NbBundle.getMessage(ConsoleMethodBreakpoint.class,
                "Method.description.stop", params);
    }

    @Override
    public String getDescription() {
        // This implementation is compatible with JDB's output
        // (except for wildcards, which JDB does not support.)
        String cname = getClassName();
        String mname;
        String methodId = getMethodName();
        if (methodId == null || methodId.length() == 0) {
            mname = "*";
        } else {
            mname = methodId;
        }
        String args;
        List<String> methodParameters = getMethodParameters();
        if (methodParameters == null || methodParameters.isEmpty()) {
            args = "*";
        } else {
            args = Strings.listToString(methodParameters, ",");
        }
        return NbBundle.getMessage(ConsoleMethodBreakpoint.class,
                "Method.description", cname, mname, args);
    }
}
