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
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.prefs.Preferences;
import org.openide.util.Cancellable;

/**
 * Defines the API for interacting with the underlying platform, which
 * provides services needed by the core classes.
 *
 * @author  Nathan Fiedler
 */
public interface PlatformService {

    /**
     * Deletes the named file (with optional leading path).
     *
     * @param  name  name of the file, with optional partial path.
     * @throws IOException  if an error occurs.
     */
    void deleteFile(String name) throws IOException;

    /**
     * Retrieve an instance of Preferences for a particular class.
     * For the NetBeans Platform, this will be an NbPreferences object.
     *
     * @param  clazz  class for which to get Preferences instance.
     * @return  the Preferences instance.
     */
    Preferences getPreferences(Class clazz);

    /**
     * Retrieve the source file name from the class file.
     *
     * @param  clazz  input stream representing class file.
     * @param  name   name of the class to be read.
     * @return  the source name from the class file, or null if missing.
     * @throws  IOException  if reading stream fails.
     */
    String getSourceName(InputStream clazz, String name) throws IOException;

    /**
     * Opens an input stream for the named file (with optional leading path).
     * <p>The caller is responsible for closing the stream when finished.</p>
     *
     * @param  name  name of the file, with optional partial path.
     * @return  input stream.
     * @throws  FileNotFoundException  if the file does not exist.
     */
    InputStream readFile(String name) throws FileNotFoundException;

    /**
     * Release the exclusive file lock for the named file. This must be
     * called after invoking the writeFile(String) method.
     *
     * @param  name  name of the file, with optional partial path.
     */
    void releaseLock(String name);

    /**
     * Displays a progress indicator to show that a task is running in
     * the background.
     *
     * @param  label     displayed with the progress indicator.
     * @param  callback  called if the user cancels the task.
     * @return  a handle for the progress indicator.
     */
    Object startProgress(String label, Cancellable callback);

    /**
     * Terminate the progress indicator for the given progress handle.
     *
     * @param  handle  progress handle.
     */
    void stopProgress(Object handle);

    /**
     * Opens an output stream for the named file (with optional leading path).
     * If the file already exists, it will be overwritten.
     * <p>The caller is required to invoke releaseLock(String) when finished
     * writing to the file.</p>
     * <p>The caller is responsible for closing the stream when finished.</p>
     *
     * @param  name  name of the file, with optional partial path.
     * @return  output stream.
     * @throws  IOException  if the write operation fails.
     */
    OutputStream writeFile(String name) throws IOException;
}
