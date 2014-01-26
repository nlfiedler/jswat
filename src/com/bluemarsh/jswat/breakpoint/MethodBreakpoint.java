/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      Breakpoints
 * FILE:        MethodBreakpoint.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/05/01        Initial version
 *      nf      08/21/01        Removed errorMessageFor()
 *      nf      03/08/02        Moved some methods to ClassUtils
 *      nf      03/20/02        Added get/set methods
 *
 * DESCRIPTION:
 *      Defines the method breakpoint class.
 *
 * $Id: MethodBreakpoint.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.MalformedMemberNameException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.MethodBreakpointUI;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.report.Category;
import com.bluemarsh.jswat.util.ClassUtils;
import com.sun.jdi.ClassType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.util.Collections;
import java.util.List;

/**
 * Class MethodBreakpoint extends the LocationBreakpoint class. Its
 * properties include a class and a method name, and an argument list
 * specifying where the breakpoint should stop.
 *
 * @author Nathan Fiedler
 */
public class MethodBreakpoint extends LocationBreakpoint {
    /** serial version */
    static final long serialVersionUID = -2335031278296223805L;
    /** Name of the method this breakpoint is set at. */
    protected String methodId;
    /** List of method arguments (if any are given), where each element
     * is a String object reprepsenting the argument type in the JNI-style
     * signature. */
    protected List methodArgs;

    /**
     * Constructs a MethodBreakpoint for the given class at the specified
     * line within that class.
     *
     * @param  classPattern  name of class in which to set breakpoint,
     *                       possibly using wildcards.
     * @param  methodId      name of method to stop at.
     * @param  methodArgs    list of method arguments as Strings in the
     *                       JNI-style signature format.
     * @exception  ClassNotFoundException
     *             Thrown if classPattern is not a valid identifier.
     * @exception  MalformedMemberNameException
     *             Thrown if the method name is invalid.
     */
    MethodBreakpoint(String classPattern, String methodId,
                     List methodArgs)
        throws ClassNotFoundException,
               MalformedMemberNameException {

        super(classPattern);
        this.methodId = methodId;
        this.methodArgs = Collections.unmodifiableList(methodArgs);
        if (!ClassUtils.isJavaIdentifier(methodId) &&
            !methodId.equals("<init>") &&
            !methodId.equals("<clinit>")) {
            throw new MalformedMemberNameException(methodId);
        }
        // By default we do not have a line number.
        lineNumber = -1;
    } // MethodBreakpoint

    /**
     * Retrieve the arguments to the method at which this breakpoint
     * is set. The returned list is unmodifiable.
     *
     * @return  list of method arguments.
     */
    public List getMethodArgs() {
        return methodArgs;
    } // getMethodArgs

    /**
     * Retrieve the method name associated with this breakpoint.
     *
     * @return  name of method this breakpoint is set to.
     */
    public String getMethodName() {
        return methodId;
    } // getMethodName

    /**
     * Returns the user interface widget for customizing this breakpoint.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return new MethodBreakpointUI(this);
    } // getUIAdapter

    /**
     * Determine the location at which to set the breakpoint using
     * the given class type. 
     *
     * @param  clazz  ClassType against which to resolve.
     * @return  Location at which to create breakpoint.
     */
    protected Location resolveLocation(ClassType clazz)
        throws ResolveException {

        try {
            Method method = ClassUtils.findMethod(
                clazz, methodId, methodArgs);
            Location loc = method.location();
            // Now is our chance to get the line number.
            lineNumber = loc.lineNumber();
            return loc;
        } catch (AmbiguousClassSpecException acse) {
            throw new ResolveException(acse);
        } catch (AmbiguousMethodException ame) {
            throw new ResolveException(ame);
        } catch (NoSuchMethodException nsme) {
            throw new ResolveException(nsme);
        }
    } // resolveLocation

    /**
     * Set the list of arguments to the method at which this breakpoint
     * is set. The list is made unmodifiable via the Collections class.
     *
     * @param  args  method argument list.
     */
    public void setMethodArgs(List args) {
        methodArgs = Collections.unmodifiableList(args);
        // Reset ourselves so we get resolved all over again.
        deleteEventRequest();
        fireChange();
    } // setMethodArgs

    /**
     * Set the method name associated with this breakpoint.
     *
     * @param  name  name of method this breakpoint is set to.
     */
    public void setMethodName(String name) {
        methodId = name;
        // Reset ourselves so we get resolved all over again.
        deleteEventRequest();
        fireChange();
    } // setMethodName

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     */
    public String toString(boolean terse) {
        StringBuffer buf = new StringBuffer(80);
        String cname = referenceSpec.toString();
        if (terse) {
            cname = ClassUtils.justTheName(cname);
        }
        buf.append(cname);

        // Append the method name, plus any method args.
        buf.append('.');
        buf.append(methodId);
        if (methodArgs != null) {
            int size = methodArgs.size();
            buf.append('(');
            if (size > 0) {
                buf.append((String) methodArgs.get(0));
                for (int i = 1; i < size; i++) {
                    buf.append(',');
                    buf.append((String) methodArgs.get(i));
                }
            }
            buf.append(')');
        }

        if (!terse) {
            buf.append(' ');
            if (suspendPolicy == EventRequest.SUSPEND_ALL) {
                buf.append(Bundle.getString("suspendAll"));
            } else if (suspendPolicy == EventRequest.SUSPEND_EVENT_THREAD) {
                buf.append(Bundle.getString("suspendThread"));
            } else if (suspendPolicy == EventRequest.SUSPEND_NONE) {
                buf.append(Bundle.getString("suspendNone"));
            }
        }
        return buf.toString();
    } // toString
} // MethodBreakpoint
