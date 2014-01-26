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
 * FILE:        ExceptionBreakpoint.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/06/01        Initial version
 *      nf      08/21/01        Removed errorMessageFor()
 *      as      12/11/01        Handle null from getMessage()
 *
 * DESCRIPTION:
 *      Defines the exception breakpoint class.
 *
 * $Id: ExceptionBreakpoint.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BasicBreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.util.ClassUtils;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.jswat.util.ThreadUtils;
import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Class ExceptionBreakpoint implements the Breakpoint interface. Its
 * only property is the name of an exception class. It halts execution
 * of the debuggee VM whenever an exception of the given type (or subtype)
 * has been thrown. This includes caught and uncaught exceptions.
 *
 * @author Nathan Fiedler
 */
public class ExceptionBreakpoint extends ResolvableBreakpoint {
    /** serial version */
    static final long serialVersionUID = -3215278515799927187L;

    /**
     * Constructs a ExceptionBreakpoint for the given exception class.
     *
     * @param  classPattern  name of class in which to set breakpoint,
     *                       possibly using wildcards.
     * @exception  ClassNotFoundException
     *             Thrown if classPattern is not a valid identifier.
     */
    ExceptionBreakpoint(String classPattern)
        throws ClassNotFoundException {

        super(classPattern);
    } // ExceptionBreakpoint

    /**
     * Create the method entry and exit event requests. If a request
     * has already been created, it will be deleted and a new one
     * will be made.
     *
     * @param  refType  exception to catch.
     * @return  newly created event request.
     */
    protected EventRequest createEventRequest(ReferenceType refType) {
        Session session = getBreakpointGroup().getSession();
        EventRequestManager erm =
            refType.virtualMachine().eventRequestManager();

        // Create the new request.
        EventRequest er = erm.createExceptionRequest(refType, true, true);
        // Save a reference to ourselves in case we need it.
        er.putProperty("breakpoint", this);
        er.setSuspendPolicy(suspendPolicy);

        // Apply thread filters.
        String filtersStr = getThreadFilters();
        if (filtersStr != null && filtersStr.length() > 0) {
            List filters = StringUtils.stringToList(filtersStr);
            try {
                for (int ii = 0; ii < filters.size(); ii++) {
                    String thdtoken = (String) filters.get(ii);
                    ThreadReference thread = ThreadUtils.getThreadByID(
                        session, thdtoken);
                    if (thread != null) {
                        ((ExceptionRequest) er).addThreadFilter(thread);
                    } else {
                        session.getStatusLog().writeln(
                            Bundle.getString("noSuchThreadFilter") + " " +
                            thdtoken);
                    }
                }
            } catch (NotActiveException nae) { }
        }

        // Apply class filters.
        filtersStr = getClassFilters();
        if (filtersStr != null && filtersStr.length() > 0) {
            List filters = StringUtils.stringToList(filtersStr);
            for (int ii = 0; ii < filters.size(); ii++) {
                String filter = (String) filters.get(ii);
                ((ExceptionRequest) er).addClassFilter(filter);
            }
        }

        // Have to enable the request to work.
        er.setEnabled(isEnabled());
        return er;
    } // createEventRequest

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        Session session = getBreakpointGroup().getSession();
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.removeListener(ExceptionEvent.class, this);
    } // destroy

    /**
     * Returns the user interface widget for customizing this breakpoint.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        BasicBreakpointUI bbui = new BasicBreakpointUI(this);
        bbui.addClassFilter();
        bbui.addThreadFilter();
        return bbui;
    } // getUIAdapter

    /**
     * Initialize the breakpoint so it may operate normally.
     */
    public void init() {
        // We need to listen for exception events.
        Session session = getBreakpointGroup().getSession();
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.addListener(ExceptionEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
    } // init

    /**
     * This breakpoint has caused the debuggee VM to stop. Increment any
     * breakpoint counters and execute all monitors associated with
     * this breakpoint.
     *
     * @param  e  Event for which we are stopping.
     * @return  true if VM should resume, false otherwise.
     */
    protected boolean performStop(Event e) {
        super.performStop(e);
        Session session = getBreakpointGroup().getSession();
        showException((ExceptionEvent) e, session.getStatusLog());
        return false;
    } // performStop

    /**
     * Resolve against the given ReferenceType. If successful, return
     * the new event request.
     *
     * @param  refType  ReferenceType against which to resolve.
     * @return  event request, or null if not resolved.
     * @exception  ResolveException
     *             Thrown if breakpoint resolve fails.
     */
    protected EventRequest resolveReference(ReferenceType refType)
        throws ResolveException {

        return createEventRequest(refType);
    } // resolveReference

    /**
     * Enables or disables this breakpoint, according to the parameter.
     * This only affects the breakpint itself. If the breakpoint group
     * containing this breakpoint is disabled, this breakpoint will
     * remain effectively disabled.
     *
     * @param  enabled  true if breakpoint should be enabled, false
     *                  if breakpoint should be disabled.
     * @see #isEnabled
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled && eventRequest != null) {
            // Delete and recreate the event request using the
            // current breakpoint settings.
            ExceptionRequest er = (ExceptionRequest) eventRequest;
            ReferenceType refType = er.exception();
            deleteEventRequest();
            eventRequest = createEventRequest(refType);
        }
    } // setEnabled

    /**
     * Show the given exception event information to the given Log.
     *
     * @param  ee   exception event to show.
     * @param  out  place to show exception details to.
     */
    static void showException(ExceptionEvent ee, Log out) {
        // Show the type of exception that was thrown.
        String tname = ee.exception().type().name();
        tname = ClassUtils.justTheName(tname);
        StringBuffer buf = new StringBuffer(tname);
        buf.append(": ");

        // Get the message of the exception so the user has more
        // information about what went wrong.
        try {
            ObjectReference obj = ee.exception();
            ThreadReference thrd = ee.thread();
            ReferenceType type = obj.referenceType();
            List methods = type.methodsByName("getMessage");
            Method method = (Method) methods.get(0);
            // Must invoke single-threaded or other threads will run
            // briefly and strange things will seem to happen from
            // the debugger user's point of view.
            Value retval = obj.invokeMethod(
                thrd, method, new LinkedList(),
                ObjectReference.INVOKE_SINGLE_THREADED);
            if (retval == null) {
                buf.append("null");
            } else {
                buf.append(retval.toString());
            }

        } catch (IncompatibleThreadStateException itse) {
            // This can't happen.
            itse.printStackTrace();
        } catch (InvalidTypeException ite) {
            // This won't happen because there are no arguments.
            ite.printStackTrace();
        } catch (ClassNotLoadedException cnle) {
            // This can't happen.
            cnle.printStackTrace();
        } catch (InvocationException ie) {
            // This is just very unlikely.
            ie.printStackTrace();
        } finally {
            out.writeln(buf.toString());
        }
    } // showException

    /**
     * Returns a String representation of this.
     */
    public String toString() {
        return toString(false);
    } // toString

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     */
    public String toString(boolean terse) {
        StringBuffer buf = new StringBuffer(80);
        buf.append("catch ");
        String cname = referenceSpec.toString();
        if (terse) {
            cname = ClassUtils.justTheName(cname);
        }
        buf.append(cname);
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
} // ExceptionBreakpoint
