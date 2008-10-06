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
 * $Id: RuntimeManagerTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.runtime;

import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RuntimeManagerTest extends TestCase {

    public RuntimeManagerTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(RuntimeManagerTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_RuntimeManager() {
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        RuntimeFactory rf = RuntimeProvider.getRuntimeFactory();
        String id = rm.generateIdentifier();
        JavaRuntime rt = rf.createRuntime(System.getProperty("java.home"), id);
        assertNotNull(rt);
        rm.add(rt);
        // Make sure the runtime appears in the set.
        Iterator<JavaRuntime> iter = rm.iterateRuntimes();
        assertTrue(iter.hasNext());
        boolean found = false;
        while (iter.hasNext()) {
            JavaRuntime rti = iter.next();
            if (rti.equals(rt)) {
                found = true;
                break;
            }
        }
        assertTrue("did not find added runtime", found);

        // Remove the runtime
        rm.remove(rt);
        // Make sure the runtime does not appear in the set.
        iter = rm.iterateRuntimes();
        assertTrue(iter.hasNext());
        found = false;
        while (iter.hasNext()) {
            JavaRuntime rti = iter.next();
            if (rti.equals(rt)) {
                found = true;
                break;
            }
        }
        assertTrue("found added runtime after removal", !found);

        // Do not close the runtime manager, otherwise it will save the
        // test runtime properties to file.
    }
}
