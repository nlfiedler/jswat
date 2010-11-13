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

import java.util.Iterator;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for the RuntimeManager class.
 *
 * @author Nathan Fiedler
 */
public class RuntimeManagerTest {

    @Test
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

    public void test_RuntimeManager_addRemoveListener() {
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        TestListener tl = new TestListener();
        rm.addRuntimeListener(tl);
        JavaRuntime runtime1 = new DummyRuntime();
        rm.add(runtime1);
        JavaRuntime runtime2 = new DummyRuntime();
        rm.add(runtime2);
        rm.remove(runtime1);
        assertTrue("missed add event", tl.added == 2);
        assertTrue("missed remove event", tl.removed == 1);
        rm.removeRuntimeListener(tl);
    }

    private static class TestListener implements RuntimeListener {

        public int added;
        public int removed;

        @Override
        public void runtimeAdded(RuntimeEvent event) {
            added++;
        }

        @Override
        public void runtimeRemoved(RuntimeEvent event) {
            removed++;
        }
    }
}
