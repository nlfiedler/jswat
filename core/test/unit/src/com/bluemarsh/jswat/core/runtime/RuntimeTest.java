/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: RuntimeTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.runtime;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RuntimeTest extends TestCase {

    public RuntimeTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(RuntimeTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Runtime() {
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        RuntimeFactory rf = RuntimeProvider.getRuntimeFactory();
        String id = rm.generateIdentifier();
        JavaRuntime rt = rf.createRuntime(System.getProperty("java.home"), id);
        // The createRuntime() should have been able to set up all of the
        // attributes of the default runtime.
        assertNotNull(rt);
        assertNotNull(rt.getIdentifier());
        assertNotNull(rt.getBase());
        assertNotNull(rt.getExec());
        assertNotNull(rt.getName());
        assertNotNull(rt.getSources());

        // Do not close the runtime manager, otherwise it will save the
        // test runtime properties to file.
    }
}
