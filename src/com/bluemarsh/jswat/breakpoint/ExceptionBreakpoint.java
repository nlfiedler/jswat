/*********************************************************************
 *
 *      Copyright (C) 2001-2004 Nathan Fiedler
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
 * $Id: ExceptionBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.ExceptionBreakpointUI;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Names;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.util.Threads;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;

/**
 * Class ExceptionBreakpoint implements the Breakpoint interface. Its
 * only property is the name of an exception class. It halts execution
 * of the debuggee VM whenever an exception of the given type (or subtype)
 * has been thrown. This includes caught and uncaught exceptions.
 *
 * @author Nathan Fiedler
 */
public class ExceptionBreakpoint extends ResolvableBreakpoint
    implements LocatableBreakpoint {
    /** True to stop when the exception is caught. */
    private boolean onCaught;
    /** True to stop when the exception is not caught. */
    private boolean onUncaught;
    /** The exception class to be caught. */
    private ReferenceType exceptionClass;

    /**
     * Default constructor for deserialization.
     */
    ExceptionBreakpoint() {
    } // ExceptionBreakpoint

    /**
     * Constructs a ExceptionBreakpoint for the given exception class.
     *
     * @param  classPattern  name of class in which to set breakpoint,
     *                       possibly using wildcards.
     * @param  onCaught      true to stop on caught exceptions.
     * @param  onUncaught    true to stop on uncaught exceptions.
     * @throws  ClassNotFoundException
     *          if classPattern is not a valid identifier.
     */
    public ExceptionBreakpoint(String classPattern, boolean onCaught,
                               boolean onUncaught)
        throws ClassNotFoundException {

        super(classPattern);
        this.onCaught = onCaught;
        this.onUncaught = onUncaught;
    } // ExceptionBreakpoint

    /**
     * Create the exception event request for the given class.
     *
     * @param  refType  exception class to be caught.
     * @return  newly created event request.
     */
    protected EventRequest createEventRequest(ReferenceType refType) {
        VirtualMachine vm = refType.virtualMachine();
        EventRequestManager erm = vm.eventRequestManager();
        ExceptionRequest er = erm.createExceptionRequest(
            refType, onCaught, onUncaught);
        prepareRequest(vm, this, er);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("created event request for " + this);
        }
        return er;
    } // createEventRequest

    /**
     * Return a brief description of the given exception event.
     *
     * @param  ee  exception event to describe.
     * @return  exception description.
     */
    protected static String describeException(ExceptionEvent ee) {
        // Show the type of exception that was thrown.
        String tname = ee.exception().type().name();
        tname = Names.justTheName(tname);
        StringBuffer buf = new StringBuffer(tname);

        // Get the message of the exception so the user has more
        // information about what went wrong.
        buf.append(": ");
        try {
            ObjectReference obj = ee.exception();
            ThreadReference thrd = ee.thread();
            ReferenceType type = obj.referenceType();
            List methods = type.methodsByName("getMessage",
                                              "()Ljava/lang/String;");
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
            buf = new StringBuffer("Error: " + itse);
        } catch (InvalidTypeException ite) {
            buf = new StringBuffer("Error: " + ite);
        } catch (ClassNotLoadedException cnle) {
            buf = new StringBuffer("Error: " + cnle);
        } catch (InvocationException ie) {
            buf = new StringBuffer("Error: " + ie);
        }
        return buf.toString();
    } // describeException

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        super.destroy();
        Session session = getBreakpointGroup().getSession();
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.removeListener(ExceptionEvent.class, this);
    } // destroy

    /**
     * Return the name of the class that this breakpoint is located in.
     * This could be a fully-qualified class name or a wild-carded name
     * pattern containing a single asterisk (e.g. "*.cname").
     *
     * @return  Class name if known, null if not.
     */
    public String getClassName() {
        return referenceSpec != null ? referenceSpec.getIdentifier() : null;
    } // getClassName

    /**
     * Retrieve the line number associated with this breakpoint. Not all
     * breakpoints will have a particular line associated with them (such
     * as method breakpoints). In such cases, this method will return -1.
     *
     * @return  line number of breakpoint, if applicable; -1 if not.
     */
    public int getLineNumber() {
        return -1;
    } // getLineNumber

    /**
     * Returns the name of the package for the class this breakpoint is
     * set within. May not be known, in which case null is returned.
     *
     * @return  package name, or null if unknown.
     */
    public String getPackageName() {
        return null;
    } // getPackageName

    /**
     * Locatable breakpoints are expected to be set in classes, hence they
     * should have a <code>ReferenceType</code>. However, the caller should
     * check for a return value of null.
     *
     * @return  reference type, or null if not set.
     */
    public ReferenceType getReferenceType() {
        if (eventRequest != null) {
            ExceptionRequest er = (ExceptionRequest) eventRequest;
            return er.exception();
        } else {
            return null;
        }
    } // getReferenceType

    /**
     * Returns the name of the source file for the class this breakpoint
     * is set within. May not be known, in which case null is returned.
     *
     * @return  source name, or null if unknown.
     */
    public String getSourceName() {
        return null;
    } // getSourceName

    /**
     * Returns the stop-on-caught status.
     *
     * @return  true if stopping when caught exceptions are thrown.
     */
    public boolean getStopOnCaught() {
        return onCaught;
    } // getStopOnCaught

    /**
     * Returns the stop-on-uncaught status.
     *
     * @return  true if stopping when uncaught exceptions are thrown.
     */
    public boolean getStopOnUncaught() {
        return onUncaught;
    } // getStopOnUncaught

    /**
     * Returns the user interface widget for customizing this breakpoint.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return new ExceptionBreakpointUI(this);
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
        if (logger.isLoggable(Level.INFO)) {
            logger.info("initialized " + this);
        }
    } // init

    /**
     * Compares the fully-qualified class name with the specification of
     * this breakpoint. If the two names match, taking wildcards into
     * consideration, then this method returns true.
     *
     * @param  name  fully-qualified class name to compare to.
     * @return  true if names match, false otherwise.
     */
    public boolean matchesClassName(String name) {
        return referenceSpec != null ? referenceSpec.matches(name) : false;
    } // matchesClassName

    /**
     * Compares the name of the package and the name of the source file
     * with those given as arguments. Nulls do not constitute a match.
     *
     * @param  pkg  name of package (with dot separators).
     * @param  src  name of source file.
     * @return  true if matches this breakpoint's information.
     */
    public boolean matchesSource(String pkg, String src) {
        return false;
    } // matchesSource

    /**
     * This breakpoint has caused the debuggee VM to stop. Increment any
     * breakpoint counters and execute all monitors associated with
     * this breakpoint.
     *
     * @param  e  Event for which we are stopping.
     * @return  true if VM should resume, false otherwise.
     */
    protected boolean performStop(Event e) {
        String desc = describeException((ExceptionEvent) e);
        desc = Bundle.getString("exceptionThrown") + ' ' + desc;
        return super.performStop(e, desc);
    } // performStop

    /**
     * Prepares the given exception request for general use. That is,
     * it sets the "breakpoint" property to point to the given breakpoint,
     * applies the thread and class filters of that breakpoint, and
     * sets it enabled if the breakpoint is enabled.
     *
     * @param  vm  virtual machine for applying thread filters.
     * @param  bp  breakpoint for which to create requests.
     * @param  er  exception request to apply filters to.
     */
    protected static void prepareRequest(VirtualMachine vm, Breakpoint bp,
                                         ExceptionRequest er) {
        // Save a reference to ourselves in case we need it.
        er.putProperty("breakpoint", bp);
        er.setSuspendPolicy(bp.getSuspendPolicy());

        // Apply thread filters.
        Session session = bp.getBreakpointGroup().getSession();
        String filtersStr = bp.getThreadFilters();
        if (filtersStr != null && filtersStr.length() > 0) {
            List filters = Strings.stringToList(filtersStr);
            for (int ii = 0; ii < filters.size(); ii++) {
                String tid = (String) filters.get(ii);
                ThreadReference thread = Threads.getThreadByID(vm, tid);
                if (thread != null) {
                    er.addThreadFilter(thread);
                } else {
                    session.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING,
                        Bundle.getString("noSuchThreadFilter") + ' ' + tid);
                }
            }
        }

        // Apply class filters.
        filtersStr = bp.getClassFilters();
        if (filtersStr != null && filtersStr.length() > 0) {
            List filters = Strings.stringToList(filtersStr);
            for (int ii = 0; ii < filters.size(); ii++) {
                String filter = (String) filters.get(ii);
                er.addClassFilter(filter);
            }
        }

        // Have to enable the request to work.
        er.setEnabled(bp.isEnabled());
    } // prepareRequest

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        onCaught = prefs.getBoolean("onCaught", true);
        onUncaught = prefs.getBoolean("onUncaught", true);
        return super.readObject(prefs);
    } // readObject

    /**
     * Resolve against the given ReferenceType. If successful, return
     * the new event request.
     *
     * @param  refType  ReferenceType against which to resolve.
     * @return  event request, or null if not resolved.
     * @throws  ResolveException
     *          if breakpoint resolve fails.
     */
    protected EventRequest resolveReference(ReferenceType refType)
        throws ResolveException {

        EventRequest er = createEventRequest(refType);
        if (er != null) {
            exceptionClass = refType;
        }
        return er;
    } // resolveReference

    /**
     * Enables or disables this breakpoint, according to the parameter.
     * This only affects the breakpoint itself. If the breakpoint group
     * containing this breakpoint is disabled, this breakpoint will
     * remain effectively disabled.
     *
     * @param  enabled  true if breakpoint should be enabled, false
     *                  if breakpoint should be disabled.
     * @see #isEnabled
     */
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            // Delete it now because we would do so anyway when we are
            // enabled later (as part of recreating the request).
            deleteEventRequest();
        }
        super.setEnabled(enabled);
        if (enabled && exceptionClass != null) {
            // Create the event request using the current settings.
            eventRequest = createEventRequest(exceptionClass);
        }
    } // setEnabled

    /**
     * Sets the stop-on-caught status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when caught exceptions are thrown.
     */
    public void setStopOnCaught(boolean stop) {
        onCaught = stop;
    } // setStopOnCaught

    /**
     * Sets the stop-on-uncaught status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when uncaught exceptions are thrown.
     */
    public void setStopOnUncaught(boolean stop) {
        onUncaught = stop;
    } // setStopOnUncaught

    /**
     * Returns a String representation of this.
     *
     * @return  string of this.
     */
    public String toString() {
        return toString(false);
    } // toString

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     * @return  string of this.
     */
    public String toString(boolean terse) {
        if (referenceSpec == null) {
            return "<not initialized>";
        }
        StringBuffer buf = new StringBuffer(80);
        buf.append("catch ");
        String cname = referenceSpec.toString();
        if (terse) {
            cname = Names.justTheName(cname);
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

    /**
     * Writes the breakpoint properties to the given preferences node.
     * It is assumed that the preferences node is completely empty.
     *
     * @param  prefs  Preferences node to which to serialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean writeObject(Preferences prefs) {
        if (!super.writeObject(prefs)) {
            return false;
        }
        prefs.putBoolean("onCaught", onCaught);
        prefs.putBoolean("onUncaught", onUncaught);
        return true;
    } // writeObject
} // ExceptionBreakpoint
