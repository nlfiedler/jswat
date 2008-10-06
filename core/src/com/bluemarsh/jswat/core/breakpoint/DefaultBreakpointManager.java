/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultBreakpointManager.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.event.Dispatcher;
import com.bluemarsh.jswat.core.event.DispatcherProvider;
import com.bluemarsh.jswat.core.event.DispatcherListener;
import com.bluemarsh.jswat.core.session.SessionListener;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

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

    public void addBreakpoint(Breakpoint bp) {
        super.addBreakpoint(bp);
        // Add the breakpoint to the default group.
        defaultGroup.addBreakpoint(bp);
        // Make sure the breakpoint is registered appropriately, but only
        // after it has been hooked into the breakpoint tree.
        if (bp instanceof SessionListener) {
            getSession().addSessionListener((SessionListener) bp);
        }
        if (bp instanceof DispatcherListener) {
            Dispatcher d = DispatcherProvider.getDispatcher(getSession());
            d.addListener((DispatcherListener) bp);
        }
        // Notify everyone that a breakpoint was added.
        fireEvent(bp, BreakpointEvent.Type.ADDED, null);
    }

    public void addBreakpointGroup(BreakpointGroup group, BreakpointGroup parent) {
        super.addBreakpointGroup(group, parent);
        if (parent == null) {
            parent = defaultGroup;
        }
        parent.addBreakpointGroup(group);
        fireEvent(new BreakpointGroupEvent(group, BreakpointGroupEvent.Type.ADDED));
    }

    protected void deleteBreakpoints(Session session) {
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = session.getIdentifier() + FILENAME_SUFFIX;
            FileObject fo = fs.findResource(name);
            if (fo != null && fo.isData()) {
                fo.delete();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }

    public BreakpointGroup getDefaultGroup() {
        return defaultGroup;
    }

    protected void loadBreakpoints(Session session) {
        // Recreate the breakpoints from the persistent store.
        XMLDecoder decoder = null;
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = session.getIdentifier() + FILENAME_SUFFIX;
            FileObject fo = fs.findResource(name);
            if (fo != null && fo.isData()) {
                InputStream is = fo.getInputStream();
                decoder = new XMLDecoder(is);
                decoder.setExceptionListener(new ExceptionListener() {
                    public void exceptionThrown(Exception e) {
                        ErrorManager.getDefault().notify(e);
                    }
                });
                defaultGroup = (BreakpointGroup) decoder.readObject();
            }
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
            if (bp instanceof DispatcherListener) {
                Dispatcher d = DispatcherProvider.getDispatcher(getSession());
                d.addListener((DispatcherListener) bp);
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

    public void removeBreakpoint(Breakpoint bp) {
        super.removeBreakpoint(bp);
        // Notify the listeners before taking action.
        fireEvent(bp, BreakpointEvent.Type.REMOVED, null);
        if (bp instanceof DispatcherListener) {
            Dispatcher d = DispatcherProvider.getDispatcher(getSession());
            d.removeListener((DispatcherListener) bp);
        }
        if (bp instanceof SessionListener) {
            getSession().removeSessionListener((SessionListener) bp);
        }
        BreakpointGroup parent = bp.getBreakpointGroup();
        // Remove the breakpoint from its group.
        parent.removeBreakpoint(bp);
        // Do nothing else at this point but to destroy it.
        bp.destroy();
    }

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

    protected void saveBreakpoints(Session session) {
        // Persist the breakpoints to a file.
        FileLock lock = null;
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = session.getIdentifier() + FILENAME_SUFFIX;
            FileObject fo = fs.findResource(name);
            if (fo == null) {
                fo = fs.getRoot().createData(name);
            }
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(defaultGroup);
            encoder.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        } finally {
            if (lock != null) lock.releaseLock();
        }
    }
}
