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
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Class DefaultLineBreakpoint is a default implementation of a LineBreakpoint.
 *
 * @author Nathan Fiedler
 */
public class DefaultLineBreakpoint extends DefaultResolvableBreakpoint
        implements LineBreakpoint {
    /** The URL of the file in which we are set. */
    private String url;
    /** Name of package containg the class this breakpoint is set in. */
    private String packageName;
    /** Name of the source file this breakpoint is set in. */
    private String sourceName;
    /** The line that we are set to stop upon. */
    private int lineNumber;

    /**
     * Creates a new instance of DefaultLineBreakpoint.
     */
    public DefaultLineBreakpoint() {
    }

    @Override
    public boolean canFilterClass() {
        return false;
    }

    @Override
    public boolean canFilterThread() {
        return true;
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    protected ClassPrepareRequest createPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest request = super.createPrepareRequest(vm);
        // If source name filtering is available, use it to make the
        // event request more effective and reduce the number of events
        // the JVM sends over the wire.
        if (vm.canUseSourceNameFilters() && sourceName != null) {
            // Must disable the request before modifying it.
            boolean enabled = request.isEnabled();
            request.setEnabled(false);
            request.addSourceNameFilter(sourceName);
            request.setEnabled(enabled);
        }
        return request;
    }

    @Override
    public String describe(Event e) {
        String name = sourceName;
        // The event is expected to be locatable, but in the event that
        // it is not, have a placeholder for the thread identifier.
        String thread = "???";
        if (e instanceof LocatableEvent) {
            LocatableEvent le = (LocatableEvent) e;
            try {
                // Get a more qualified value than just the source name.
                name = le.location().sourcePath(null);
                thread = Threads.getIdentifier(le.thread());
            } catch (AbsentInformationException aie) {
                // Ignore and use sourceName as-is.
            }
        }
        return NbBundle.getMessage(DefaultLineBreakpoint.class,
                "Line.description.stop", name, lineNumber, thread);
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(DefaultLineBreakpoint.class,
                "Line.description", sourceName, lineNumber);
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    protected boolean matches(ReferenceType clazz) {
        boolean found = false;
        try {
            List<String> paths = clazz.sourcePaths(null);
            for (String path : paths) {
                // For Windows, must replace \ with / to match.
                path = path.replace("\\", "/");
                // Check that the path is non-empty before comparing.
                // Note that for classes in the default package, this
                // will match anything with the same file name, no matter
                // what package it might be from.
                if (path.length() > 0 && url.endsWith(path)) {
                    found = true;
                    break;
                }
            }
        } catch (AbsentInformationException aie) {
            // Many classes are compiled without debugging information.
            return false;
        } catch (ObjectCollectedException oce) {
            // Although unlikely, the class may have been collected.
            return false;
        }

        // If not a match via source name, then try matching against the
        // package name, if our breakpoint was given a package name at all.
        // This helps to resolve when source lives in a directory structure
        // that does not correspond to the package name.
        if (!found && packageName != null && packageName.length() > 0) {
            String pname = Names.getPackageName(clazz.name());
            found = packageName.equals(pname);
        }
        return found;
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    protected boolean resolveReference(ReferenceType clazz,
            List<EventRequest> requests) throws ResolveException {
        List locs = null;
        try {
            locs = clazz.locationsOfLine(null, sourceName, lineNumber);
            if (locs.isEmpty()) {
                List inners = clazz.nestedTypes();
                Iterator iter = inners.iterator();
                while (iter.hasNext() && locs.isEmpty()) {
                    ReferenceType type = (ReferenceType) iter.next();
                    locs = type.locationsOfLine(null, sourceName, lineNumber);
                }
            }
        } catch (AbsentInformationException aie) {
            // If we got here, that means the matches() method thought this
            // class was a good candidate, but apparently not.
            throw new ResolveException(clazz.name(), aie);
        } catch (ClassNotPreparedException cnpe) {
            // Oh well, we'll catch it when it is loaded.
            return false;
        }
        if (locs.size() > 0) {
            // We assume the first location for this line is good enough.
            Location location = (Location) locs.get(0);
            VirtualMachine vm = location.virtualMachine();
            EventRequestManager erm = vm.eventRequestManager();
            BreakpointRequest er = erm.createBreakpointRequest(location);
            register(er);
            requests.add(er);
            return true;
        } else {
            // Indicate that the location did not match.
            return false;
        }
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
    public void setLineNumber(int line) {
        if (line < 1) {
            throw new IllegalArgumentException("line cannot be < 1");
        }
        int old = lineNumber;
        lineNumber = line;
        // Reset ourselves so we get resolved all over again.
        deleteRequests();
        propSupport.firePropertyChange(PROP_LINENUMBER, old, line);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    @Override
    public void setPackageName(String pkg) {
        String old = packageName;
        packageName = pkg;
        propSupport.firePropertyChange(PROP_PACKAGENAME, old, pkg);
    }

    @Override
    public void setSourceName(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name cannot be null/empty");
        }
        String old = sourceName;
        sourceName = name;
        propSupport.firePropertyChange(PROP_SOURCENAME, old, name);
    }

    @Override
    public void setURL(String url) {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("url cannot be null/empty");
        }
        String old = this.url;
        this.url = url;
        propSupport.firePropertyChange(PROP_URL, old, url);
    }

    @Override
    public void suspended(SessionEvent sevt) {
    }
}
