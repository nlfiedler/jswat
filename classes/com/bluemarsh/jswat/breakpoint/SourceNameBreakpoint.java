/*********************************************************************
 *
 *      Copyright (C) 2003 Daniel Bonniot
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
 * $Id: SourceNameBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.SourceNameBreakpointUI;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Class SourceNameBreakpoint is a specialized LineBreakpoint that
 * contains the source file name and package name of the class in which
 * the breakpoint is set. This is created by the source views which may
 * not have the exact class name information.
 *
 * @author Daniel Bonniot <bonniot@users.sf.net>
 * @author Nathan Fiedler
 */
public class SourceNameBreakpoint extends LineBreakpoint {
    /** Package the breakpoint belongs to. */
    private String packageName;

    /**
     * Default constructor for deserialization.
     */
    SourceNameBreakpoint() {
    } // SourceNameBreakpoint

    /**
     * Constructs a SourceNameBreakpoint for the given source file and
     * package at the specified line within that source file.
     *
     * @param  pkgname  the package the breakpoint belongs to.
     * @param  srcname  the source file the breakpoint belongs to.
     * @param  line     line at which to stop (1-based).
     * @throws  ClassNotFoundException
     *          if classPattern is not a valid identifier.
     */
    public SourceNameBreakpoint(String pkgname, String srcname, int line)
        throws ClassNotFoundException {

        super(pkgname == null || pkgname.length() == 0
                ? "*" : pkgname + ".*", srcname, line);
        this.packageName = pkgname;
    } // SourceNameBreakpoint

    /**
     * Constructs a ReferenceSpec of the appropriate type.
     *
     * @param  classId  class identifier.
     * @return  new ReferenceSpec instance.
     * @throws  ClassNotFoundException
     *          if class name is not valid.
     */
    protected ReferenceSpec createReferenceSpec(String classId)
        throws ClassNotFoundException {

        return new SnReferenceSpec(classId);
    } // createReferenceSpec

    /**
     * Returns the name of the package for the class this breakpoint is
     * set within. May not be known, in which case null is returned.
     *
     * @return  package name, or null if unknown.
     */
    public String getPackageName() {
        return packageName;
    } // getPackageName

    /**
     * Returns the user interface widget for customizing this breakpoint.
     * This method returns a new ui adapter each time it is called.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return new SourceNameBreakpointUI(this);
    } // getUIAdapter

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        packageName = prefs.get("packageName", null);
        boolean val = super.readObject(prefs);
        if (getSourceName() == null) {
            return false;
        }
        return val;
    } // readObject

    /**
     * Determine the location at which to set the breakpoint using the
     * given class type.
     *
     * @param  clazz  ClassType against which to resolve.
     * @return  Location at which to create breakpoint, or null.
     * @throws  ResolveException
     *          if resolution failed in a bad way.
     */
    protected Location resolveLocation(ClassType clazz)
        throws ResolveException {
        List locs = null;
        try {
            String src = getSourceName();
            locs = clazz.locationsOfLine(null, src, lineNumber);
            if (locs.size() == 0) {
                // Try without any source name at all.
                locs = clazz.locationsOfLine(null, null, lineNumber);
            }
        } catch (AbsentInformationException aie) {
            throw new ResolveException("No data for " + clazz + "/"
                                       + getSourceName(), aie);
        }
        if (locs.size() > 0) {
            // We assume the first location for this line is good enough.
            return (Location) locs.get(0);
        } else {
            // Since multiple classes may be defined in a single file,
            // and since we do not know which class contains the line
            // of code in question, we cannot treat this condition as
            // an error, but just hope that eventually we resolve.
            return null;
        }
    } // resolveLocation

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

        // We have to override the method so we do not generate
        // errors for the unusual cases.
        if (refType instanceof ClassType) {
            Location location = resolveLocation((ClassType) refType);
            if (location != null) {
                return createEventRequest(location);
            }
        }
        return null;
    } // resolveReference

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     * @return  String representation of this.
     */
    public String toString(boolean terse) {
        StringBuffer buf = new StringBuffer(80);
        if (packageName != null && packageName.length() > 0) {
            buf.append(packageName);
            buf.append(' ');
        }
        buf.append(getSourceName());
        buf.append(':');
        buf.append(lineNumber);
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
        if (packageName != null) {
            prefs.put("packageName", packageName);
        }
        return true;
    } // writeObject

    /**
     * Class SnReferenceSpec is used for specifying classes. This
     * subclass matches classes in the right package, that contains the
     * source file this breakpoint belongs to.
     *
     * @author  Daniel Bonniot
     */
    class SnReferenceSpec extends ResolvableBreakpoint.ReferenceSpec {

        /**
         * Constructs a SnReferenceSpec for the given class name
         * pattern.
         *
         * @param  classId  class identifier string.
         * @throws  ClassNotFoundException
         *          if classId is not a valid identifier.
         */
        public SnReferenceSpec(String classId) throws ClassNotFoundException {
            super(classId);
        } // SnReferenceSpec

        /**
         * Create class prepare requests appropriate for this reference
         * type specification.
         *
         * @param  bp  breakpoint for which to create prepare requests.
         * @param  vm  VirtualMachine to use for creating requests.
         */
        public void createPrepareRequest(
                ResolvableBreakpoint bp, VirtualMachine vm) {
            // Create a prepare request for each pattern.
            ClassPrepareRequest request =
                vm.eventRequestManager().createClassPrepareRequest();
            if (!classPattern.equals("*")) {
                request.addClassFilter(classPattern);
            }
            // No count filter -- we must check every class in the package.
            bp.addPrepareRequest(request);
        } // createPrepareRequest

        /**
         * Determines if the given class matches this specification.
         *
         * @param  clazz  class to match against.
         * @return  true if name matches this specification.
         */
        public boolean matches(ReferenceType clazz) {
            try {
                String src = getSourceName();
                List names = clazz.sourceNames(null);
                return names.contains(src);
            } catch (AbsentInformationException aie) {
                return false;
            }
        } // matches

        /**
         * Determines if the given class name matches this specification.
         *
         * @param  classrcname  name of class to match against.
         * @return  true if name matches this specification.
         */
        public boolean matches(String classrcname) {
            // There is no way to know if the right source is concerned.
            return false;
        } // matches
    } // SnReferenceSpec
} // SourceNameBreakpoint
