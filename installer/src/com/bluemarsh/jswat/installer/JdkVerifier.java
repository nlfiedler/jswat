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
 * The Original Software is JSwat Installer. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.installer;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class JdkVerifier scans a given directory looking for archives that
 * contain the JDI classes.
 *
 * @author Nathan Fiedler
 */
public class JdkVerifier {
    /** True if JDI classes were found. */
    private boolean hasBootstrap;
    /** True if Java 6.0 classes were found. */
    private boolean hasServiceLoader;

    /**
     * Creates a new instance of JdkVerifier.
     */
    public JdkVerifier() {
    }

    /**
     * Indicates if the selected JDK installation has the JDI classes.
     *
     * @return  true if JDI was found, false if not.
     */
    public boolean hasDebugInterface() {
        return hasBootstrap;
    }

    /**
     * Scans the given directory, verifying that this represents a valid
     * version of the JDK for JSwat to use.
     *
     * @param  dir  supposed JDK installation directory.
     */
    public void scanPath(File dir) {
        File[] children = dir.listFiles();
        if (children != null) {
            int index = 0;
            while (index < children.length && (!hasBootstrap || !hasServiceLoader)) {
                File child = children[index];
                if (child.isDirectory()) {
                    scanPath(child);
                } else {
                    String name = child.getName().toLowerCase();
                    if (name.endsWith(".jar") || name.endsWith(".zip")) {
                        try {
                            ZipFile zip = new ZipFile(child);
                            ZipEntry entry = zip.getEntry("com/sun/jdi/Bootstrap.class");
                            if (entry != null) {
                                hasBootstrap = true;
                            }
                            entry = zip.getEntry("java/util/ServiceLoader.class");
                            if (entry != null) {
                                hasServiceLoader = true;
                            }
                            zip.close();
                        } catch (IOException ioe) {
                            // ignore this archive and carry on
                        }
                    }
                }
                index++;
            }
        }
    }

    /**
     * Indicates if the selected JDK version is sufficient for JSwat.
     *
     * @return  true if Java version okay, false if not.
     */
    public boolean sufficientVersion() {
        return hasServiceLoader;
    }
}
