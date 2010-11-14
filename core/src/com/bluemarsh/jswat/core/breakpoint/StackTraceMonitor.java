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
 * are Copyright (C) 2006-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.output.OutputProvider;
import com.bluemarsh.jswat.core.output.OutputWriter;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Produces the stack trace of the current thread to the output window.
 *
 * @author  Nathan Fiedler
 */
public class StackTraceMonitor implements Monitor {

    /** The instance of this class. */
    private static StackTraceMonitor theInstance;

    static {
        theInstance = new StackTraceMonitor();
    }

    /**
     * Default constructor for deserialization.
     */
    private StackTraceMonitor() {
    }

    @Override
    public String describe() {
        return NbBundle.getMessage(BeepMonitor.class, "StackTraceMonitor.desc");
    }

    /**
     * Returns the single instance of this class.
     *
     * @return  the singleton instance.
     */
    public static StackTraceMonitor getInstance() {
        return theInstance;
    }

    @Override
    public void perform(BreakpointEvent event) {
        Event evt = event.getEvent();
        if (!(evt instanceof LocatableEvent)) {
            // Without a location, we can do nothing.
            return;
        }
        LocatableEvent le = (LocatableEvent) evt;
        ThreadReference thread = le.thread();
        if (thread == null) {
            return;
        }
        List<StackFrame> stack = null;
        try {
            stack = thread.frames();
        } catch (IncompatibleThreadStateException itse) {
            return;
        } catch (ObjectCollectedException oce) {
            return;
        }
        if (stack == null) {
            return;
        }
        // This is copied from the WhereCommand in the cmd module.
        StringBuilder sb = new StringBuilder(256);
        sb.append(NbBundle.getMessage(StackTraceMonitor.class,
                "CTL_StackTrace_header", thread.name()));
        sb.append('\n');
        int nFrames = stack.size();
        if (nFrames == 0) {
            sb.append(NbBundle.getMessage(StackTraceMonitor.class,
                    "CTL_StackTrace_emptyStack"));
            sb.append('\n');
        }
        for (int index = 0; index < nFrames; index++) {
            StackFrame frame = stack.get(index);
            Location loc = frame.location();
            Method method = loc.method();
            sb.append("  [");
            sb.append(index + 1);
            sb.append("] ");
            sb.append(method.declaringType().name());
            sb.append('.');
            sb.append(method.name());
            sb.append(" (");
            if (method.isNative()) {
                sb.append(NbBundle.getMessage(StackTraceMonitor.class,
                        "CTL_StackTrace_native"));
            } else if (loc.lineNumber() != -1) {
                try {
                    sb.append(loc.sourceName());
                } catch (AbsentInformationException e) {
                    sb.append(NbBundle.getMessage(StackTraceMonitor.class,
                            "CTL_StackTrace_absentInfo"));
                }
                sb.append(':');
                sb.append(loc.lineNumber());
            }
            sb.append(')');
            long pc = loc.codeIndex();
            if (pc != -1) {
                sb.append(", pc = ");
                sb.append(pc);
            }
            sb.append('\n');
        }
        OutputWriter writer = OutputProvider.getWriter();
        writer.printOutput(sb.toString());
    }

    @Override
    public boolean requiresThread() {
        return true;
    }
}
