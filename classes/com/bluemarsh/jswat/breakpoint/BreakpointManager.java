/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 * $Id: BreakpointManager.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.MalformedMemberNameException;
import com.bluemarsh.jswat.Manager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.VMConnection;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.EventListenerList;
import com.bluemarsh.jswat.util.Names;
import com.bluemarsh.jswat.util.Strings;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Class BreakpointManager defines is responsible for managing groups
 * of breakpoints. It does not contain any breakpoints directly, but
 * rather contains the groups which contain the breakpoints. The
 * breakpoint manager acts as a factory for creating all types of
 * breakpoints.
 *
 * <p>The breakpoint manager contains a breakpoint group called "Default".
 * This default breakpoint group takes all the new breakpoints that are
 * not associated with any other group.</p>
 *
 * @author  Nathan Fiedler
 */
public class BreakpointManager implements Manager, VMEventListener {
    /** Logger. */
    private static Logger logger;
    /** The EventRequestManager for this session. */
    private EventRequestManager eventManager;
    /** List of breakpoint listeners. */
    private EventListenerList listeners;
    /** Session that owns this breakpoint manager. */
    private Session owningSession;
    /** If true, indicates we are presently listening for class prepare
     * events from the debuggee VM. */
    private boolean listeningForClassPrepare;
    /** The default breakpoint group, into which all new groups and
     * breakpoints will go by default. */
    private BreakpointGroup defaultGroup;
    /** Table of all breakpoints, keyed by a unique number. The
     * number is assigned at the time the breakpoint is created and
     * will be unique among the set of breakpoints.
     * @see #lastBreakpointNumber
     */
    private Hashtable breakpointsTable;
    /** Value representing the last number assigned to a new breakpoint.
     * Used to key breakpoints in a table so they may be referred to by
     * a unique number.
     * @see #breakpointsTable
     */
    private int lastBreakpointNumber;

    static {
        // Initialize the logger.
        logger = Logger.getLogger("com.bluemarsh.jswat.breakpoint");
        com.bluemarsh.jswat.logging.Logging.setInitialState(logger);
    }

    /**
     * Creates a BreakpointManager with the default parameters.
     */
    public BreakpointManager() {
        super();
        listeners = new EventListenerList();
        breakpointsTable = new Hashtable();
    } // BreakpointManager

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        VirtualMachine vm = owningSession.getVM();
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
                    owningSession.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING, re.errorMessage());
                }
            }
        }

        // Have to enable the default group so new breakpoints will
        // be enabled.
        defaultGroup.setEnabled(true);

        // See if we should stop in the main() method.
        Preferences node = Preferences.userRoot().node(
            "com/bluemarsh/jswat/breakpoint");
        VMConnection connection = owningSession.getConnection();
        if (node.getBoolean("stopOnMain", Defaults.STOP_ON_MAIN)
            && !connection.isRemote()) {
            // First check if a breakpoint does not already exist
            // (in the unlikely event that the previous session was
            // not resumed and/or the breakpoint was never hit).
            boolean alreadyExists = false;
            iter = defaultGroup.breakpoints(true);
            while (iter.hasNext()) {
                Object o = iter.next();
                if (o instanceof MethodBreakpoint) {
                    MethodBreakpoint mbp = (MethodBreakpoint) o;
                    String name = mbp.getMethodName();
                    if (name.equals("main")) {
                        List args = mbp.getMethodArgs();
                        if (args.size() == 1) {
                            String arg = (String) args.get(0);
                            if (arg.equals("java.lang.String[]")) {
                                alreadyExists = true;
                                break;
                            }
                        }
                    }
                }
            }

            // Set a breakpoint on the main() method.
            String cname = connection.getMainClass();
            if (!alreadyExists && cname != null) {
                List argList = new ArrayList();
                argList.add("java.lang.String[]");
                try {
                    Breakpoint bp = new MethodBreakpoint(
                        cname, "main", argList);
                    // Make this breakpoint short-lived.
                    bp.deleteOnExpire();
                    bp.setExpireCount(1);
                    addNewBreakpoint(bp);
                } catch (ClassNotFoundException cnfe) {
                    // oh well, too bad
                } catch (MalformedMemberNameException mmne) {
                    // this is impossible
                } catch (ResolveException re) {
                    // we can expect this every time
                }
            }
        }
    } // activated

    /**
     * Add a breakpoint listener to this manager object.
     *
     * @param  listener  new listener to add notification list
     */
    public void addBreakListener(BreakpointListener listener) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("adding breakpoint listener: "
                        + Names.justTheName(
                            listener.getClass().getName()));
        }
        listeners.add(BreakpointListener.class, listener);
    } // addBreakListener

    /**
     * Add a breakpoint group listener to this manager object.
     *
     * @param  listener  new listener to add notification list
     */
    public void addGroupListener(GroupListener listener) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("adding group listener: "
                        + Names.justTheName(
                            listener.getClass().getName()));
        }
        listeners.add(GroupListener.class, listener);
    } // addGroupListener

    /**
     * Adds the given breakpoint to the default breakpoint group and
     * initializes it. If the breakpoint is resolvable, an attempt is
     * made to resolve it.
     *
     * @param  bp  breakpoint to add.
     * @throws  ResolveException
     *          if error resolving breakpoint.
     */
    public void addNewBreakpoint(Breakpoint bp) throws ResolveException {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("adding breakpoint: " + bp);
        }
        // Add the breakpoint to the default group.
        defaultGroup.addBreakpoint(bp);
        // Keep track of the breakpoint numbers.
        synchronized (breakpointsTable) {
            breakpointsTable.put(new Integer(++lastBreakpointNumber), bp);
            bp.setNumber(lastBreakpointNumber);
        }

        if (bp instanceof ResolvableBreakpoint) {
            // Start listening for class prepare events.
            listenForClassPrepareEvents();
        }

        // Notify everyone that a breakpoint was added.
        fireChange(bp, BreakpointEvent.TYPE_ADDED);
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
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
        // Persist the breakpoints to the preferences.
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/breakpoint/defaultGroup");
        // First completely obliterate this node.
        try {
            prefs.removeNode();
        } catch (BackingStoreException bse) {
            // Just overwrite the node then.
        }
        // Then recreate it from scratch.
        prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/breakpoint/defaultGroup");
        defaultGroup.writeObject(prefs);
        defaultGroup = null;
        owningSession = null;
    } // closing

    /**
     * Creates a new breakpoint group and adds it to this manager.
     *
     * @param  name  new breakpoint group's name.
     * @return  new breakpoint group.
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
     * @return  new breakpoint group.
     */
    public BreakpointGroup createBreakpointGroup(String name,
                                                 BreakpointGroup parent) {
        BreakpointGroup group = new BreakpointGroup(name);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("creating breakpoint group: " + group);
        }
        parent.addBreakpointGroup(group);
        fireGroupChange(group, GroupEvent.TYPE_ADDED);
        return group;
    } // createBreakpointGroup

    /**
     * Prepare the breakpoint manager for disconnection from the
     * virtual machine we're debugging. This means dropping the
     * association with the EventRequestManager given in the
     * <code>activate</code> method.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
        eventManager = null;

        // Stop listening for class prepare events.
        if (listeningForClassPrepare) {
            VMEventManager vmeman = (VMEventManager)
                owningSession.getManager(VMEventManager.class);
            vmeman.removeListener(ClassPrepareEvent.class, this);
            listeningForClassPrepare = false;
        }

        // Reset all of the breakpoints.
        Iterator iter = defaultGroup.groups(true);
        while (iter.hasNext()) {
            BreakpointGroup group = (BreakpointGroup) iter.next();
            group.reset();
        }
    } // deactivated

    /**
     * Disables the given breakpoint and notifies any listeners.
     *
     * @param  bp  Breakpoint to disable.
     */
    public void disableBreakpoint(Breakpoint bp) {
        if (bp.isEnabled()) {
            bp.setEnabled(false);
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
            if (logger.isLoggable(Level.INFO)) {
                logger.info("disabling breakpoint group: " + group);
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
            if (logger.isLoggable(Level.INFO)) {
                logger.info("enabling breakpoint group: " + group);
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
            // Yes, a breakpoint is waiting to resolve.
            ResolvableBreakpoint bp = (ResolvableBreakpoint) o;
            boolean resolved = false;
            try {
                if (bp.isEnabled()) {
                    resolved = bp.resolveAgainstEvent((ClassPrepareEvent) e);
                }
            } catch (ResolveException re) {
                StringBuffer sb = new StringBuffer(80);
                sb.append(Bundle.getString("BreakManager.failedResolve"));
                sb.append(' ');
                sb.append(bp);
                sb.append(": ");
                sb.append(re.errorMessage());
                owningSession.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_WARNING, sb.toString());
                // Treat this like any other event, so the listeners get
                // the suspend notification.
                owningSession.handleDebugEvent(e);
                return false;
            }
            if (resolved) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("resolved " + bp);
                }
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
     * Look through the existing breakpoints to see if there is one with
     * the given class type and line number. If a match is found, this
     * method returns the breakpoint; otherwise it returns null.
     *
     * @param  cname  fully-qualified class name.
     * @param  line   line number.
     * @return  breakpoint, or null if none was found.
     */
    public Breakpoint getBreakpoint(String cname, int line) {
        Iterator iter = defaultGroup.breakpoints(true);
        while (iter.hasNext()) {
            Breakpoint bp = (Breakpoint) iter.next();
            if (bp instanceof LocatableBreakpoint) {
                LocatableBreakpoint lb = (LocatableBreakpoint) bp;
                if (lb.getLineNumber() == line) {
                    ReferenceType clazz = lb.getReferenceType();
                    // Use the resolved name, if available.
                    if (clazz != null && clazz.name().equals(cname)) {
                        return bp;
                    }
                    // Use whatever the breakpoint has for a name.
                    if (lb.getClassName().equals(cname)) {
                        return bp;
                    }
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
        return (Breakpoint) breakpointsTable.get(new Integer(n));
    } // getBreakpoint

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
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
        owningSession = session;

        // Recreate the breakpoints from the persistent store.
        logger.info("reading breakpoints from Preferences");
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/breakpoint/defaultGroup");
        defaultGroup = new ManagerGroup(session);
        if (!defaultGroup.readObject(prefs)) {
            // Fall back on this if the deserialization failed.
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING,
                Bundle.getString("BreakManager.deserializeFailed"));
            defaultGroup = new ManagerGroup(session);
        }

        boolean uncaughtExists = false;

        // Iterate the breakpoints and get their 'number' property,
        // and use that value to add the breakpoint to the table.
        Iterator iter = defaultGroup.breakpoints(true);
        synchronized (breakpointsTable) {
            while (iter.hasNext()) {
                Breakpoint bp = (Breakpoint) iter.next();
                int bpi = bp.getNumber();
                breakpointsTable.put(new Integer(bpi), bp);
                if (bpi > lastBreakpointNumber) {
                    lastBreakpointNumber = bpi;
                }
                if (bp instanceof UncaughtExceptionBreakpoint) {
                    uncaughtExists = true;
                }
            }
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info("last breakpoint # = " + lastBreakpointNumber);
        }

        if (!uncaughtExists) {
            // Create a Breakpoint to stop when any uncaught exception is
            // thrown. This breakpoint does not need to be resolved.
            // Create an exception breakpoint that will catch any and all
            // exceptions that are thrown.
            Breakpoint uncaught = new UncaughtExceptionBreakpoint();
            try {
                addNewBreakpoint(uncaught);
            } catch (ResolveException re) {
                // this cannot happen
            }
        }
    } // opened

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
     * @param  spec  breakpoint specification.
     * @return  a new Breakpoint.
     * @throws  ClassNotFoundException
     *          if class name is invalid or missing.
     * @throws  IllegalArgumentException
     *          if specification is missing.
     * @throws  MalformedMemberNameException
     *          if method name is invalid or missing.
     * @throws  ResolveException
     *          if there was a problem resolving the breakpoint.
     */
    public Breakpoint parseBreakpointSpec(String spec)
        throws ClassNotFoundException,
               IllegalArgumentException,
               MalformedMemberNameException,
               ResolveException {

        // Possible breakpoint specifications:
        //  [[<package>.]<class>:]<line>
        //  [[<package>.]<class>.]<method>[(<arg-list>)]

        String cname = null;
        String locspec = null;

        int colonIdx = spec.indexOf(':');
        if (colonIdx > 0) {
            // Definitely a line breakpoint.
            locspec = spec.substring(colonIdx + 1);
            cname = spec.substring(0, colonIdx);
            Integer.parseInt(locspec);
        } else {
            int parenIdx = spec.indexOf('(');
            if (parenIdx < 0) {
                parenIdx = spec.length();
            }
            int lastDotIdx = spec.lastIndexOf('.', parenIdx);
            if (lastDotIdx > 0) {
                locspec = spec.substring(lastDotIdx + 1);
                cname = spec.substring(0, lastDotIdx);
            } else {
                locspec = spec;
            }

            // Validate what we have determined.
            try {
                Integer.parseInt(locspec);
                if (cname != null) {
                    throw new MalformedMemberNameException(locspec);
                }
            } catch (NumberFormatException nfe) {
                String mname;
                parenIdx = locspec.indexOf('(');
                if (parenIdx > 0) {
                    mname = locspec.substring(0, parenIdx);
                } else {
                    mname = locspec;
                }
                if (!Names.isMethodIdentifier(mname)) {
                    throw new MalformedMemberNameException(mname);
                }
            }
        }

        return parseBreakpointSpec(cname, locspec);
    } // parseBreakpointSpec

    /**
     * Evalute the given arguments in order to create a Breakpoint object.
     *
     * @param  cname    name of class in which to set breakpoint.
     * @param  locspec  location specification, either a line number
     *                  of a method specifier.
     * @return  a new Breakpoint.
     * @throws  ClassNotFoundException
     *          if class name is invalid or missing.
     * @throws  IllegalArgumentException
     *          if specification is missing.
     * @throws  MalformedMemberNameException
     *          if method name is invalid or missing.
     * @throws  ResolveException
     *          if there was a problem resolving the breakpoint.
     */
    public Breakpoint parseBreakpointSpec(String cname, String locspec)
        throws ClassNotFoundException,
               IllegalArgumentException,
               MalformedMemberNameException,
               ResolveException {

        if (cname != null) {
            cname = cname.trim();
            if (cname.length() == 0) {
                cname = null;
            }
        }
        locspec = locspec.trim();

        if (cname == null) {
            // Get the current location's class name.
            ContextManager contextManager = (ContextManager)
                owningSession.getManager(ContextManager.class);
            Location loc = contextManager.getCurrentLocation();
            if (loc != null) {
                ReferenceType type = loc.declaringType();
                if (type != null) {
                    cname = type.name();
                }
            }
            // Test again since type may have been null.
            if (cname == null) {
                throw new IllegalArgumentException(
                    Bundle.getString("missingClassName"));
            }
        } else if (cname.indexOf('.') == -1) {
            Preferences node = Preferences.userRoot().node(
                "com/bluemarsh/jswat/breakpoint");
            if (node.getBoolean("addStarDot", Defaults.ADD_STAR_DOT)) {
                // Prepend the implicit "*." to the class name.
                cname = "*." + cname;
            }
        }

        // See if it's a line breakpoint.
        try {
            int line = Integer.parseInt(locspec);
            if (logger.isLoggable(Level.INFO)) {
                logger.info("creating breakpoint: " + cname + ':' + line);
            }
            Breakpoint bp = new LineBreakpoint(cname, line);
            addNewBreakpoint(bp);
            return bp;
        } catch (NumberFormatException nfe) {
            // Ignore it, it was not a line breakpoint.
        }

        // Must be a method breakpoint.
        int firstp = locspec.indexOf('(');
        int lastp = locspec.lastIndexOf(')');
        String method;
        List argList = null;
        if (firstp < 0 && lastp < 0) {
            method = locspec;
        } else if (firstp < 0 || lastp < 0) {
            throw new IllegalArgumentException(
                Bundle.getString("missingMethodArgs"));
        } else {
            method = locspec.substring(0, firstp);
            String args = locspec.substring(firstp + 1, lastp);
            argList = Strings.stringToList(args);
        }
        if (method.length() == 0) {
            throw new IllegalArgumentException(
                Bundle.getString("missingMethodName"));
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.info("creating breakpoint: " + cname + '.' + method);
        }
        Breakpoint bp = new MethodBreakpoint(cname, method, argList);
        addNewBreakpoint(bp);
        return bp;
    } // parseBreakpointSpec

    /**
     * Remove a break listener from the listener list.
     *
     * @param  listener  listener to remove from notification list
     */
    public void removeBreakListener(BreakpointListener listener) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("removing breakpoint listener: "
                        + Names.justTheName(
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
        if (logger.isLoggable(Level.INFO)) {
            logger.info("removing breakpoint: " + bp);
        }
        // Notify the listeners before taking action.
        fireChange(bp, BreakpointEvent.TYPE_REMOVED);
        BreakpointGroup parent = bp.getBreakpointGroup();
        // Remove the breakpoint from its group.
        parent.removeBreakpoint(bp);
        // Remove the breakpoint from our number table.
        synchronized (breakpointsTable) {
            breakpointsTable.remove(new Integer(bp.getNumber()));
            if (breakpointsTable.size() == 0) {
                // Reset the last breakpoint number value.
                lastBreakpointNumber = 0;
            }
        }
        bp.setEnabled(false);
        bp.destroy();
    } // removeBreakpoint

    /**
     * Removes the given breakpoint group from this breakpoint manager.
     * This results in all of the child groups and breakpoints contained
     * therein to be removed as well. Fires breakpoint removed events
     * for all affected breakpoints.
     *
     * @param  group  breakpoint group to remove.
     */
    public void removeBreakpointGroup(BreakpointGroup group) {
        removeBreakpointGroup(group, true);
    } // removeBreakpointGroup

    /**
     * Removes the given breakpoint group from this breakpoint manager.
     * This results in all of the child groups and breakpoints contained
     * therein to be removed as well. Fires breakpoint removed events
     * for all affected breakpoints.
     *
     * @param  group       breakpoint group to remove.
     * @param  notDefault  true to disallow removing default group.
     */
    public void removeBreakpointGroup(BreakpointGroup group,
                                      boolean notDefault) {
        if (notDefault && group == defaultGroup) {
            throw new IllegalArgumentException(
                Bundle.getString("noRemoveDefault"));
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.info("removing breakpoint group: " + group);
        }

        // First deal with this group's subgroups.
        Iterator iter = group.groups(false);
        while (iter.hasNext()) {
            // Recurse into the subgroups, removing them in turn.
            BreakpointGroup subgroup = (BreakpointGroup) iter.next();
            removeBreakpointGroup(subgroup, notDefault);
        }

        // Now remove this group's breakpoints.
        iter = group.breakpoints(false);
        List tempList = new ArrayList();
        while (iter.hasNext()) {
            // Have to use a temporary list to avoid concurrent modification.
            tempList.add(iter.next());
        }
        iter = tempList.iterator();
        while (iter.hasNext()) {
            Breakpoint bp = (Breakpoint) iter.next();
            // Notify the listeners before taking action.
            fireChange(bp, BreakpointEvent.TYPE_REMOVED);
            // Remove the breakpoint properly.
            removeBreakpoint(bp);
        }

        // Remove the breakpoint group from its parent.
        BreakpointGroup parent = group.getParent();
        // Default has no parent, in which case we just leave it be.
        if (parent != null) {
            parent.removeBreakpointGroup(group);
        }
        fireGroupChange(group, GroupEvent.TYPE_REMOVED);
    } // removeBreakpointGroup

    /**
     * Remove a breakpoint group listener from the listener list.
     *
     * @param  listener  listener to remove from notification list
     */
    public void removeGroupListener(GroupListener listener) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("removing group listener: "
                        + Names.justTheName(
                            listener.getClass().getName()));
        }
        listeners.remove(GroupListener.class, listener);
    } // removeGroupListener

    /**
     * Try to resolve the given breakpoint.
     *
     * @param  bp  breakpoint to be resolved.
     * @throws  ResolveException
     *          if error resolving breakpoint.
     */
    public void resolveBreakpoint(ResolvableBreakpoint bp)
        throws ResolveException {

        VirtualMachine vm = owningSession.getVM();
        if (vm != null && !bp.isResolved() && bp.isEnabled()) {
            bp.resolveEagerly(vm);
            if (bp.isResolved()) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("resolved " + bp);
                }
                fireChange(bp, BreakpointEvent.TYPE_MODIFIED);
            }
        }
    } // resolveBreakpoint

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended
} // BreakpointManager
