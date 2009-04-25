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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.util.AmbiguousMethodException;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * DefaultBreakpointFactory creates Breakpoint instances using the default
 * breakpoint implementations in the
 * <code>com.bluemarsh.jswat.core.breakpoint</code> package.
 *
 * @author Nathan Fiedler
 */
public class DefaultBreakpointFactory implements BreakpointFactory {

    @Override
    public Breakpoint createBreakpoint(String spec, DebuggingContext context)
            throws AbsentInformationException, AmbiguousClassSpecException,
            AmbiguousMethodException, MalformedClassNameException,
            MalformedMemberNameException, NumberFormatException {

        // Trim useless whitespace.
        spec = spec.trim();

        // Determine what type of breakpoint specification was provided.
        if (spec.matches("^\\d+$") || spec.matches(".+:\\d+$")) {
            // It must be a line breakpoint.
            int ci = spec.indexOf(':');
            String suffix = spec;
            // Remove the line number from the spec.
            if (ci > 0) {
                suffix = spec.substring(ci + 1);
                spec = spec.substring(0, ci);
            } else {
                spec = null;
            }
            // Let this throw the appropriate number format exception.
            int line = Integer.parseInt(suffix);
            // Looks like we have a line breakpoint after all.
            if (spec == null) {
                // With no class name, current location must be set.
                Location loc = context.getLocation();
                if (loc == null) {
                    throw new AmbiguousClassSpecException();
                }
                // Get the source path and name.
                spec = loc.sourcePath() + File.separator + loc.sourceName();
            } else {
                // Check if the user gave a file name or a class name.
                if (spec.indexOf(File.separatorChar) < 0) {
                    // Do our best to convert the class name to a file name.
                    spec = Names.classnameToFilename(spec);
                }
            }
            // Even for Windows, need to use slash for the URL.
            spec.replace(File.separatorChar, '/');
            // This is kind of a hack, but the url is not really used
            // as a URL in most places, just the last part has to match
            // the resolved class information. In any case, there is no
            // good solution since the user may be setting a breakpoint
            // in code for which they have no source available.
            String url = "file://foo/" + spec;
            try {
                return createLineBreakpoint(url, null, line);
            } catch (MalformedURLException mue) {
                // This probably means the class name is mangled.
                throw new MalformedClassNameException(mue);
            }
        } else {
            // It must be a method breakpoint.
            int ci = spec.indexOf(':');
            String suffix = spec;
            // Remove the method portion from the spec.
            if (ci > 0) {
                suffix = spec.substring(ci + 1);
                spec = spec.substring(0, ci);
            } else {
                // With no class name, current location must be set.
                Location loc = context.getLocation();
                if (loc == null) {
                    throw new AmbiguousClassSpecException();
                }
                spec = loc.declaringType().name();
            }
            int p1 = suffix.indexOf('(');
            int p2 = suffix.lastIndexOf(')');
            String method;
            List<String> args = null;
            if (p1 < 0 && p2 < 0) {
                method = suffix;
                args = Collections.emptyList();
            } else if (p1 < 0 || p2 < 0) {
                throw new IllegalArgumentException(suffix);
            } else {
                method = suffix.substring(0, p1);
                args = Strings.stringToList(suffix.substring(p1 + 1, p2));
            }
            if (method.length() == 0) {
                throw new AmbiguousMethodException();
            }
            return createMethodBreakpoint(spec, method, args);
        }
    }

    @Override
    public BreakpointGroup createBreakpointGroup(String name) {
        BreakpointGroup group = new DefaultBreakpointGroup();
        group.setName(name);
        return group;
    }

    @Override
    public ClassBreakpoint createClassBreakpoint(String filter,
            boolean prepare, boolean unload) {
        ClassBreakpoint cb = new DefaultClassBreakpoint();
        cb.setClassFilter(filter);
        cb.setStopOnPrepare(prepare);
        cb.setStopOnUnload(unload);
        return cb;
    }

    @Override
    public Condition createCondition(String expr) {
        ExpressionCondition ec = new ExpressionCondition();
        ec.setExpression(expr);
        return ec;
    }

    @Override
    public ExceptionBreakpoint createExceptionBreakpoint(String cname,
            boolean caught, boolean uncaught)
            throws MalformedClassNameException {
        ExceptionBreakpoint eb = new DefaultExceptionBreakpoint();
        eb.setClassName(cname);
        eb.setStopOnCaught(caught);
        eb.setStopOnUncaught(uncaught);
        return eb;
    }

    @Override
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

    @Override
    public LocationBreakpoint createLocationBreakpoint(Location location) {
        LocationBreakpoint lb = new DefaultLocationBreakpoint();
        lb.setLocation(location);
        return lb;
    }

    @Override
    public MethodBreakpoint createMethodBreakpoint(String cname, String method,
            List<String> args)
            throws MalformedClassNameException, MalformedMemberNameException {
        MethodBreakpoint mb = new DefaultMethodBreakpoint();
        mb.setClassName(cname);
        mb.setMethodName(method);
        mb.setMethodParameters(args);
        return mb;
    }

    @Override
    public ThreadBreakpoint createThreadBreakpoint(String thread, boolean start,
            boolean death) {
        ThreadBreakpoint tb = new DefaultThreadBreakpoint();
        tb.setThreadFilter(thread);
        tb.setStopOnDeath(death);
        tb.setStopOnStart(start);
        return tb;
    }

    @Override
    public TraceBreakpoint createTraceBreakpoint(String cfilter, String tfilter,
            boolean enter, boolean exit) {
        TraceBreakpoint tb = new DefaultTraceBreakpoint();
        tb.setClassFilter(cfilter);
        tb.setThreadFilter(tfilter);
        tb.setStopOnEnter(enter);
        tb.setStopOnExit(exit);
        return tb;
    }

    @Override
    public UncaughtExceptionBreakpoint createUncaughtExceptionBreakpoint() {
        return new DefaultUncaughtExceptionBreakpoint();
    }

    @Override
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

    @Override
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
