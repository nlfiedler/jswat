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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultBreakpointFactory.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * DefaultBreakpointFactory creates Breakpoint instances using the default
 * breakpoint implementations in the
 * <code>com.bluemarsh.jswat.core.breakpoint</code> package.
 *
 * @author Nathan Fiedler
 */
public class DefaultBreakpointFactory implements BreakpointFactory {

    public BreakpointGroup createBreakpointGroup(String name) {
        BreakpointGroup group = new DefaultBreakpointGroup();
        group.setName(name);
        return group;
    }

    public ClassBreakpoint createClassBreakpoint(String filter,
            boolean prepare, boolean unload) {
        ClassBreakpoint cb = new DefaultClassBreakpoint();
        cb.setClassFilter(filter);
        cb.setStopOnPrepare(prepare);
        cb.setStopOnUnload(unload);
        return cb;
    }

    public Condition createCondition(String expr) {
        ExpressionCondition ec = new ExpressionCondition();
        ec.setExpression(expr);
        return ec;
    }

    public ExceptionBreakpoint createExceptionBreakpoint(String cname,
            boolean caught, boolean uncaught)
            throws MalformedClassNameException {
        ExceptionBreakpoint eb = new DefaultExceptionBreakpoint();
        eb.setClassName(cname);
        eb.setStopOnCaught(caught);
        eb.setStopOnUncaught(uncaught);
        return eb;
    }

    public LineBreakpoint createLineBreakpoint(String url, String pkg, int line)
            throws MalformedClassNameException, MalformedURLException {
        LineBreakpoint lb = new DefaultLineBreakpoint();
        String cname = pkg == null || pkg.length() == 0 ? "*" : pkg + ".*";
        lb.setClassName(cname);
        lb.setPackageName(pkg);
        lb.setURL(url);
        lb.setLineNumber(line);
        String fname = new File(new URL(url).getFile()).getName();
        lb.setSourceName(fname);
        return lb;
    }

    public LocationBreakpoint createLocationBreakpoint(Location location) {
        LocationBreakpoint lb = new DefaultLocationBreakpoint();
        lb.setLocation(location);
        return lb;
    }

    public MethodBreakpoint createMethodBreakpoint(String cname, String method,
            List<String> args)
            throws MalformedClassNameException, MalformedMemberNameException {
        MethodBreakpoint mb = new DefaultMethodBreakpoint();
        mb.setClassName(cname);
        mb.setMethodName(method);
        mb.setMethodParameters(args);
        return mb;
    }

    public ThreadBreakpoint createThreadBreakpoint(String thread, boolean start,
            boolean death) {
        ThreadBreakpoint tb = new DefaultThreadBreakpoint();
        tb.setThreadFilter(thread);
        tb.setStopOnDeath(death);
        tb.setStopOnStart(start);
        return tb;
    }

    public TraceBreakpoint createTraceBreakpoint(String cfilter, String tfilter,
            boolean enter, boolean exit) {
        TraceBreakpoint tb = new DefaultTraceBreakpoint();
        tb.setClassFilter(cfilter);
        tb.setThreadFilter(tfilter);
        tb.setStopOnEnter(enter);
        tb.setStopOnExit(exit);
        return tb;
    }

    public UncaughtExceptionBreakpoint createUncaughtExceptionBreakpoint() {
        return new DefaultUncaughtExceptionBreakpoint();
    }

    public WatchBreakpoint createWatchBreakpoint(String cname, String field,
            boolean access, boolean modify)
            throws MalformedClassNameException,
                   MalformedMemberNameException {
        ResolvableWatchBreakpoint wb = new DefaultWatchBreakpoint();
        wb.setClassName(cname);
        wb.setFieldName(field);
        wb.setStopOnAccess(access);
        wb.setStopOnModify(modify);
        return wb;
    }

    public WatchBreakpoint createWatchBreakpoint(Field field,
            ObjectReference obj, boolean access, boolean modify) {
        InstanceWatchBreakpoint wb = new DefaultInstanceWatchBreakpoint();
        wb.setField(field);
        wb.setObjectReference(obj);
        wb.setStopOnAccess(access);
        wb.setStopOnModify(modify);
        return wb;
    }
}
