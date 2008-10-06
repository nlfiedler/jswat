/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * $Id: WatchBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.FieldNotObjectException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.WatchBreakpointUI;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.VariableValue;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.util.Threads;
import com.bluemarsh.jswat.util.Variables;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Class WatchBreakpoint implements the Breakpoint interface. It stops
 * each time a particular field is accessed or modified by the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class WatchBreakpoint extends AbstractBreakpoint
    implements SessionListener {
    /** Name of the field we are watching. */
    private String fieldName;
    /** The field that is the variable we are watching, or null
     * if not yet resolved. */
    private Field watchedField;
    /** Instance field object, if provided. */
    private ObjectReference objectRef;
    /** True to stop on field access. */
    private boolean onAccess;
    /** True to stop on field modification. */
    private boolean onModify;
    /** Access event request. */
    private AccessWatchpointRequest accessRequest;
    /** Modification event request. */
    private ModificationWatchpointRequest modifyRequest;

    /**
     * Default constructor for deserialization.
     */
    WatchBreakpoint() {
    } // WatchBreakpoint

    /**
     * Creates a WatchBreakpoint for the named field.
     *
     * @param  varname  name of field to be watched.
     * @param  access   stop when the field is accessed.
     * @param  modify   stop when the field is modified.
     */
    public WatchBreakpoint(String varname, boolean access, boolean modify) {
        this(varname, access, modify, null);
    } // WatchBreakpoint

    /**
     * Creates a WatchBreakpoint for the named field.
     *
     * @param  varname  name of field to be watched.
     * @param  access   stop when the field is accessed.
     * @param  modify   stop when the field is modified.
     * @param  objref  object reference, if any.
     */
    public WatchBreakpoint(String varname, boolean access, boolean modify,
                           ObjectReference objref) {
        fieldName = varname;
        onAccess = access;
        onModify = modify;
        objectRef = objref;
    } // WatchBreakpoint

    /**
     * Creates a WatchBreakpoint for the given field.
     *
     * @param  var     field to be watched.
     * @param  access  true to stop on access.
     * @param  modify  true to stop on modification.
     */
    public WatchBreakpoint(Field var, boolean access, boolean modify) {
        this(var, access, modify, null);
    } // WatchBreakpoint

    /**
     * Creates a WatchBreakpoint for the given field.
     *
     * @param  var     field to be watched.
     * @param  access  true to stop on access.
     * @param  modify  true to stop on modification.
     * @param  objref  object reference, if any.
     */
    public WatchBreakpoint(Field var, boolean access, boolean modify,
                           ObjectReference objref) {
        this(var.name(), access, modify, objref);
        watchedField = var;
    } // WatchBreakpoint

    /**
     * Called when the Session has activated. This occurs when the debuggee
     * has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        if (isEnabled()) {
            createRequests();
        }
    } // activated

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
    } // closing

    /**
     * Create the method entry and exit event requests.
     */
    protected void createRequests() {
        // We are disabled until we are resolved.
        super.setEnabled(false);

        // Delete the old requests, if any.
        deleteRequests();

        Session session = getBreakpointGroup().getSession();
        if (!session.isActive()) {
            return;
        }
        VirtualMachine vm = session.getConnection().getVM();
        EventRequestManager erm = vm.eventRequestManager();

        if (watchedField == null) {
            // Resolve the field name to a Field.
            String errorMsg = null;
            ContextManager ctxtman = (ContextManager)
                session.getManager(ContextManager.class);
            ThreadReference thread = ctxtman.getCurrentThread();
            if (thread == null) {
                errorMsg = Bundle.getString("Watch.noCurrentThread");
            }

            try {
                if (thread != null) {
                    int frame = ctxtman.getCurrentFrame();
                    VariableValue varValue = Variables.getField(
                        fieldName, thread, frame);
                    if (varValue.field() != null) {
                        watchedField = varValue.field();
                    } else {
                        errorMsg = Bundle.getString("Watch.variableNotField");
                    }
                }
            } catch (AbsentInformationException aie) {
                errorMsg = Bundle.getString("Watch.noVariableInfo");
            } catch (ClassNotPreparedException cnpe) {
                errorMsg = Bundle.getString("Watch.classNotLoaded");
            } catch (FieldNotObjectException fnoe) {
                errorMsg = Bundle.getString("Watch.fieldNotObject");
            } catch (IllegalThreadStateException itse) {
                errorMsg = Bundle.getString("Watch.threadNotSuspended");
            } catch (IncompatibleThreadStateException itse) {
                errorMsg = Bundle.getString("Watch.threadNotSuspended");
            } catch (IndexOutOfBoundsException ioobe) {
                // If thread has no frame this exception gets thrown.
                errorMsg = Bundle.getString("Watch.noFrame");
            } catch (InvalidStackFrameException isfe) {
                errorMsg = Bundle.getString("Watch.invalidFrame");
            } catch (NativeMethodException nme) {
                errorMsg = Bundle.getString("Watch.nativeMethod");
            } catch (NoSuchFieldException nsfe) {
                // May also indicate that the local variable is not
                // yet visible at this location.
                errorMsg = Bundle.getString("Watch.undefinedField");
            } catch (ObjectCollectedException oce) {
                errorMsg = Bundle.getString("Watch.objectCollected");
            }

            if (errorMsg != null) {
                session.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_WARNING, errorMsg);
                return;
            }
        }

        String filtersStr = getClassFilters();
        List filters = null;
        if (filtersStr != null && filtersStr.length() > 0) {
            filters = Strings.stringToList(filtersStr);
        }

        // Create the new requests.
        if (onAccess) {
            accessRequest = erm.createAccessWatchpointRequest(watchedField);
            accessRequest.putProperty("breakpoint", this);
            accessRequest.setSuspendPolicy(getSuspendPolicy());
            if (objectRef != null) {
                accessRequest.addInstanceFilter(objectRef);
            }
            filtersStr = getThreadFilters();
            if (filtersStr != null && filtersStr.length() > 0) {
                filters = Strings.stringToList(filtersStr);
                for (int ii = 0; ii < filters.size(); ii++) {
                    String tid = (String) filters.get(ii);
                    ThreadReference thread =
                        Threads.getThreadByID(vm, tid);
                    if (thread != null) {
                        accessRequest.addThreadFilter(thread);
                    } else {
                        session.getUIAdapter().showMessage(
                            UIAdapter.MESSAGE_WARNING,
                            Bundle.getString("noSuchThreadFilter")
                            + ' ' + tid);
                    }
                }
            }
            if (filters != null) {
                for (int ii = 0; ii < filters.size(); ii++) {
                    String filter = (String) filters.get(ii);
                    accessRequest.addClassFilter(filter);
                }
            }
            accessRequest.setEnabled(true);
        }

        if (onModify) {
            modifyRequest = erm.createModificationWatchpointRequest(
                watchedField);
            modifyRequest.putProperty("breakpoint", this);
            modifyRequest.setSuspendPolicy(getSuspendPolicy());
            if (objectRef != null) {
                if (vm.canUseInstanceFilters()) {
                    modifyRequest.addInstanceFilter(objectRef);
                } else {
                    // No filter support, wipe object reference.
                    objectRef = null;
                    session.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING,
                        Bundle.getString("Watch.noInstanceFilterSupport"));
                }
            }
            filtersStr = getThreadFilters();
            if (filtersStr != null && filtersStr.length() > 0) {
                filters = Strings.stringToList(filtersStr);
                for (int ii = 0; ii < filters.size(); ii++) {
                    String tid = (String) filters.get(ii);
                    ThreadReference thread =
                        Threads.getThreadByID(vm, tid);
                    if (thread != null) {
                        modifyRequest.addThreadFilter(thread);
                    } else {
                        session.getUIAdapter().showMessage(
                            UIAdapter.MESSAGE_WARNING,
                            Bundle.getString("noSuchThreadFilter")
                            + ' ' + tid);
                    }
                }
            }
            if (filters != null) {
                for (int ii = 0; ii < filters.size(); ii++) {
                    String filter = (String) filters.get(ii);
                    modifyRequest.addClassFilter(filter);
                }
            }
            modifyRequest.setEnabled(true);
        }
        super.setEnabled(true);
    } // createRequests

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
        // Must disable ourselves. We cannot possibly resolve again when
        // the session is deactivated. The user will have to enable us when
        // the current location is set.
        setEnabled(false);
        watchedField = null;
        // Yep, this is a one-off filter; once the session deactivates,
        // the filter is dead.
        objectRef = null;
    } // deactivated

    /**
     * Delete the method entry and exit event requests.
     */
    protected void deleteRequests() {
        // Delete the old requests, if any.
        try {
            if (accessRequest != null) {
                VirtualMachine vm = accessRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(accessRequest);
            }
            if (modifyRequest != null) {
                VirtualMachine vm = modifyRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(modifyRequest);
            }
        } catch (VMDisconnectedException vmde) {
            // This happens all the time.
        }
        accessRequest = null;
        modifyRequest = null;
    } // deleteRequests

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        super.destroy();
        deleteRequests();

        // We need to stop listening for events.
        Session session = getBreakpointGroup().getSession();
        session.removeListener(this);
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.removeListener(AccessWatchpointEvent.class, this);
        vmeman.removeListener(ModificationWatchpointEvent.class, this);
    } // destroy

    /**
     * Returns the name of the field being watched.
     *
     * @return  name of field.
     */
    public String getFieldName() {
        return fieldName;
    } // getFieldName

    /**
     * Returns the object instance filter, if nay.
     *
     * @return  object instance filter, or null.
     */
    public ObjectReference getObjectFilter() {
        return objectRef;
    } // getObjectFilter

    /**
     * Returns the stop-on-access status.
     *
     * @return  true if stopping when field is accessed.
     */
    public boolean getStopOnAccess() {
        return onAccess;
    } // getStopOnAccess

    /**
     * Returns the stop-on-modify status.
     *
     * @return  true if stopping when field is modified.
     */
    public boolean getStopOnModify() {
        return onModify;
    } // getStopOnModify

    /**
     * Returns the user interface widget for customizing this breakpoint.
     * This method returns a new ui adapter each time it is called.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return new WatchBreakpointUI(this);
    } // getUIAdapter

    /**
     * Initialize the breakpoint so it may operate normally.
     */
    public void init() {
        // We need to listen for watchpoint events.
        Session session = getBreakpointGroup().getSession();
        session.addListener(this);

        // At this point, opened(Session) has been called
        // and activated(Session) may have been called.

        if (!session.isActive()) {
            // We are disabled until we are resolved.
            super.setEnabled(false);
        }

        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.addListener(AccessWatchpointEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
        vmeman.addListener(ModificationWatchpointEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
    } // init

    /**
     * Returns true if the breakpoint has been resolved against the
     * intended object in the debuggee VM. How a breakpoint resolves itself
     * depends on the type of the breakpoint.
     *
     * @return  returns true if Field is known, false otherwise.
     */
    public boolean isResolved() {
        return accessRequest != null || modifyRequest != null;
    } // isResolved

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
    } // opened

    /**
     * This breakpoint has caused the debuggee VM to stop. Execute all
     * monitors associated with this breakpoint. If the breakpoint is
     * locatable, perform the usual operations that go along with a
     * locatable event.
     *
     * @param  e  Event for which we are stopping.
     * @return  true if VM should resume, false otherwise.
     */
    protected boolean performStop(Event e) {
        // Perform the default breakpoint behavior.
        super.performStop(e);

        // Show the watch information.
        Session session = getBreakpointGroup().getSession();
        StringBuffer buf = new StringBuffer(80);
        Value valueToBe = null;
        if (e instanceof AccessWatchpointEvent) {
            buf.append(Bundle.getString("Watch.accessed"));
        } else if (e instanceof ModificationWatchpointEvent) {
            buf.append(Bundle.getString("Watch.modified"));
            valueToBe = ((ModificationWatchpointEvent) e).valueToBe();
        }

        WatchpointEvent we = (WatchpointEvent) e;
        buf.append(' ');
        buf.append(we.field().name());
        buf.append(", ");
        buf.append(String.valueOf(we.object().uniqueID()));
        buf.append(", ");
        if (valueToBe == null) {
            buf.append(we.valueCurrent());
        } else {
            buf.append(valueToBe);
        }
        session.getUIAdapter().showMessage(
            UIAdapter.MESSAGE_NOTICE, buf.toString());

        return false;
    } // performStop

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        fieldName = prefs.get("fieldName", null);
        onAccess = prefs.getBoolean("onAccess", true);
        onModify = prefs.getBoolean("onModify", true);
        return super.readObject(prefs);
    } // readObject

    /**
     * Reset the stopped count to zero and clear any other attributes such
     * that this breakpoint can be used again for a new session. This does
     * not change the enabled-ness of the breakpoint.
     */
    public void reset() {
        super.reset();
        deleteRequests();
    } // reset

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Enables or disables this breakpoint, according to the parameter.
     * This only affects the breakpoint itself. If the breakpoint group
     * containing this breakpoint is disabled, this breakpoint will remain
     * effectively disabled.
     *
     * @param  enabled  true if breakpoint should be enabled, false
     *                  if breakpoint should be disabled.
     * @see #isEnabled
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            // Re-create the requests using the latest settings.
            try {
                createRequests();
            } catch (Exception e) {
                // Hmm, maybe the Field has become stale due to a hotswap.
                // Let's try resolving the field again.
                watchedField = null;
                // If this fails, too bad.
                createRequests();
            }
            // The above method may have disabled us again if
            // there was an error.
        } else {
            deleteRequests();
        }
    } // setEnabled

    /**
     * Sets the name of the field to watch.
     *
     * @param  name   name of field to watch.
     */
    public void setFieldName(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name must be non-empty");
        }
        fieldName = name;
    } // setFieldName

    /**
     * Sets the stop-on-access status. Caller must disable this breakpoint
     * before calling this method.
     *
     * @param  stop  true to stop when field is accessed.
     */
    public void setStopOnAccess(boolean stop) {
        onAccess = stop;
    } // setStopOnAccess

    /**
     * Sets the stop-on-modify status. Caller must disable this breakpoint
     * before calling this method.
     *
     * @param  stop  true to stop when field is modified.
     */
    public void setStopOnModify(boolean stop) {
        onModify = stop;
    } // setStopOnModify

    /**
     * Set the suspend policy for the request. Use one of the
     * <code>com.sun.jdi.request.EventRequest</code> constants for
     * suspending threads. The breakpoint must be disabled before calling
     * this method.
     *
     * @param  policy  one of the EventRequest suspend constants.
     */
    public void setSuspendPolicy(int policy) {
        super.setSuspendPolicy(policy);
        if (accessRequest != null) {
            accessRequest.setSuspendPolicy(getSuspendPolicy());
        }
        if (modifyRequest != null) {
            modifyRequest.setSuspendPolicy(getSuspendPolicy());
        }
    } // setSuspendPolicy

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended

    /**
     * Returns a String representation of this.
     *
     * @return  string representation of this.
     */
    public String toString() {
        return toString(false);
    } // toString

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     * @return  string representation of this.
     */
    public String toString(boolean terse) {
        StringBuffer buf = new StringBuffer(80);
        buf.append("watch ");
        buf.append(fieldName);
        String filters = getClassFilters();
        if (filters != null && filters.length() > 0) {
            buf.append(", class ");
            buf.append(filters);
        }
        filters = getThreadFilters();
        if (filters != null && filters.length() > 0) {
            buf.append(", thread ");
            buf.append(filters);
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

    /**
     * Writes the breakpoint properties to the given preferences node. It
     * is assumed that the preferences node is completely empty.
     *
     * @param  prefs  Preferences node to which to serialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean writeObject(Preferences prefs) {
        if (!super.writeObject(prefs)) {
            return false;
        }
        prefs.put("fieldName", fieldName);
        prefs.putBoolean("onAccess", onAccess);
        prefs.putBoolean("onModify", onModify);
        return true;
    } // writeObject
} // WatchBreakpoint
