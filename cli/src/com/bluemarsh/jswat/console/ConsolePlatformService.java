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
 * are Copyright (C) 2009-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.PlatformService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.prefs.Preferences;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.openide.util.Cancellable;

/**
 * Console implementation of the platform service which works independently
 * of any supporting framework or platform.
 *
 * @author  Nathan Fiedler
 */
public class ConsolePlatformService implements PlatformService {

    /** Name of the user directory where files are stored by default. */
    private static final String USER_DIR = ".jswat-console";
    /** The path to the user's home directory. */
    private File userDirectory;

    /**
     * Creates a new instance of ConsolePlatformService.
     */
    public ConsolePlatformService() {
        userDirectory = new File(System.getProperty("user.home"), USER_DIR);
        // Ensure the user directory exists to avoid exceptions in logging.
        if (!userDirectory.exists()) {
            userDirectory.mkdirs();
        }
    }

    @Override
    public void deleteFile(String name) throws IOException {
        File file = new File(userDirectory, name);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public String getSourceName(InputStream clazz, String name) throws IOException {
        ClassParser parser = new ClassParser(clazz, name);
        try {
            JavaClass jc = parser.parse();
            return jc.getSourceFileName();
        } catch (ClassFormatException cfe) {
            throw new IOException(cfe.toString());
        }
    }

    @Override
    public Preferences getPreferences(Class<?> clazz) {
        return Preferences.userNodeForPackage(clazz);
    }

    @Override
    public InputStream readFile(String name) throws FileNotFoundException {
        File file = new File(userDirectory, name);
        return new FileInputStream(file);
    }

    @Override
    public void releaseLock(String name) {
        // Nothing to do.
    }

    @Override
    public Object startProgress(String label, Cancellable callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopProgress(Object handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream writeFile(String name) throws IOException {
        File file = new File(userDirectory, name);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        return new FileOutputStream(file);
    }
}
