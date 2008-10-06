/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: ResolvableBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.util.Names;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.prefs.Preferences;

/**
 * Class ResolvableBreakpoint represents a breakpoint that requires
 * resolution against a class in the debuggee VM. Such breakpoints
 * include the location breakpoint, exception breakpoint, and watchpoint
 * breakpoint.
 *
 * @author  Nathan Fiedler
 */
public abstract class ResolvableBreakpoint extends AbstractBreakpoint {
    /** Specification for the class this breakpoint is meant for. */
    protected ReferenceSpec referenceSpec;
    /** List of ClassPrepareRequest used to resolve the breakpoint. */
    protected transient List prepareRequests;
    /** Resolved event request if any, or null if not yet resolved. */
    protected transient EventRequest eventRequest;

    /**
     * Default constructor for deserialization.
     */
    ResolvableBreakpoint() {
        prepareRequests = new ArrayList();
    } // ResolvableBreakpoint

    /**
     * Constructs a ResolvableBreakpoint using the given class
     * identifier.
     *
     * @param  classId  class name pattern with optional wildcards.
     * @throws  ClassNotFoundException
     *          if classId is not a valid identifier.
     */
    ResolvableBreakpoint(String classId) throws ClassNotFoundException {
        this();
        referenceSpec = createReferenceSpec(classId);
    } // ResolvableBreakpoint

    /**
     * Adds a class prepare request to by managed by this breakpoint.
     *
     * @param  req  class prepare request to associate with this breakpoint.
     */
    protected void addPrepareRequest(ClassPrepareRequest req) {
        prepareRequests.add(req);
        // Save reference so we can determine ownership later.
        req.putProperty("breakpoint", this);
        req.enable();
    }

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

        return new ReferenceSpec(classId);
    } // createReferenceSpec

    /**
     * Delete the event request.
     */
    protected void deleteEventRequest() {
        if (eventRequest != null) {
            try {
                VirtualMachine vm = eventRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(eventRequest);
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("deleted event request for " + this);
                }
            } catch (VMDisconnectedException vmde) {
                // This happens all the time.
            }
            eventRequest = null;
        }
    } // deleteEventRequest

    /**
     * Delete the class prepare request so we can resolve all over.
     */
    protected void deletePrepareRequests() {
        // Don't want to know about the class prepare events.
        if (!prepareRequests.isEmpty()) {
            Iterator iter = prepareRequests.iterator();
            try {
                while (iter.hasNext()) {
                    EventRequest er = (EventRequest) iter.next();
                    VirtualMachine vm = er.virtualMachine();
                    EventRequestManager erm = vm.eventRequestManager();
                    erm.deleteEventRequest(er);
                }
            } catch (VMDisconnectedException vmde) {
                // this will happen all the time
            }
            prepareRequests.clear();
        }
    } // deletePrepareRequests

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        deleteEventRequest();
        deletePrepareRequests();
        super.destroy();
    } // destroy

    /**
     * Returns the event request for this breakpoint, if the breakpoint
     * has been resolved. If this value is non-null, the caller can be
     * certain the breakpoint is resolved.
     *
     * @return  breakpoint's event request.
     * @see #isResolved
     */
    public EventRequest eventRequest() {
        return eventRequest;
    } // eventRequest

    /**
     * Returns true if the breakpoint has been resolved against the
     * intended object in the debuggee VM. How a breakpoint resolves
     * itself depends on the type of the breakpoint.
     *
     * @return  true if this breakpoint has resolved, false otherwise.
     */
    public boolean isResolved() {
        return eventRequest() != null;
    } // isResolved

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        String classId = prefs.get("referenceSpec", null);
        if (classId == null) {
            return false;
        }
        try {
            referenceSpec = createReferenceSpec(classId);
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
        return super.readObject(prefs);
    } // readObject

    /**
     * Reset the stopped count to zero and clear any other attributes
     * such that this breakpoint can be used again. This does not change
     * the enabled-ness of the breakpoint.
     */
    public void reset() {
        // Make this breakpoint unresolved so it will be resolved again.
        deleteEventRequest();
        deletePrepareRequests();
        // Now do the usual reset thing, including notifying listeners.
        super.reset();
    } // reset

    /**
     * Try to resolve this breakpoint against the class prepare event.
     *
     * @param  event  class prepare event
     * @return  true if resolved, false otherwise.
     * @throws  ResolveException
     *          if the resolution fails in a bad way.
     */
    public boolean resolveAgainstEvent(ClassPrepareEvent event)
        throws ResolveException {
        ReferenceType clazz = event.referenceType();
        EventRequest er = null;
        if (referenceSpec.matches(clazz)) {
            // It looks like a match, let's try to resolve the request.
            er = resolveReference(clazz);
            if (er != null) {
                // Remove the previous event request, if any, but only
                // if we successfully created a new breakpoint request.
                deleteEventRequest();
                eventRequest = er;
                return true;
            }
        }
        return false;
    } // resolveAgainstEvent

    /**
     * Try to resolve this event request eagerly. That is, try to find a
     * matching prepared class now. If one is not found, create a class
     * prepare request so we can resolve when the class is loaded.
     *
     * @param  vm  VirtualMachine
     * @throws  ResolveException
     *          if the resolution fails in a bad way.
     */
    public void resolveEagerly(VirtualMachine vm)
        throws ResolveException {

        // Clear out the old prepare requests to make room for the new ones.
        deletePrepareRequests();
        // Create class prepare requests for this spec.
        referenceSpec.createPrepareRequest(this, vm);

        // Get the appropriate list of classes.
        List classes = null;
        if (referenceSpec.isExact()) {
            classes = vm.classesByName(referenceSpec.getIdentifier());
        } else {
            vm.suspend();
            classes = vm.allClasses();
            vm.resume();
        }

        // We handle exceptions specially because a class may be loaded
        // by more than one class loader, and one of those instances may
        // have the location requested, while the others may not. If
        // none of the classes resolve successfully, then we throw the
        // exception.
        ResolveException originalExc = null;
        EventRequest er = null;

        // Run through the list of classes trying to find a match.
        Iterator iter = classes.iterator();
        while (iter.hasNext()) {
            ReferenceType clazz = (ReferenceType) iter.next();
            try {
                // NJPL support requires matching against the class
                // itself, not the name of the class.
                if (clazz.isPrepared() && referenceSpec.matches(clazz)) {
                    er = resolveReference(clazz);
                    // Keep going through the list so we get all of the
                    // matching classes, in the event that a single
                    // class has been loaded by multiple classloaders.
                }

            } catch (ResolveException re) {
                // Hmm, let's see if we should keep looking for another
                // instance of the class with this location before
                // throwing this exception.
                Throwable t = re.getCause();
                if (t instanceof LineNotFoundException) {
                    if (originalExc == null) {
                        originalExc = re;
                    }
                } else if (t instanceof NoSuchMethodException) {
                    if (originalExc == null) {
                        originalExc = re;
                    }
                } else {
                    throw re;
                }
            }
        }

        if (er == null && originalExc != null) {
            // With an exception and no successful resolution...
            throw originalExc;
        }
        eventRequest = er;
    } // resolveEagerly

    /**
     * Resolve against the given ReferenceType. If successful, return the
     * new event request.
     *
     * @param  refType  ReferenceType against which to resolve.
     * @return  event request, or null if not resolved.
     * @throws  ResolveException
     *          if the resolution fails in a bad way.
     */
    protected abstract EventRequest resolveReference(ReferenceType refType)
        throws ResolveException;

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
        EventRequest er = eventRequest();
        if (er != null) {
            try {
                er.setEnabled(isEnabled());
            } catch (InvalidRequestStateException irse) {
                // This seems to happen when the code has been redefined.
                eventRequest = null;
                prepareRequests.clear();
            } catch (VMDisconnectedException vmde) {
                eventRequest = null;
                prepareRequests.clear();
            }
        }
    } // setEnabled

    /**
     * Set the suspend policy for the request. Use one of the
     * <code>com.sun.jdi.request.EventRequest</code> constants for
     * suspending threads.
     *
     * @param  policy  one of the EventRequest suspend constants.
     */
    public void setSuspendPolicy(int policy) {
        super.setSuspendPolicy(policy);
        // Update the existing event request suspend policy.
        if (eventRequest != null) {
            eventRequest.setSuspendPolicy(suspendPolicy);
        }
    } // setSuspendPolicy

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
        prefs.put("referenceSpec", referenceSpec.getIdentifier());
        return true;
    } // writeObject

    /**
     * Class ReferenceSpec is used for specifying classes. It can accept
     * class name patterns that are either prefixed or suffixed with an
     * asterisk wildcard.
     *
     * @author  Nathan Fiedler
     */
    public class ReferenceSpec {
        /** Name of the class specified, without wildcards. */
        protected String className;
        /** Name of the class specified, possibly with wild cards. */
        protected String classPattern;
        /** True if class specification is suffixed with a wildcard. */
        private boolean postWild;
        /** True if class specification is prefixed with a wildcard. */
        private boolean preWild;

        /**
         * Constructs a new ReferenceSpec for the given class
         * name pattern.
         *
         * @param  classId  class identifier string.
         * @throws  ClassNotFoundException
         *          if classId is not a valid identifier.
         */
        public ReferenceSpec(String classId)
            throws ClassNotFoundException {
            // Save away the original pattern, wildcards and all.
            classPattern = classId;

            // Determine the wildcardedness of the class name pattern.
            // Will either start or end with an asterisk.
            preWild = classId.charAt(0) == '*';
            postWild = classId.endsWith("*");
            if (preWild) {
                className = classId.substring(1);
            } else if (postWild) {
                className = classId.substring(0, classId.length() - 1);
            } else {
                className = classId;
            }

            // Do strict checking of class name validity because if the
            // name is invalid, it will never match a future loaded class.
            StringTokenizer tokenizer = new StringTokenizer(className, ".");
            int numTokens = tokenizer.countTokens();
            int curToken = 0;
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                curToken++;
                if (token.length() == 0) {
                    if (curToken == 1) {
                        // The first element can be empty when wildcarded.
                        continue;
                    } else if (curToken == numTokens) {
                        // The last element can be empty when wildcarded.
                        break;
                    }
                }
                // Each dot-separated piece must be a valid identifier.
                if (!Names.isJavaIdentifier(token)) {
                    throw new ClassNotFoundException(
                        classId + ": invalid part = " + token);
                }
            }
        } // ReferenceSpec

        /**
         * Create class prepare requests appropriate for this reference
         * type specification.
         *
         * @param  bp  breakpoint for which to create prepare requests.
         * @param  vm  VirtualMachine to use for creating requests.
         */
        public void createPrepareRequest(
                ResolvableBreakpoint bp, VirtualMachine vm) {
            String basename = className;
            int idx = basename.indexOf('$');
            if (idx > -1) {
                basename = basename.substring(0, idx);
            }
            // Create patterns for the base class and its inner classes.
            String[] names = {
                basename,
                basename + ".*",
                basename + "$*"
            };
            // Create a prepare request for each pattern.
            for (int ii = 0; ii < names.length; ii++) {
                ClassPrepareRequest request =
                    vm.eventRequestManager().createClassPrepareRequest();
                request.addClassFilter(names[ii]);
                //request.addCountFilter(1);
                bp.addPrepareRequest(request);
            }
        } // createPrepareRequest

        /**
         * Returns the class identifier this specification is specifying.
         * This pattern includes any leading or trailing wildcards.
         *
         * @return  class identifier.
         */
        public String getIdentifier() {
            return classPattern;
        } // getIdentifier

        /**
         * Returns true if this type specification is an exact name.
         *
         * @return  true if exact name, false if wildcard.
         */
        public boolean isExact() {
            return !preWild && !postWild;
        } // isExact

        /**
         * Determines if the given class matches this specification.
         *
         * @param  clazz  class to match against.
         * @return  true if name matches this specification.
         */
        public boolean matches(ReferenceType clazz) {
            // Default implementation.
            return matches(clazz.name());
        } // matches

        /**
         * Determines if the given class name matches this specification.
         *
         * @param  cname  name of class to match against.
         * @return  true if name matches this specification.
         */
        public boolean matches(String cname) {
            if (preWild) {
                return cname.endsWith(className);
            } else if (postWild) {
                return cname.startsWith(className);
            } else {
                return cname.equals(className);
            }
        } // matches

        /**
         * Returns a String representation of this. This returns the class
         * name pattern, including wildcards.
         *
         * @return  String.
         */
        public String toString() {
            return classPattern;
        } // toString
    } // ReferenceSpec
} // ResolvableBreakpoint
