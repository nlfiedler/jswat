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
 * $Id: DefaultRuntimeFactory.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.runtime;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Class DefaultRuntimeFactory implements RuntimeFactory to construct
 * default implementations of JavaRuntime. It is familiar with the
 * structure of the most popular Java Runtime Environments, and can
 * automatically detect the location of certain files and directories.
 *
 * @author Nathan Fiedler
 */
public class DefaultRuntimeFactory implements RuntimeFactory {

    public JavaRuntime createRuntime(String id) {
        JavaRuntime jr = new DefaultRuntime();
        jr.setIdentifier(id);
        return jr;
    }

    public JavaRuntime createRuntime(String base, String id) {
        if (base == null || base.length() == 0) {
            throw new IllegalArgumentException("base is required");
        }
        File basedir = new File(base);
        if (!basedir.exists()) {
            throw new IllegalArgumentException("base must exist");
        }
        if (basedir.getName().equals("jre")) {
            // Strip off that tricky jre directory.
            basedir = basedir.getParentFile();
        }
        JavaRuntime rt = createRuntime(id);
        // Set the base directory.
        rt.setBase(basedir.getAbsolutePath());

        // Find and set the Java executable.
        File bindir = new File(basedir, "bin");
        String[] exec_candidates = new String[] {
            "java", "java.exe", "jvm", "jvm.exe"
        };
        for (String exec : exec_candidates) {
            if (new File(bindir, exec).exists()) {
                rt.setExec(exec);
                break;
            }
        }

        // Find and set the Java core classes source code.
        String[] src_candidates = new String[] {
            "src", "src.zip", "src.jar"
        };
        List<String> srcs = new LinkedList<String>();
        for (String src : src_candidates) {
            File srcdir = new File(basedir, src);
            if (srcdir.exists()) {
                srcs.add(srcdir.getAbsolutePath());
            }
        }
        rt.setSources(srcs);

        return rt;
    }

    public String getDefaultBase() {
        String home = System.getProperty("java.home");
        File base = new File(home);
        if (base.getName().equals("jre")) {
            // Strip off that tricky jre directory.
            base = base.getParentFile();
            home = base.getAbsolutePath();
        }
        return home;
    }
}
