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

    @Override
    public JavaRuntime createRuntime(String id) {
        JavaRuntime jr = new DefaultRuntime();
        jr.setIdentifier(id);
        return jr;
    }

    @Override
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
        String[] exec_candidates = new String[]{
            "java", "java.exe", "jvm", "jvm.exe"
        };
        for (String exec : exec_candidates) {
            if (new File(bindir, exec).exists()) {
                rt.setExec(exec);
                break;
            }
        }

        // Find and set the Java core classes source code.
        String[] src_candidates = new String[]{
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

    @Override
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
