/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultRuntimeManager.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.runtime;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 * DefaultRuntimeManager manages JavaRuntime instances that are persisted
 * to properties files stored in the userdir.
 *
 * @author Nathan Fiedler
 */
public class DefaultRuntimeManager extends AbstractRuntimeManager {
    /** List of the open runtimes. */
    private List<JavaRuntime> openRuntimes;

    /**
     * Creates a new instance of RuntimeManager.
     */
    public DefaultRuntimeManager() {
        super();
        openRuntimes = new LinkedList<JavaRuntime>();
    }

    public synchronized void add(JavaRuntime runtime) {
        openRuntimes.add(runtime);
    }

    public Iterator<JavaRuntime> iterateRuntimes() {
        // Make sure the caller cannot modify the list.
        List<JavaRuntime> ro = Collections.unmodifiableList(openRuntimes);
        return ro.iterator();
    }

    @SuppressWarnings("unchecked")
    public void loadRuntimes(RuntimeFactory factory) {
        XMLDecoder decoder = null;
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = "runtimes.xml";
            FileObject fo = fs.findResource(name);
            if (fo != null && fo.isData()) {
                InputStream is = fo.getInputStream();
                decoder = new XMLDecoder(is);
                decoder.setExceptionListener(new ExceptionListener() {
                    public void exceptionThrown(Exception e) {
                        ErrorManager.getDefault().notify(e);
                    }
                });
                // Validate the runtimes to ensure they still exist.
                openRuntimes = (List<JavaRuntime>) decoder.readObject();
                ListIterator<JavaRuntime> liter = openRuntimes.listIterator();
                while (liter.hasNext()) {
                    JavaRuntime jr = liter.next();
                    if (!jr.isValid()) {
                        liter.remove();
                    }
                }
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

    public synchronized void remove(JavaRuntime runtime) {
        openRuntimes.remove(runtime);
    }

    public synchronized void saveRuntimes() {
        FileLock lock = null;
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = "runtimes.xml";
            FileObject fo = fs.findResource(name);
            if (fo == null) {
                fo = fs.getRoot().createData(name);
            }
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(openRuntimes);
            encoder.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        } finally {
            if (lock != null) lock.releaseLock();
        }
    }
}
