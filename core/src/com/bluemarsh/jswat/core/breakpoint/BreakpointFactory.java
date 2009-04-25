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
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import java.net.MalformedURLException;
import java.util.List;

/**
 * A BreakpointFactory creates Breakpoint instances. A concrete implementation
 * can be acquired from the <code>BreakpointProvider</code> class.
 *
 * @author Nathan Fiedler
 */
public interface BreakpointFactory {

    /**
     * Creates either a line breakpoint or a method breakpoint, based on
     * the specification given. The recognized specification formats include:
     *
     * <ul>
     *   <li><code>[[&lt;package&gt;.]&lt;class&gt;:]&lt;line&gt;</code></li>
     *   <li><code>[[&lt;package&gt;.]&lt;class&gt;:]&lt;method&gt;[(&lt;arg-list&gt;)]</code></li>
     *   <li><code>[&lt;path&gt;/&lt;file&gt;:]&lt;line&gt;</code></li>
     * </ul>
     *
     * <p>Both a path and file name, as well as a fully-qualified class name,
     * are supported means for specifying a location. That is, if the user
     * wants to set a breakpoint in a source file, they can specify the
     * path and name of that source file. The path does not have to be
     * absolute, nor does the file have to exist on the local system. The
     * only requirement is that the path and file name match the source path
     * and source name found in the class file. Note that the path separator
     * must be the platform-specific value (e.g. \ on Windows), but it will
     * always be converted to forward slash (/) internally.</p>
     *
     * <p>If the user wants to use a class name, specifying the class
     * by its fully-qualified binary name (e.g. java.lang.String) will
     * probably be sufficient. If the class is inside a source file whose
     * extension differs from the default, then it may not resolve.</p>
     *
     * <p>The <code>arg-list</code> is a comma-separated list of types,
     * such as <code>int</code> and <code>java.lang.String</code>. The
     * types must match one of the methods in the given class for the
     * breakpoint to be resolved successfully.</p>
     *
     * <p>If the class name is not given, then the current location must
     * be set in the <code>DebuggingContext</code> or the breakpoint
     * creation will fail.</p>
     *
     * @param  spec     specification for the breakpoint.
     * @param  context  provides current location; may be null if the
     *                  breakpoint specification includes a class name.
     * @return  the new breakpoint.
     * @throws  AbsentInformationException
     *          if extracting source information from the current location
     *          is unsuccessful (in which case the caller may want to give
     *          a class name next time).
     * @throws  AmbiguousClassSpecException
     *          thrown if class name was missing and context was null.
     * @throws  AmbiguousMethodException
     *          if the method specified is not specific enough to match.
     * @throws  MalformedClassNameException
     *          if the class name is not a valid identifier.
     * @throws  MalformedMemberNameException
     *          if the method name is not a valid identifier.
     * @throws  NumberFormatException
     *          if a line number was not actually a number.
     */
    Breakpoint createBreakpoint(String spec, DebuggingContext context)
            throws AbsentInformationException, AmbiguousClassSpecException,
            AmbiguousMethodException, MalformedClassNameException,
            MalformedMemberNameException, NumberFormatException;

    /**
     * Create a breakpoint group and add it to the given breakpoint group.
     *
     * @param  name  name for breakpoint group.
     * @return  new breakpoint group.
     */
    BreakpointGroup createBreakpointGroup(String name);

    /**
     * Create a breakpoint that stops when matching classes load or unload,
     * as described by the given parameters.
     *
     * @param  filter   class filter, or null to match all classses.
     * @param  prepare  true to stop on class prepare, false to ignore.
     * @param  unload   true to stop on class unload, false to ignore.
     * @return  new class breakpoint.
     */
    ClassBreakpoint createClassBreakpoint(String filter, boolean prepare,
            boolean unload);

    /**
     * Create a condition that is satisfied when the given expression
     * evaluates to true.
     *
     * @param  expr  boolean expression to evaluate.
     * @return  new condition.
     */
    Condition createCondition(String expr);

    /**
     * Create a breakpoint for the named exception class, stopping if it was
     * caught or not, depending on the given parameters.
     *
     * @param  cname     name of class in which to set breakpoint, with wildcards.
     * @param  caught    true to stop on caught exceptions, false to ignore.
     * @param  uncaught  true to stop on uncaught exceptions, false to ignore.
     * @return  new exception breakpoint.
     * @throws  MalformedClassNameException
     *          if class pattern is not a valid identifier.
     */
    ExceptionBreakpoint createExceptionBreakpoint(String cname, boolean caught,
            boolean uncaught) throws MalformedClassNameException;

    /**
     * Create a breakpoint at a line in a file.
     *
     * @param  url   URL for the source file, as from FileObject.
     * @param  pkg   name of package containing class, or null if unknown.
     * @param  line  1-based line number at which to stop.
     * @return  new line breakpoint.
     * @throws  MalformedClassNameException
     *          if the class name is not a valid identifier.
     * @throws  MalformedURLException
     *          if the URL is invalid.
     */
    LineBreakpoint createLineBreakpoint(String url, String pkg, int line)
        throws MalformedClassNameException, MalformedURLException;

    /**
     * Create a breakpoint at a specific location.
     *
     * @param  location  JDI Location at which to set breakpoint.
     * @return  new location breakpoint.
     */
    LocationBreakpoint createLocationBreakpoint(Location location);

    /**
     * Create a breakpoint for the given class at the specified method
     * within that class.
     *
     * @param  cname   name of class in which to set breakpoint, with wildcards.
     * @param  method  name of method to stop at.
     * @param  args    list of method parameter types as Strings.
     * @return  new method breakpoint.
     * @throws  MalformedClassNameException
     *          if class name is not a valid identifier.
     * @throws  MalformedMemberNameException
     *          if the method name is invalid.
     */
    MethodBreakpoint createMethodBreakpoint(String cname, String method,
            List<String> args)
        throws MalformedClassNameException, MalformedMemberNameException;

    /**
     * Create a breakpoint for the named thread, stopping if the thread starts
     * or dies, depending on the given parameters.
     *
     * @param  thread  thread name, or null to match all threads.
     * @param  start   true to stop on thread start, or false to ignore.
     * @param  death   true to stop on thread death, or false to ignore.
     * @return  new thread breakpoint.
     */
    ThreadBreakpoint createThreadBreakpoint(String thread, boolean start,
            boolean death);

    /**
     * Create a breakpoint that simply displays a message as methods are
     * entered and exited in the debuggee. Class and thread filters control
     * the amount of information sent to the debugger.
     *
     * @param  cfilter  class filter, or null to match all.
     * @param  tfilter  thread filter, or null to match all.
     * @param  enter    stop when the method is entered.
     * @param  exit     stop when the method is exited.
     * @return  new trace breakpoint.
     */
    TraceBreakpoint createTraceBreakpoint(String cfilter, String tfilter,
            boolean enter, boolean exit);

    /**
     * Create a breakpoint that stops when any type of exception is not caught.
     *
     * @return  new uncaught exception breakpoint.
     */
    UncaughtExceptionBreakpoint createUncaughtExceptionBreakpoint();

    /**
     * Create a breakpoint for the named field, stopping if the field is
     * accessed or modified, depending on the given parameters.
     *
     * @param  cname   name of class containing the field.
     * @param  field   name of field to be watched.
     * @param  access  stop when the field is accessed.
     * @param  modify  stop when the field is modified.
     * @return  new variable breakpoint.
     * @throws  MalformedClassNameException
     *          if class name is not a valid identifier.
     * @throws  MalformedMemberNameException
     *          if methodname is not a valid identifier.
     */
    WatchBreakpoint createWatchBreakpoint(String cname, String field,
            boolean access, boolean modify)
            throws MalformedClassNameException, MalformedMemberNameException;

    /**
     * Create a breakpoint for a specific field, stopping if the field is
     * accessed or modified, depending on the given parameters. The field
     * name property of this breakpoint cannot be changed.
     *
     * @param  field   field to be watched.
     * @param  obj     object instance containing field.
     * @param  access  stop when the field is accessed.
     * @param  modify  stop when the field is modified.
     * @return  new variable breakpoint.
     */
    WatchBreakpoint createWatchBreakpoint(Field field, ObjectReference obj,
            boolean access, boolean modify);
}
