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
 * FILE:        BreakpointManager.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/24/01        Initial version
 *      nf      08/21/01        Use readable resolve error message
 *      nf      08/23/01        Fixed bug #184, 191
 *      nf      08/27/01        Print breakpoint in resolve error
 *      nf      09/03/01        Removed clearAllBreakpoints and updated
 *                              breakpointsTable in removeBreakpoint()
 *      nf      03/20/02        Code snippet moved to StringUtils
 *      nf      10/27/02        Fixed bug 572, 580
 *
 * DESCRIPTION:
 *      Defines the breakpoint manager class.
 *
 * $Id: BreakpointManager.java 636 2002-10-27 20:54:28Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.DefaultManager;
import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.MalformedMemberNameException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.report.Category;
import com.bluemarsh.jswat.util.ClassUtils;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.util.EventListenerList;
import com.bluemarsh.util.IntHashtable;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Class BreakpointManager defines is responsible for managing groups
 * of breakpoints. It does not contain any breakpoints directly, but
 * rather contains the groups which contain the breakpoints. The
 * breakpoint manager acts as a factory for creating all types of
 * breakpoints.
 * <p>
 * The breakpoint manager contains a breakpoint group called "Default".
 * This default breakpoint group takes all the new breakpoints that are
 * not associated with any other group.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointManager extends DefaultManager implements VMEventListener {
    /** Reporting category. */
    protected static Category logCategory = Category.instanceOf("breakpoint");
    /** The EventRequestManager for this session. */
    protected EventRequestManager eventManager;
    /** List of breakpoint listeners. */
    protected EventListenerList listeners;
    /** Session that owns this breakpoint manager. */
    protected Session owningSession;
    /** If true, indicates we are presently listening for class prepare
     * events from the debuggee VM. */
    protected boolean listeningForClassPrepare;
    /** The default breakpoint group, into which all new groups and
     * breakpoints will go by default. */
    protected BreakpointGroup defaultGroup;
    /** Table of all breakpoints, keyed by a unique number. The
     * number is assigned at the time the breakpoint is created and
     * will be unique among the set of breakpoints.
     * @see #lastBreakpointNumber
     */
    protected IntHashtable breakpointsTable;
    /** Value representing the last number assigned to a new breakpoint.
     * Used to key breakpoints in a table so they may be referred to by
     * a unique number.
     * @see #breakpointsTable
     */
    protected int lastBreakpointNumber;

    /**
     * Creates a BreakpointManager with the default parameters.
     */
    public BreakpointManager() {
        super();
        listeners = new EventListenerList();
        breakpointsTable = new IntHashtable();
    } // BreakpointManager

    /**
     * Ready this breakpoint manager for handling breakpoint requests.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        VirtualMachine vm = session.getVM();
        eventManager = vm.eventRequestManager();

        // Decide if we should be listening for class prepare events
        // or not, depending on whether we have any breakpoints.
        if (defaultGroup.breakpointCount() > 0) {
            listenForClassPrepareEvents();
        }

        // Have to set up the breakpoints to get resolved.
        Iterator iter = defaultGroup.breakpoints(true);
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof ResolvableBreakpoint) {
                ResolvableBreakpoint rbp = (ResolvableBreakpoint) o;
                try {
                    resolveBreakpoint(rbp);
                } catch (ResolveException re) {
                    // Error occurred.
                    Log out = session.getStatusLog();
                    out.writeln(re.errorMessage());
                }
            }
        }

        // Have to enable the default group so new breakpoints will
        // be enabled.
        defaultGroup.setEnabled(true);
    } // activate

    /**
     * Add a breakpoint listener to this manager object.
     *
     * @param  listener  new listener to add notification list
     */
    public void addBreakListener(BreakpointListener listener) {
        if (logCategory.isEnabled()) {
            logCategory.report("adding breakpoint listener: " +
                               ClassUtils.justTheName(
                                   listener.getClass().getName()));
        }
        listeners.add(BreakpointListener.class, listener);
    } // addBreakListener

    /**
     * Adds the given breakpoint to this breakpoint group.
     *
     * @param  bp  breakpoint to add.
     */
    public void addBreakpoint(Breakpoint bp) {
        if (logCategory.isEnabled()) {
            logCategory.report("adding breakpoint: " + bp);
        }
        // Add the breakpoint to the default group.
        defaultGroup.addBreakpoint(bp);
        // Keep track of the breakpoint numbers.
        breakpointsTable.put(++lastBreakpointNumber, bp);
        bp.setProperty("number", new Integer(lastBreakpointNumber));

        if (bp instanceof ResolvableBreakpoint) {
            // Start listening for class prepare events.
            listenForClassPrepareEvents();
        }

        // Notify everyone that a breakpoint was added.
        fireChange(bp, BreakpointEvent.TYPE_ADDED);
    } // addBreakpoint

    /**
     * Add a breakpoint group listener to this manager object.
     *
     * @param  listener  new listener to add notification list
     */
    public void addGroupListener(GroupListener listener) {
        if (logCategory.isEnabled()) {
            logCategory.report("adding group listener: " +
                               ClassUtils.justTheName(
                                   listener.getClass().getName()));
        }
        listeners.add(GroupListener.class, listener);
    } // addGroupListener

    /**
     * Adds the given breakpoint to this breakpoint group and
     * initializes and resolves it.
     *
     * @param  bp  breakpoint to add.
     * @exception  ResolveException
     *             Thrown if error resolving breakpoint.
     */
    public void addNewBreakpoint(Breakpoint bp) throws ResolveException {
        // Add the breakpoint.
        addBreakpoint(bp);
        // Initialize breakpoint after adding it.
        bp.init();
        // Try to resolve the breakpoint now.
        if (bp instanceof ResolvableBreakpoint) {
            try {
                resolveBreakpoint((ResolvableBreakpoint) bp);
            } catch (ResolveException re) {
                // Remove the breakpoint, since a resolve exception
                // is always an uncorrectable problem.
                removeBreakpoint(bp);
                // Rethrow the exception so the caller handles it.
                throw re;
            }
        }
    } // addNewBreakpoint

    /**
     * Returns a count of the breakpoints under this manager.
     *
     * @param  recurse  true to include subgroup counts, false to ignore
     *                  subgroups.
     * @return  number of breakpoints.
     */ 
    public int breakpointCount(boolean recurse) {
        return defaultGroup.breakpointCount(recurse);
    } // breakpointCount

    /**
     * Returns an iterator over the set of breakpoints under this manager.
     *
     * @param  recurse  true to recurse through all the groups.
     * @return  Iterator over the breakpoints.
     */
    public Iterator breakpoints(boolean recurse) {
        return defaultGroup.breakpoints(recurse);
    } // breakpoints

    /**
     * Called when the Session is about to close down.
     *
     * @param  session  Session being closed.
     */
    public void close(Session session) {
        // Persist the breakpoints to a file.
        try {
            // Write the breakpoints as serialized objects to the
            // ~/.jswat/breakpoints file.
            File file = new File(System.getProperty("user.home") +
                                 File.separator + ".jswat", "breakpoints");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutput oo = new ObjectOutputStream(fos);
            oo.writeObject(defaultGroup);
            oo.close();
        } catch (IOException ioe) {
            // We don't expect anything to go wrong.
            ioe.printStackTrace();
        }

        owningSession = null;
    } // close

    /**
     * Creates a Breakpoint to stop at the specified line within the
     * specified class. The class name pattern may have a wildcard
     * (asterisk, *) at either the beginning or end.
     *
     * @param  classPattern  class name pattern.
     * @param  line          line within class at which to stop.
     * @return  new Breakpoint.
     * @exception  ClassNotFoundException
     *             Thrown if the class pattern was invalid.
     * @exception  ResolveException
     *             Thrown if error resolving breakpoint request.
     */
    public Breakpoint createBreakpoint(String classPattern, int line)
        throws ClassNotFoundException,
               ResolveException {
        if (logCategory.isEnabled()) {
            logCategory.report("creating breakpoint: " + classPattern +
                               ':' + line);
        }

        Breakpoint bp = new LineBreakpoint(classPattern, line);
        addNewBreakpoint(bp);
        return bp;
    } // createBreakpoint

    /**
     * Creates a Breakpoint to stop at the specified method within the
     * specified class. The class name pattern may have a wildcard
     * (asterisk, *) at either the beginning or end.
     *
     * @param  classPattern  class name pattern.
     * @param  methodId      name of method.
     * @param  methodArgs    list of argument types in method.
     * @return  new Breakpoint.
     * @exception  ClassNotFoundException
     *             Thrown if the class pattern was invalid.
     * @exception  ResolveException
     *             Thrown if error resolving breakpoint request.
     */
    public Breakpoint createBreakpoint(String classPattern,
                                       String methodId,
                                       List methodArgs)
        throws ClassNotFoundException,
               MalformedMemberNameException,
               ResolveException {
        if (logCategory.isEnabled()) {
            logCategory.report("creating breakpoint: " + classPattern +
                               '.' + methodId);
        }

        Breakpoint bp = new MethodBreakpoint(classPattern, methodId,
                                             methodArgs);
        addNewBreakpoint(bp);
        return bp;
    } // createBreakpoint

    /**
     * Creates a new breakpoint group and adds it to this manager.
     *
     * @param  name  new breakpoint group's name.
     */
    public BreakpointGroup createBreakpointGroup(String name) {
        return createBreakpointGroup(name, defaultGroup);
    } // createBreakpointGroup

    /**
     * Creates a new breakpoint group and adds it to the given
     * breakpoint group.
     *
     * @param  name    new breakpoint group's name.
     * @param  parent  parent breakpoint group.
     */
    public BreakpointGroup createBreakpointGroup(String name,
                                                 BreakpointGroup parent) {
        BreakpointGroup group = new BreakpointGroup(name);
        if (logCategory.isEnabled()) {
            logCategory.report("creating breakpoint group: " + group);
        }
        parent.addBreakpointGroup(group);
        fireGroupChange(group, GroupEvent.TYPE_ADDED);
        return group;
    } // createBreakpointGroup

    /**
     * Creates a Breakpoint to stop when the specified exception occurs.
     * The exception class name pattern may have a wildcard (asterisk, *)
     * at either the beginning or end.
     *
     * @param  classPattern  name of the exception to catch.
     * @return  new Breakpoint.
     * @exception  ClassNotFoundException
     *             Thrown if the class pattern was invalid.
     * @exception  ResolveException
     *             Thrown if error resolving breakpoint request.
     */
    public Breakpoint createExceptionCatch(String classPattern)
        throws ClassNotFoundException,
               ResolveException {
        if (logCategory.isEnabled()) {
            logCategory.report("creating exception catch: " + classPattern);
        }

        Breakpoint bp = new ExceptionBreakpoint(classPattern);
        addNewBreakpoint(bp);
        return bp;
    } // createExceptionCatch

    /**
     * Creates a Breakpoint to stop when any uncaught exception is thrown.
     * This breakpoint is never added to the default breakpoint group,
     * nor is it necessary for it to be resolved. In fact, for all intents
     * and purposes, this breakpoint won't really exist. It won't be
     * persisted nor will it be customizable.
     *
     * @exception  IllegalStateException
     *             Thrown if the session is not currently active.
     */
    public void createExceptionCatch() {
        if (eventManager == null) {
            throw new IllegalStateException("session must be active");
        }
        logCategory.report("creating exception catch for all " +
                           "uncaught exceptions");
        // Create an exception breakpoint that will catch any and all
        // exceptions that are thrown.
        new UncaughtExceptionBreakpoint(eventManager).init(owningSession);
    } // createExceptionCatch

    /**
     * Creates a Breakpoint to trace method entry and exit events.
     *
     * @param  classes  class filters, separated by commas.
     * @param  threads  thread filters, separated by commas.
     * @return  new Breakpoint.
     */
    public Breakpoint createTrace(String classes, String threads) {
        if (logCategory.isEnabled()) {
            logCategory.report("creating trace: " + classes + ", " + threads);
        }

        Breakpoint bp = new TraceBreakpoint(classes, threads);
        try {
            addNewBreakpoint(bp);
        } catch (ResolveException re) {
            // this won't happen
        }
        return bp;
    } // createTrace

    /**
     * Prepare the breakpoint manager for disconnection from the
     * virtual machine we're debugging. This means dropping the
     * association with the EventRequestManager given in the
     * <code>activate</code> method.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
        eventManager = null;

        // Stop listening for class prepare events.
        if (listeningForClassPrepare) {
            VMEventManager vmeman = (VMEventManager)
                session.getManager(VMEventManager.class);
            vmeman.removeListener(ClassPrepareEvent.class, this);
            listeningForClassPrepare = false;
        }

        // Reset all of the breakpoints.
        Iterator iter = defaultGroup.groups(true);
        while (iter.hasNext()) {
            BreakpointGroup group = (BreakpointGroup) iter.next();
            group.reset();
        }
    } // deactivate

    /**
     * Disables the given breakpoint and notifies any listeners.
     *
     * @param  bp  Breakpoint to disable.
     */
    public void disableBreakpoint(Breakpoint bp) {
        if (bp.isEnabled()) {
            bp.setEnabled(false);
            if (logCategory.isEnabled()) {
                logCategory.report("disabling breakpoint: " + bp);
            }
        }
    } // disableBreakpoint

    /**
     * Disables the given breakpoint group and notifies any listeners.
     *
     * @param  group  breakpoint group to disable.
     */
    public void disableBreakpointGroup(BreakpointGroup group) {
        if (group.isEnabled()) {
            group.setEnabled(false);
            if (logCategory.isEnabled()) {
                logCategory.report("disabling breakpoint group: " + group);
            }
            // Fire off disable events for all the affected breakpoints.
            Iterator iter = group.breakpoints(true);
            while (iter.hasNext()) {
                Breakpoint bp = (Breakpoint) iter.next();
                fireChange(bp, BreakpointEvent.TYPE_MODIFIED);
            }
            fireGroupChange(group, GroupEvent.TYPE_DISABLED);
        }
    } // disableBreakpointGroup

    /**
     * Enables the given breakpoint and notifies any listeners.
     *
     * @param  bp  Breakpoint to enable.
     */
    public void enableBreakpoint(Breakpoint bp) {
        if (!bp.isEnabled()) {
            bp.setEnabled(true);
            if (logCategory.isEnabled()) {
                logCategory.report("enabling breakpoint: " + bp);
            }
        }
    } // enableBreakpoint

    /**
     * Enables the given breakpoint group and notifies any listeners.
     *
     * @param  group  breakpoint group to enable.
     */
    public void enableBreakpointGroup(BreakpointGroup group) {
        if (!group.isEnabled()) {
            group.setEnabled(true);
            if (logCategory.isEnabled()) {
                logCategory.report("enabling breakpoint group: " + group);
            }
            // Fire off enable events for all the affected breakpoints.
            Iterator iter = group.breakpoints(true);
            while (iter.hasNext()) {
                Breakpoint bp = (Breakpoint) iter.next();
                // Need to make sure the breakpoint is really enabled.
                // It may be the case that a parent group is disabled, or
                // the breakpoint was disabled from an earlier event.
                if (bp.isEnabled()) {
                    fireChange(bp, BreakpointEvent.TYPE_MODIFIED);
                }
            }
            fireGroupChange(group, GroupEvent.TYPE_ENABLED);
        }
    } // enableBreakpointGroup

    /**
     * Invoked when a VM event has occurred.
     *
     * @param  e  VM event (class prepare event)
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e) {
        // We only register for ClassPrepareEvent, so no need to check.

        // Let's see if this event was brought on by a breakpoint.
        EventRequest eventRequest = e.request();
        Object o = eventRequest.getProperty("breakpoint");
        if (o != null && o instanceof ResolvableBreakpoint) {
            ResolvableBreakpoint bp = (ResolvableBreakpoint) o;

            // Yes, a breakpoint is waiting to resolve.
            EventRequest oldER = bp.eventRequest();
            EventRequest newER = null;
            try {
                if (bp.isEnabled()) {
                    newER = bp.resolveAgainstEvent((ClassPrepareEvent) e);
                }
            } catch (ResolveException re) {
                // Error occurred.
                Log out = owningSession.getStatusLog();
                owningSession.setStatus(Bundle.getString(
                    "BreakManager.failedResolve"));
                out.writeln(bp.toString());
                out.writeln(re.errorMessage());
                return false;
            }
            if (newER != null && oldER != newER) {
                if (logCategory.isEnabled()) {
                    logCategory.report("resolved " + bp);
                }
                // Hurray, it has just been (re)resolved.
                fireChange(bp, BreakpointEvent.TYPE_MODIFIED);
            }
        }
        return true;
    } // eventOccurred

    /**
     * Let all the change listeners know of a recent change
     * in the breakpoints. This creates a BreakpointEvent
     * object and sends it out to the listeners.
     *
     * @param  bp    breakpoint
     * @param  type  breakpoint event type
     */
    public void fireChange(Breakpoint bp, int type) {
        // Nothing to do if no listeners registered.
        if (listeners == null) {
            return;
        }
        // Create the change event.
        BreakpointEvent bce = null;
        // Get the listener list as class/instance pairs.
        Object[] list = listeners.getListenerList();
        // Process the listeners last to first.
        // List is in pairs: class, instance
        for (int i = list.length - 2; i >= 0; i -= 2) {
            if (list[i] == BreakpointListener.class) {
                if (bce == null) {
                    // Lazily create the event.
                    bce = new BreakpointEvent(this, bp, type);
                }
                BreakpointListener cl = (BreakpointListener) list[i + 1];
                switch (type) {
                case BreakpointEvent.TYPE_ADDED :
                    cl.breakpointAdded(bce);
                    break;
                case BreakpointEvent.TYPE_MODIFIED :
                    cl.breakpointModified(bce);
                    break;
                case BreakpointEvent.TYPE_REMOVED :
                    cl.breakpointRemoved(bce);
                    break;
                default :
                    throw new IllegalArgumentException("invalid type");
                }
            }
        }
    } // fireChange

    /**
     * Let all the group change listeners know of a recent change
     * in the breakpoint groups. This creates a GroupEvent object
     * and sends it out to the listeners.
     *
     * @param  group  breakpoint group.
     * @param  type   breakpoint group event type.
     */
    protected void fireGroupChange(BreakpointGroup group, int type) {
        // Nothing to do if no listeners registered.
        if (listeners == null) {
            return;
        }
        GroupEvent ge = null;
        Object[] list = listeners.getListenerList();
        // Process the listeners last to first.
        // List is in pairs: class, instance
        for (int i = list.length - 2; i >= 0; i -= 2) {
            if (list[i] == GroupListener.class) {
                if (ge == null) {
                    // Lazily create the event.
                    ge = new GroupEvent(this, group, type);
                }
                GroupListener gl = (GroupListener) list[i + 1];
                switch (type) {
                case GroupEvent.TYPE_ADDED :
                    gl.groupAdded(ge);
                    break;
                case GroupEvent.TYPE_DISABLED :
                    gl.groupDisabled(ge);
                    break;
                case GroupEvent.TYPE_ENABLED :
                    gl.groupEnabled(ge);
                    break;
                case GroupEvent.TYPE_REMOVED :
                    gl.groupRemoved(ge);
                    break;
                default :
                    throw new IllegalArgumentException("invalid type");
                }
            }
        }
    } // fireGroupChange

    /**
     * Look through the existing breakpoints to see if there is one
     * with the given class type and line number. If a match is found,
     * this method returns the breakpoint; otherwise it returns null.
     *
     * @param  cname  fully-qualified class name.
     * @param  line   line number.
     * @return  Breakpoint, or null if none was found.
     */
    public Breakpoint getBreakpoint(String cname, int line) {
        Iterator iter = defaultGroup.breakpoints(true);
        while (iter.hasNext()) {
            Breakpoint bp = (Breakpoint) iter.next();
            if (bp instanceof LocatableBreakpoint) {
                LocatableBreakpoint lb = (LocatableBreakpoint) bp;
                if (lb.getClassName().equals(cname) &&
                    lb.getLineNumber() == line) {
                    return bp;
                }
            }
        }
        return null;
    } // getBreakpoint

    /**
     * Retrieve the nth breakpoint. Each and every breakpoint managed
     * by this breakpoint manager has a unique, invariant number
     * assigned to it. The breakpoints can be retrieved by referring
     * to this number.
     *
     * @param  n   breakpoint number.
     * @return  Breakpoint, or null if there was no breakpoint referenced
     *          by the <code>n</code> value.
     */
    public Breakpoint getBreakpoint(int n) {
        return (Breakpoint) breakpointsTable.get(n);
    } // getBreakpoint

    /**
     * Retrieve the number assigned to the given breakpoint. Each and every
     * breakpoint managed by this breakpoint manager has a unique, invariant
     * number assigned to it. The breakpoints can be retrieved by referring
     * to this number.
     *
     * @param  bp  breakpoint.
     * @return  number assigned to this breakpoint.
     */
    public int getBreakpointNumber(Breakpoint bp) {
        Integer n = (Integer) bp.getProperty("number");
        return n.intValue();
    } // getBreakpointNumber

    /**
     * Get the qualified name of the class containing the current context
     * if it exists. Otherwise return null.
     *
     * @return  current type name, or null if none.
     */
    protected String getCurrentTypeName() {
        String currentTypeName = null;
        ContextManager contextManager = (ContextManager)
            owningSession.getManager(ContextManager.class);
        Location currentLocation = contextManager.getCurrentLocation();
        if (currentLocation != null) {
            ReferenceType currentType = currentLocation.declaringType();
            if (currentType != null) {
                currentTypeName = currentType.name();
            }
        }
        return currentTypeName;
    } // getCurrentTypeName

    /**
     * Returns the default breakpoint group.
     *
     * @return  "default" breakpoint group.
     */
    public BreakpointGroup getDefaultGroup() {
        return defaultGroup;
    } // getDefaultGroup

    /**
     * Returns an iterator over the set of groups under this manager.
     *
     * @param  recurse  true to iterate over all subgroups.
     * @return  Iterator over the groups.
     */
    public Iterator groups(boolean recurse) {
        return defaultGroup.groups(recurse);
    } // groups

    /**
     * Called after the Session has instantiated this mananger.
     * To avoid problems with circular dependencies between managers,
     * iniitialize data members before calling
     * <code>Session.getManager()</code>.
     *
     * @param  session  Session initializing this manager.
     */
    public void init(Session session) {
        owningSession = session;

        // Recreate the breakpoints from the persistent store.
        try {
            File file = new File(System.getProperty("user.home") +
                                 File.separator + ".jswat", "breakpoints");
            if (file.exists() && file.canRead()) {
                logCategory.report("reading breakpoints from file");
                FileInputStream fis = new FileInputStream(file);
                ObjectInput oi = new ObjectInputStream(fis);
                defaultGroup = (ManagerGroup) oi.readObject();
                oi.close();
            }
        } catch (Exception e) {
            session.getStatusLog().writeln(
                "Breakpoints deserialization failed.");
        }

        if (defaultGroup == null) {
            // Fall back on this if the deserialization failed.
            logCategory.report("creating default breakpoint group");
            defaultGroup = new ManagerGroup();
        }
        ((ManagerGroup) defaultGroup).setSession(session);

        // Iterate the breakpoints and get their 'number' property,
        // and use that value to add the breakpoint to the table.
        Iterator iter = defaultGroup.breakpoints(true);
        while (iter.hasNext()) {
            Breakpoint bp = (Breakpoint) iter.next();
            Integer bpn = (Integer) bp.getProperty("number");
            int bpi = bpn.intValue();
            breakpointsTable.put(bpi, bp);
            if (bpi > lastBreakpointNumber) {
                lastBreakpointNumber = bpi;
            }

            // Initialize the breakpoint.
            bp.init();
        }
    } // init

    /**
     * Listen to all class prepare events.
     */
    protected void listenForClassPrepareEvents() {
        if (!listeningForClassPrepare) {
            VMEventManager vmeman = (VMEventManager)
                owningSession.getManager(VMEventManager.class);
            vmeman.addListener(ClassPrepareEvent.class, this,
                               VMEventListener.PRIORITY_HIGH);
            listeningForClassPrepare = true;
        }
    } // listenForClassPrepareEvents

    /**
     * Try to parse the given user breakpoint specification into
     * it's component parts and create a Breakpoint object.
     *
     * @param  tokenizer  String tokenizer to parse.
     * @return  a new Breakpoint.
     * @exception  ClassNotFoundException
     *             Thrown if class name is invalid or missing.
     * @exception  IllegalArgumentException
     *             Thrown if specification is missing.
     * @exception  MalformedMemberNameException
     *             Thrown if method name is invalid or missing.
     * @exception  NumberFormatException
     *             Thrown if line number is invalid or missing.
     * @exception  ResolveException
     *             Thrown if there was a problem resolving the breakpoint.
     */
    public Breakpoint parseBreakpointSpec(StringTokenizer tokenizer)
        throws ClassNotFoundException,
               IllegalArgumentException,
               MalformedMemberNameException,
               NumberFormatException,
               ResolveException {
        
        // Save the first token for interpretation.
        Breakpoint breakpoint = null;
        String firstToken;
        try {
            firstToken = tokenizer.nextToken(" :(");
        } catch (NoSuchElementException nsee) {
            throw new IllegalArgumentException("missing specification");
        }

        // Get the rest of the breakpoint specification.
        String restOfSpec = null;
        if (tokenizer.hasMoreTokens()) {
            // This retrieves delimiters but trims whitespace.
            restOfSpec = tokenizer.restTrim();
        } else {
            // Test if argument was just a line number
            try {
                int lineNumber = Integer.parseInt(firstToken);

                // Create the breakpoint in the current method
                String currentTypeName = getCurrentTypeName();
                if(currentTypeName == null) {
                    throw new IllegalArgumentException
                        ("no class name specified");
                }
                return createBreakpoint(currentTypeName, lineNumber);
            } catch(NumberFormatException e) {}
        }

        if ((restOfSpec != null) && restOfSpec.startsWith(":")) {
            // It's of the form <class-id>:<line-number>
            String classId = firstToken;
            String lineToken = restOfSpec.substring(1);
            int lineNumber = Integer.parseInt(lineToken);
            return createBreakpoint(classId, lineNumber);
        }

        // Must be a method breakpoint
        List argList = null;
        String classId;
        String methodName;

        if (restOfSpec != null) {
            // Must have specified arguments
            if (!restOfSpec.startsWith("(") || !restOfSpec.endsWith(")")) {
                throw new MalformedMemberNameException
                    ("invalid method: " + firstToken + restOfSpec);
            }
            // Trim the parentheses from the args list.
            restOfSpec = restOfSpec.substring(1, restOfSpec.length() - 1);

            // Get the argument types into a list.
            argList = StringUtils.stringToList(restOfSpec);
        }

        // Parse method name, either <method> or <class-id>.<method>
        // Try stripping class from class.method token.
        int idot = firstToken.lastIndexOf(".");
        // Dot in first/last character.
        if ((idot == 0) || (idot == firstToken.length() - 1)) {
            // Bad form, notify the user.
            throw new MalformedMemberNameException("missing method name");
        }

        if (idot == -1) {
            // No dot, just have a method name - use the current class
            methodName = firstToken;
            classId = getCurrentTypeName();

            if (classId == null) {
                throw new IllegalArgumentException("class name required");
            }
        } else {
            // Separate the class name and method name
            methodName = firstToken.substring(idot + 1);
            classId = firstToken.substring(0, idot);
        }

        return createBreakpoint(classId, methodName, argList);
    } // parseBreakpointSpec

    /**
     * Remove a break listener from the listener list.
     *
     * @param  listener  listener to remove from notification list
     */
    public void removeBreakListener(BreakpointListener listener) {
        if (logCategory.isEnabled()) {
            logCategory.report("removing breakpoint listener: " +
                               ClassUtils.justTheName(
                                   listener.getClass().getName()));
        }
        listeners.remove(BreakpointListener.class, listener);
    } // removeBreakListener

    /**
     * Removes the given breakpoint from this breakpoint manager.
     * This results in the breakpoint being effectively unreachable,
     * as well as disabled.
     * Fires a breakpoint removed event to all the listeners.
     *
     * @param  bp  breakpoint to remove.
     */
    public void removeBreakpoint(Breakpoint bp) {
        if (logCategory.isEnabled()) {
            logCategory.report("removing breakpoint: " + bp);
        }
        // Notify the listeners before taking action.
        fireChange(bp, BreakpointEvent.TYPE_REMOVED);
        BreakpointGroup parent = bp.getBreakpointGroup();
        // Remove the breakpoint from its group.
        parent.removeBreakpoint(bp);
        Integer bpn = (Integer) bp.getProperty("number");
        // Remove the breakpoint from our number table.
        breakpointsTable.remove(bpn.intValue());
        bp.setEnabled(false);
        bp.destroy();
    } // removeBreakpoint

    /**
     * Removes the given breakpoint group from this breakpoint manager.
     * This results in all of the child groups and breakpoints contained
     * therein to be removed as well.
     * Fires breakpoint removed events for all affected breakpoints.
     *
     * @param  group  breakpoint group to remove.
     * @exception  IllegalArgumentException
     *             Thrown if the group is the default group.
     */
    public void removeBreakpointGroup(BreakpointGroup group) {
        if (group == defaultGroup) {
            throw new IllegalArgumentException("cannot remove default group");
        }

        if (logCategory.isEnabled()) {
            logCategory.report("removing breakpoint group: " + group);
        }

        // Notify the listeners before taking action.
        Iterator iter = group.breakpoints(true);
        while (iter.hasNext()) {
            Breakpoint bp = (Breakpoint) iter.next();
            fireChange(bp, BreakpointEvent.TYPE_REMOVED);
        }

        // Remove the breakpoint group.
        BreakpointGroup parent = group.getParent();
        parent.removeBreakpointGroup(group);
        fireGroupChange(group, GroupEvent.TYPE_REMOVED);
    } // removeBreakpointGroup

    /**
     * Remove a breakpoint group listener from the listener list.
     *
     * @param  listener  listener to remove from notification list
     */
    public void removeGroupListener(GroupListener listener) {
        if (logCategory.isEnabled()) {
            logCategory.report("removing group listener: " +
                               ClassUtils.justTheName(
                                   listener.getClass().getName()));
        }
        listeners.remove(GroupListener.class, listener);
    } // removeGroupListener

    /**
     * Try to resolve the given breakpoint.
     *
     * @param  bp  breakpoint to be resolved.
     * @exception  ResolveException
     *             Thrown if error resolving breakpoint.
     */
    public void resolveBreakpoint(ResolvableBreakpoint bp)
        throws ResolveException {

        VirtualMachine vm = owningSession.getVM();
        if (vm != null && !bp.isResolved() && bp.isEnabled()) {
            bp.resolveEagerly(vm);
            if (bp.isResolved()) {
                if (logCategory.isEnabled()) {
                    logCategory.report("resolved " + bp);
                }
                fireChange(bp, BreakpointEvent.TYPE_MODIFIED);
            }
        }
    } // resolveBreakpoint
} // BreakpointManager
