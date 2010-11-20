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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.runtime;

import com.bluemarsh.jswat.core.PlatformProvider;
import com.bluemarsh.jswat.core.PlatformService;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DefaultRuntimeManager manages JavaRuntime instances that are persisted
 * to properties files stored in the userdir.
 *
 * @author Nathan Fiedler
 */
public class DefaultRuntimeManager extends AbstractRuntimeManager {

    /** Logger for gracefully reporting unexpected errors. */
    private static final Logger logger = Logger.getLogger(
            DefaultRuntimeManager.class.getName());
    /** List of the open runtimes. */
    private List<JavaRuntime> openRuntimes;

    /**
     * Creates a new instance of RuntimeManager.
     */
    public DefaultRuntimeManager() {
        super();
        openRuntimes = new LinkedList<JavaRuntime>();
    }

    @Override
    public synchronized void add(JavaRuntime runtime) {
        openRuntimes.add(runtime);
        fireEvent(new RuntimeEvent(runtime, RuntimeEventType.ADDED));
    }

    @Override
    public Iterator<JavaRuntime> iterateRuntimes() {
        // Make sure the caller cannot modify the list.
        List<JavaRuntime> ro = Collections.unmodifiableList(openRuntimes);
        return ro.iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadRuntimes(RuntimeFactory factory) {
        XMLDecoder decoder = null;
        try {
            PlatformService platform = PlatformProvider.getPlatformService();
            String name = "runtimes.xml";
            InputStream is = platform.readFile(name);
            decoder = new XMLDecoder(is);
            decoder.setExceptionListener(new ExceptionListener() {

                @Override
                public void exceptionThrown(Exception e) {
                    logger.log(Level.SEVERE, null, e);
                }
            });
            openRuntimes = (List<JavaRuntime>) decoder.readObject();
            // Leave possibly invalid runtimes in the last, let user
            // decide what to do with them.
        } catch (FileNotFoundException e) {
            // Ignore this error, it's normal.
        } catch (Exception e) {
            // Parser, I/O, and various runtime exceptions may occur,
            // need to report them and gracefully recover.
            logger.log(Level.SEVERE, null, e);
        } finally {
            if (decoder != null) {
                decoder.close();
            }
        }
    }

    @Override
    public synchronized void remove(JavaRuntime runtime) {
        openRuntimes.remove(runtime);
        fireEvent(new RuntimeEvent(runtime, RuntimeEventType.REMOVED));
    }

    @Override
    public synchronized void saveRuntimes() {
        PlatformService platform = PlatformProvider.getPlatformService();
        String name = "runtimes.xml";
        try {
            OutputStream os = platform.writeFile(name);
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(openRuntimes);
            encoder.close();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
        } finally {
            platform.releaseLock(name);
        }
    }
}
