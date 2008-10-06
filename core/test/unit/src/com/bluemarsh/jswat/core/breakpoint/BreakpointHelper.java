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
 * $Id: BreakpointHelper.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.expr.EvaluatorHelper;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author Nathan Fiedler
 */
public class BreakpointHelper {

    /**
     * Creates a new instance of BreakpointHelper.
     */
    private BreakpointHelper() {
    }

    /**
     * Retrieve a local variable and compare it to the value given.
     *
     * @param  session  session for which to get location.
     * @param  name     name of local variable.
     * @param  value    value to compare with variable.
     * @return  true if equal, false otherwise.
     */
    public static boolean compareVariable(Session session, String name, Object value) {
        boolean equal = false;
        DebuggingContext dc = ContextProvider.getContext(session);
        try {
            StackFrame frame = dc.getStackFrame();
            if (frame != null) {
                LocalVariable var = frame.visibleVariableByName(name);
                if (var != null) {
                    Value val = frame.getValue(var);
                    equal = EvaluatorHelper.areEqual(val, value);
                }
            }
        } catch (Exception e) {
            // ignore and return false
        }
        return equal;
    }

    /**
     * Delete all breakpoints in the given session.
     *
     * @param  session  from which to delete all breakpoints.
     */
    public static void deleteAll(Session session) {
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        BreakpointGroup defgrp = bm.getDefaultGroup();
        // Remove everything below the default group, but not the
        // default group itself.
        List<BreakpointGroup> groups = new ArrayList<BreakpointGroup>();
        Iterator<BreakpointGroup> giter = defgrp.groups(false);
        // Must copy the groups to a list to avoid concurrent modification.
        while (giter.hasNext()) {
            BreakpointGroup bg = giter.next();
            groups.add(bg);
        }
        for (int ii = groups.size() - 1; ii >= 0; ii--) {
            BreakpointGroup group = groups.get(ii);
            bm.removeBreakpointGroup(group);
        }
        // Now remove the breakpoints in the default group, except for the
        // uncaught exception breakpoint, which is tested later.
        Iterator<Breakpoint> biter = defgrp.breakpoints(false);
        List<Breakpoint> brks = new ArrayList<Breakpoint>();
        while (biter.hasNext()) {
            Breakpoint bp = biter.next();
            if (!(bp instanceof UncaughtExceptionBreakpoint)) {
                brks.add(bp);
            }
        }
        for (int ii = brks.size() - 1; ii >= 0; ii--) {
            Breakpoint bp = brks.get(ii);
            bm.removeBreakpoint(bp);
        }
    }

    /**
     * Retrieve the current location for the given session, if any.
     *
     * @param  session  session for which to get location.
     * @return  current program counter, or null if none.
     */
    public static Location getLocation(Session session) {
        DebuggingContext dc = ContextProvider.getContext(session);
        return dc.getLocation();
    }

    /**
     * Retrieve the current thread for the given session, if any.
     *
     * @param  session  session for which to get thread.
     * @return  current thread, or null if none.
     */
    public static ThreadReference getThread(Session session) {
        DebuggingContext dc = ContextProvider.getContext(session);
        return dc.getThread();
    }

    /**
     * Set the breakpoint to expire on one hit and delete itself. Add it to
     * the breakpoint manager for the given session.
     *
     * @param  bp       breakpoint to prepare.
     * @param  session  session for which to retrieve breakpoint manager.
     */
    public static void prepareBreakpoint(Breakpoint bp, Session session) {
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        bp.setDeleteOnExpire(true);
        bp.setExpireCount(1);
        bm.addBreakpoint(bp);
    }
}
