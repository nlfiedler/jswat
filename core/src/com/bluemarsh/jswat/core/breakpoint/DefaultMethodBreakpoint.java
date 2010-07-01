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
 * are Copyright (C) 2001-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.util.AmbiguousMethodException;
import com.bluemarsh.jswat.core.util.Classes;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Strings;
import com.bluemarsh.jswat.core.util.Threads;
import com.bluemarsh.jswat.core.util.Types;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.openide.util.NbBundle;

/**
 * Class DefaultMethodBreakpoint is a default implementation of a
 * MethodBreakpoint.
 *
 * @author Nathan Fiedler
 */
public class DefaultMethodBreakpoint extends DefaultResolvableBreakpoint
        implements MethodBreakpoint {
    /** Name of the method this breakpoint is set at (may be empty string). */
    protected String methodId;
    /** List of method parameters, where each element is represents the
     * parameter type (may be empty list). */
    protected List<String> methodParameters;

    /**
     * Creates a new instance of DefaultMethodBreakpoint.
     */
    public DefaultMethodBreakpoint() {
    }

    @Override
    public boolean canFilterClass() {
        return false;
    }

    @Override
    public boolean canFilterThread() {
        return false;
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    public String describe(Event e) {
        // Use the exact location information, since the description of this
        // breakpoint may be vague, and useless when events occur repeatedly.
        LocatableEvent event = (LocatableEvent) e;
        String cname = event.location().declaringType().name();
        String mname = event.location().method().name();
        List<String> arglist = event.location().method().argumentTypeNames();
        String args = Strings.listToString(arglist, ",");
        String tname = Threads.getIdentifier(event.thread());
        String[] params = new String[] { cname, mname, args, tname };
        return NbBundle.getMessage(DefaultMethodBreakpoint.class,
                "Method.description.stop", params);
    }

    @Override
    public String getDescription() {
        String cname = getClassName();
        String mname;
        if (methodId == null || methodId.length() == 0) {
            mname = "*";
        } else {
            mname = methodId;
        }
        String args;
        if (methodParameters == null || methodParameters.size() == 0) {
            args = "*";
        } else {
            args = Strings.listToString(methodParameters, ",");
        }
        return NbBundle.getMessage(DefaultMethodBreakpoint.class,
                "Method.description", cname, mname, args);
    }

    @Override
    public List<String> getMethodParameters() {
        return methodParameters;
    }

    @Override
    public String getMethodName() {
        return methodId;
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    protected boolean resolveReference(ReferenceType refType,
            List<EventRequest> requests) throws ResolveException {

        // Determine the set of locations to which we match.
        List<Location> locations = new LinkedList<Location>();
        if (methodId.length() == 0) {
            // No method name at all, resolve against all methods.
            List<Method> methods = refType.methods();
            for (Method method : methods) {
                locations.add(method.location());
            }
        } else if (methodParameters.size() == 0) {
            // Resolve against all methods of a certain name.
            List<Method> methods = refType.methodsByName(methodId);
            for (Method method : methods) {
                locations.add(method.location());
            }
        } else {
            // Resolve against a specific method with parameters.
            try {
                List<String> parameterTypes = Types.typeNamesToJNI(
                        methodParameters);
                // Replace unresolved typenames with '*'.
                ListIterator<String> liter = parameterTypes.listIterator();
                while (liter.hasNext()) {
                    String type = liter.next();
                    if (type == null) {
                        liter.set("*");
                    } else if (type.equals("L*;")) {
                        liter.set("*");
                    }
                }
                Method method = Classes.findMethod(
                    refType, methodId, parameterTypes, false);
                locations.add(method.location());
            } catch (AmbiguousMethodException ame) {
                throw new ResolveException(ame);
            } catch (InvalidTypeException ite) {
                throw new ResolveException(
                    new InvalidParameterTypeException(ite.getMessage()));
            } catch (NoSuchMethodException nsme) {
                throw new ResolveException(nsme);
            }
        }

        // Resolve against all of the discovered locations, if any.
        for (Location location : locations) {
            // Null method locations denote abstract methods.
            if (location != null) {
                VirtualMachine vm = location.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                BreakpointRequest er = erm.createBreakpointRequest(location);
                register(er);
                requests.add(er);
            }
        }
        return locations.size() > 0;
    }

    @Override
    public void resuming(SessionEvent sevt) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        super.setEnabled(enabled);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    @Override
    public void setMethodName(String name) throws MalformedMemberNameException {
        if (name == null) {
            throw new IllegalArgumentException("name must be non-null");
        }
        // Allow empty method names, but non-empty must be valid.
        if (name.length() > 0 && !Names.isMethodIdentifier(name)) {
            throw new MalformedMemberNameException(name);
        }
        String old = methodId;
        methodId = name;
        // Reset ourselves so we get resolved all over again.
        deleteRequests();
        propSupport.firePropertyChange(PROP_METHODNAME, old, name);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    @Override
    public void setMethodParameters(List<String> args) {
        if (args == null) {
            throw new NullPointerException("args cannot be null");
        }
        List old = methodParameters;
        methodParameters = args;
        // Reset ourselves so we get resolved all over again.
        deleteRequests();
        propSupport.firePropertyChange(PROP_METHODPARAMETERS, old, args);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    @Override
    public void suspended(SessionEvent sevt) {
    }
}
