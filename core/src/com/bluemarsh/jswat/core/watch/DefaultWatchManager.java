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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.watch;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 * The default implementation of the WatchManager interface.
 *
 * @author Nathan Fiedler
 */
public class DefaultWatchManager extends AbstractWatchManager {
    /** List of all defined watches. */
    private List<Watch> watchList;

    /**
     * Creates a new instance of DefaultWatchManager.
     */
    public DefaultWatchManager() {
        watchList = new LinkedList<Watch>();
    }

    public void addWatch(Watch watch) {
        watchList.add(watch);
        fireEvent(new WatchEvent(watch, WatchEvent.Type.ADDED));
    }

    public void disconnected(SessionEvent sevt) {
        super.disconnected(sevt);
        removeFixedWatches();
    }

    @SuppressWarnings("unchecked")
    protected void loadWatches(Session session) {
        XMLDecoder decoder = null;
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = "watches.xml";
            FileObject fo = fs.findResource(name);
            if (fo != null && fo.isData()) {
                InputStream is = fo.getInputStream();
                decoder = new XMLDecoder(is);
                decoder.setExceptionListener(new ExceptionListener() {
                    public void exceptionThrown(Exception e) {
                        ErrorManager.getDefault().notify(e);
                    }
                });
                watchList = (List<Watch>) decoder.readObject();
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
    }

    /**
     * Removes all of the FixedWatch instances in the list.
     */
    private void removeFixedWatches() {
        // Remove the fixed watches, since they cannot be retrieved
        // once we are disconnected from the debuggee.
        Iterator<Watch> iter = watchIterator();
        while (iter.hasNext()) {
            Watch w = iter.next();
            if (w instanceof FixedWatch) {
                iter.remove();
            }
        }
    }

    public void removeWatch(Watch watch) {
        watchList.remove(watch);
        fireEvent(new WatchEvent(watch, WatchEvent.Type.REMOVED));
    }

    protected void saveWatches(Session session) {
        // The fixed watches cannot be persisted.
        removeFixedWatches();
        // Save the remaining watches to storage.
        FileLock lock = null;
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = "watches.xml";
            FileObject fo = fs.findResource(name);
            if (fo == null) {
                fo = fs.getRoot().createData(name);
            }
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(watchList);
            encoder.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        } finally {
            if (lock != null) lock.releaseLock();
        }
    }

    public Iterator<Watch> watchIterator() {
        return watchList.iterator();
    }
}
