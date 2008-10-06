/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License 
 * Version 1.0 (the "License"); you may not use this file except in 
 * compliance with the License. A copy of the License is available at 
 * http://www.sun.com/
 *
 * The Original Code is JSwat Installer. The Initial Developer of the 
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: JdkVerifier.java 15 2007-06-03 00:01:17Z nfiedler $
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
    /** True if Java 5.0 classes were found. */
    private boolean hasStringBuilder;

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
            while (index < children.length &&
                    (!hasBootstrap || !hasStringBuilder)) {
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
                            entry = zip.getEntry("java/lang/StringBuilder.class");
                            if (entry != null) {
                                hasStringBuilder = true;
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
        return hasStringBuilder;
    }
}
