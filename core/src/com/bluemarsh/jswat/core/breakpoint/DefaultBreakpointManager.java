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

import com.bluemarsh.jswat.core.PlatformProvider;
import com.bluemarsh.jswat.core.PlatformService;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionListener;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.ErrorManager;

/**
 * DefaultBreakpointManager is responsible for maintaining the breakpoints
 * and breakpoint groups hierarchy, as well as saving all of these objects
 * to a persistent storage medium.
 *
 * @author  Nathan Fiedler
 */
public class DefaultBreakpointManager extends AbstractBreakpointManager {
    /** Suffix for the session file names. */
    private static final String FILENAME_SUFFIX = "-breakpoints.xml";
    /** The default breakpoint group, into which all new groups and
     * breakpoints will go by default. */
    private BreakpointGroup defaultGroup;

    @Override
    public void addBreakpoint(Breakpoint bp) {
        super.addBreakpoint(bp);
        // Add the breakpoint to the default group.
        defaultGroup.addBreakpoint(bp);
        // Make sure the breakpoint is registered appropriately, but only
        // after it has been hooked into the breakpoint tree.
        if (bp instanceof SessionListener) {
            getSession().addSessionListener((SessionListener) bp);
        }
        // Notify everyone that a breakpoint was added.
        fireEvent(bp, BreakpointEvent.Type.ADDED, null);
    }

    @Override
    public void addBreakpointGroup(BreakpointGroup group, BreakpointGroup parent) {
        super.addBreakpointGroup(group, parent);
        if (parent == null) {
            parent = defaultGroup;
        }
        parent.addBreakpointGroup(group);
        fireEvent(new BreakpointGroupEvent(group, BreakpointGroupEvent.Type.ADDED));
    }

    @Override
    protected void deleteBreakpoints(Session session) {
        try {
            PlatformService platform = PlatformProvider.getPlatformService();
            String name = session.getIdentifier() + FILENAME_SUFFIX;
            platform.deleteFile(name);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }

    @Override
    public BreakpointGroup getDefaultGroup() {
        return defaultGroup;
    }

    @Override
    protected void loadBreakpoints(Session session) {
        // Recreate the breakpoints from the persistent store.
        XMLDecoder decoder = null;
        try {
            PlatformService platform = PlatformProvider.getPlatformService();
            String name = session.getIdentifier() + FILENAME_SUFFIX;
            InputStream is = platform.readFile(name);
            decoder = new XMLDecoder(is);
            decoder.setExceptionListener(new ExceptionListener() {
                @Override
                public void exceptionThrown(Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            });
            defaultGroup = (BreakpointGroup) decoder.readObject();
        } catch (FileNotFoundException e) {
            // Do not report this error, it's normal.
        } catch (Exception e) {
            // Parser, I/O, and various runtime exceptions may occur,
            // need to report them and gracefully recover.
            ErrorManager.getDefault().notify(e);
        } finally {
            if (decoder != null) {
                decoder.close();
            }
        }
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        if (defaultGroup == null) {
            defaultGroup = bf.createBreakpointGroup("Default");
        }

        // Make sure our uncaught exceptions breakpoint exists.
        // At the same time, we register each of the breakpoints.
        boolean uncaughtExists = false;
        Iterator<Breakpoint> biter = defaultGroup.breakpoints(true);
        while (biter.hasNext()) {
            Breakpoint bp = biter.next();
            // Need to register the breakpoint upon deserialization.
            if (bp instanceof SessionListener) {
                getSession().addSessionListener((SessionListener) bp);
            }
            // Need to listen for changes in the breakpoint.
            bp.addBreakpointListener(this);
            if (bp instanceof UncaughtExceptionBreakpoint) {
                uncaughtExists = true;
            }
        }
        if (!uncaughtExists) {
            Breakpoint bp = bf.createUncaughtExceptionBreakpoint();
            addBreakpoint(bp);
        }

        // Make sure we are listening to all of the groups.
        Iterator<BreakpointGroup> giter = defaultGroup.groups(true);
        while (giter.hasNext()) {
            giter.next().addBreakpointGroupListener(this);
        }
    }

    @Override
    public void removeBreakpoint(Breakpoint bp) {
        super.removeBreakpoint(bp);
        // Notify the listeners before taking action.
        fireEvent(bp, BreakpointEvent.Type.REMOVED, null);
        if (bp instanceof SessionListener) {
            getSession().removeSessionListener((SessionListener) bp);
        }
        BreakpointGroup parent = bp.getBreakpointGroup();
        // Remove the breakpoint from its group.
        parent.removeBreakpoint(bp);
        // Do nothing else at this point but to destroy it.
        bp.destroy();
    }

    @Override
    public void removeBreakpointGroup(BreakpointGroup group) {
        super.removeBreakpointGroup(group);
        // First deal with this group's subgroups.
        List<BreakpointGroup> groups = new ArrayList<BreakpointGroup>();
        Iterator<BreakpointGroup> giter = group.groups(false);
        // Must copy the groups to a list to avoid concurrent modification.
        while (giter.hasNext()) {
            groups.add(giter.next());
        }
        for (int ii = groups.size() - 1; ii >= 0; ii--) {
            removeBreakpointGroup(groups.get(ii));
        }
        // Now remove the group's breakpoints.
        Iterator<Breakpoint> biter = group.breakpoints(false);
        List<Breakpoint> brks = new ArrayList<Breakpoint>();
        while (biter.hasNext()) {
            brks.add(biter.next());
        }
        for (int ii = brks.size() - 1; ii >= 0; ii--) {
            removeBreakpoint(brks.get(ii));
        }

        // Remove the breakpoint group from its parent.
        BreakpointGroup parent = group.getParent();
        if (parent != null) {
            parent.removeBreakpointGroup(group);
        }
        fireEvent(new BreakpointGroupEvent(group, BreakpointGroupEvent.Type.REMOVED));
    }

    @Override
    protected void saveBreakpoints(Session session) {
        // Persist the breakpoints to a file.
        String name = session.getIdentifier() + FILENAME_SUFFIX;
        PlatformService platform = PlatformProvider.getPlatformService();
        try {
            OutputStream os = platform.writeFile(name);
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(defaultGroup);
            encoder.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        } finally {
            platform.releaseLock(name);
        }
    }
}
