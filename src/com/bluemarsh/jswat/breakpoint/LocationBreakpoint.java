/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: LocationBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.util.Threads;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Class LocationBreakpoint is the base class for all breakpoints that are
 * based on a particular location in code. This includes line breakpoints
 * and method breakpoints.
 *
 * @author Nathan Fiedler
 */
public abstract class LocationBreakpoint extends ResolvableBreakpoint
    implements LocatableBreakpoint {
    /** The reference type we resolved against. */
    protected ReferenceType referenceType;
    /** Line number of breakpoint. */
    protected int lineNumber;

    /**
     * Default constructor for deserialization.
     */
    LocationBreakpoint() {
    }

    /**
     * Constructs a LocationBreakpoint using the given class identifier.
     *
     * @param  classId  class name pattern with optional wildcards.
     * @throws  ClassNotFoundException
     *          if classId is not a valid identifier.
     */
    LocationBreakpoint(String classId) throws ClassNotFoundException {
        super(classId);
    }

    /**
     * Create the breakpoint event request against the given location.
     *
     * @param  location  location at which to stop.
     * @return  event request.
     */
    protected EventRequest createEventRequest(Location location) {
        VirtualMachine vm = location.virtualMachine();
        EventRequestManager erm = vm.eventRequestManager();
        BreakpointRequest er = erm.createBreakpointRequest(location);
        // Save a reference to ourselves in case we need it.
        er.putProperty("breakpoint", this);
        er.setSuspendPolicy(getSuspendPolicy());

        // Apply thread filters.
        Session session = getBreakpointGroup().getSession();
        String filtersStr = getThreadFilters();
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

        // Have to enable the request to work.
        er.setEnabled(isEnabled());
        // Set the reference type to the resolved class.
        referenceType = location.declaringType();
        return er;
    }

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        super.destroy();
        Session session = getBreakpointGroup().getSession();
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.removeListener(com.sun.jdi.event.BreakpointEvent.class, this);
    }

    /**
     * Return the name of the class that this breakpoint is located in.
     * This could be a fully-qualified class name or a wild-carded name
     * pattern containing a single asterisk (e.g. "*.cname").
     *
     * @return  Class name if known, null if not.
     */
    public String getClassName() {
        return referenceSpec != null ? referenceSpec.getIdentifier() : null;
    }

    /**
     * Retrieve the line number associated with this breakpoint. Not
     * all location breakpoints will have a particular line associated
     * with them (such as method breakpoints). In such cases, this
     * method may return -1.
     *
     * @return  line number of breakpoint, or -1 if unknown.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the name of the package for the class this breakpoint is
     * set within. May not be known, in which case null is returned.
     *
     * @return  package name, or null if unknown.
     */
    public String getPackageName() {
        return null;
    }

    /**
     * Locatable breakpoints are expected to be set in classes, hence
     * they should have a <code>ReferenceType</code>. However, the
     * caller should check for a return value of null.
     *
     * @return  reference type, or null if not set.
     */
    public ReferenceType getReferenceType() {
        return referenceType;
    }

    /**
     * Returns the name of the source file for the class this breakpoint
     * is set within. May not be known, in which case null is returned.
     *
     * @return  source name, or null if unknown.
     */
    public String getSourceName() {
        return null;
    }

    /**
     * Initialize the breakpoint so it may operate normally.
     */
    public void init() {
        // We need to listen for breakpoint events.
        Session session = getBreakpointGroup().getSession();
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.addListener(com.sun.jdi.event.BreakpointEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
    }

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
    }

    /**
     * Compares the name of the package and the name of the source file
     * with those given as arguments. Null source names do not
     * constitute a match, but null package names are allowed.
     *
     * @param  pkg  name of package (with dot separators).
     * @param  src  name of source file.
     * @return  true if matches this breakpoint's information.
     */
    public boolean matchesSource(String pkg, String src) {
        String ourPkg = getPackageName();
        String ourSrc = getSourceName();
        // True if both package names are null, or are both non-null and
        // equal, and the source names are non-null and equal.
        boolean match = (pkg == ourPkg
                || (pkg != null && ourPkg != null && ourPkg.equals(pkg)))
                && src != null && ourSrc != null && ourSrc.equals(src);
        if (!match) {
            ReferenceType clazz = getReferenceType();
            if (clazz != null) {
                try {
                    VirtualMachine vm = clazz.virtualMachine();
                    if (vm.canGetSourceDebugExtension()) {
                        // Use ReferenceType.sourcePaths() for matching.
                        StringBuffer path = new StringBuffer();
                        if (pkg != null) {
                            path.append(pkg.replace('.', File.separatorChar));
                            // Append a separator regardless of whether src is non-null,
                            // because without a source it wouldn't match anyway.
                            path.append(File.separatorChar);
                        }
                        if (src != null) {
                            path.append(src);
                        }
                        List paths = clazz.sourcePaths(null);
                        return paths.contains(path.toString());
                    }
                } catch (AbsentInformationException aie) {
                    // ignore and fall through
                } catch (VMDisconnectedException vmde) {
                    // ignore and fall through
                }
            }
        }
        return match;
    }

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        lineNumber = prefs.getInt("lineNumber", -1);
        return super.readObject(prefs);
    }

    /**
     * Determine the location at which to set the breakpoint using
     * the given class type.
     *
     * @param  clazz  ClassType against which to resolve.
     * @return  Location at which to create breakpoint.
     * @throws  ResolveException
     *          if resolution failed in a bad way.
     */
    protected abstract Location resolveLocation(ClassType clazz)
        throws ResolveException;

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

        // Check that the reference type is a class.
        if (!(refType instanceof ClassType)) {
            throw new ResolveException(new InvalidTypeException());
        }
        Location location = resolveLocation((ClassType) refType);
        return createEventRequest(location);
    }

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
        // Delete and recreate the event request using the current
        // breakpoint settings.
        deleteEventRequest();
        deletePrepareRequests();
        if (enabled) {
            // Do a complete re-resolve so we account for hotswapped
            // classes, which need to be found again.
            Session session = getBreakpointGroup().getSession();
            VirtualMachine vm = session.getVM();
            if (vm != null) {
                try {
                    resolveEagerly(vm);
                } catch (ResolveException re) {
                    session.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING,
                        "LocationBreakpoint.setEnabled() failed: " + re);
                }
            }
        }
        // Do this last so the listeners get the notification after we have
        // completed our work (otherwise they indicate the wrong state,
        // like the source view gutter colorizer).
        super.setEnabled(enabled);
    }

    /**
     * Returns a String representation of this.
     *
     * @return  string representing this.
     */
    public String toString() {
        return toString(false);
    }

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
        prefs.putInt("lineNumber", lineNumber);
        return true;
    }
}
