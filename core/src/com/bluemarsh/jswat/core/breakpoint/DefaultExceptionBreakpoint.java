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
 * are Copyright (C) 2001-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultExceptionBreakpoint.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Class DefaultExceptionBreakpoint is a default implementation of an
 * ExceptionBreakpoint.
 *
 * @author Nathan Fiedler
 */
public class DefaultExceptionBreakpoint extends DefaultResolvableBreakpoint
        implements ExceptionBreakpoint {
    /** True to stop when the exception is caught. */
    private boolean onCaught;
    /** True to stop when the exception is not caught. */
    private boolean onUncaught;

    /**
     * Creates a new instance of ExceptionBreakpoint.
     */
    public DefaultExceptionBreakpoint() {
    }

    public boolean canFilterClass() {
        return false;
    }

    public boolean canFilterThread() {
        return false;
    }

    public void closing(SessionEvent sevt) {
    }

    public String describe(Event e) {
        if (e instanceof ExceptionEvent) {
            return Utilities.describeException((ExceptionEvent) e);
        } else {
            throw new IllegalArgumentException(
                    "expected exception event, but got " +
                    e.getClass().getName());
        }
    }

    public String getDescription() {
        String cname = getClassName();
        String type = "";
        if (onCaught && onUncaught) {
            type = NbBundle.getMessage(DefaultExceptionBreakpoint.class,
                    "Exception.description.both");
        } else if (onCaught) {
            type = NbBundle.getMessage(DefaultExceptionBreakpoint.class,
                    "Exception.description.caught");
        } else if (onUncaught) {
            type = NbBundle.getMessage(DefaultExceptionBreakpoint.class,
                    "Exception.description.uncaught");
        }
        return NbBundle.getMessage(ExceptionBreakpoint.class,
                "Exception.description", cname, type);
    }

    public boolean getStopOnCaught() {
        return onCaught;
    }

    public boolean getStopOnUncaught() {
        return onUncaught;
    }

    public void opened(Session session) {
    }

    protected boolean resolveReference(ReferenceType refType,
            List<EventRequest> requests) throws ResolveException {
        VirtualMachine vm = refType.virtualMachine();
        EventRequestManager erm = vm.eventRequestManager();
        ExceptionRequest er = erm.createExceptionRequest(
            refType, onCaught, onUncaught);
        String filter = getClassFilter();
        if (filter != null) {
            er.addClassFilter(filter);
        }
        register(er);
        requests.add(er);
        return true;
    }

    public void resuming(SessionEvent sevt) {
    }

    public void setEnabled(boolean enabled) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        super.setEnabled(enabled);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    public void setStopOnCaught(boolean stop) {
        boolean old = onCaught;
        onCaught = stop;
        propSupport.firePropertyChange(PROP_STOPONCAUGHT, old, stop);
    }

    public void setStopOnUncaught(boolean stop) {
        boolean old = onUncaught;
        onUncaught = stop;
        propSupport.firePropertyChange(PROP_STOPONUNCAUGHT, old, stop);
    }

    public void suspended(SessionEvent sevt) {
    }
}
