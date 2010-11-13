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
 * are Copyright (C) 2007-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.runtime;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the RuntimeEventMulticaster class.
 */
public class RuntimeEventMulticasterTest {

    @Test
    public void test_RuntimeEventMulticaster() {
        RuntimeListener sl = RuntimeEventMulticaster.add(null, null);
        assertEquals(sl, null);

        sl = RuntimeEventMulticaster.remove(null, null);
        assertEquals(sl, null);

        TestListener l1 = new TestListener();
        sl = RuntimeEventMulticaster.add(sl, l1);
        TestListener l2 = new TestListener();
        sl = RuntimeEventMulticaster.add(sl, l2);

        assertEquals(0, l1.added);
        assertEquals(0, l1.removed);
        assertEquals(0, l2.added);
        assertEquals(0, l2.removed);

        JavaRuntime runtime = new DummyRuntime();
        RuntimeEvent sevt = new RuntimeEvent(runtime, RuntimeEvent.Type.ADDED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.added);
        assertEquals(0, l1.removed);
        assertEquals(1, l2.added);
        assertEquals(0, l2.removed);

        sevt = new RuntimeEvent(runtime, RuntimeEvent.Type.REMOVED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.added);
        assertEquals(1, l1.removed);
        assertEquals(1, l2.added);
        assertEquals(1, l2.removed);

        sl = RuntimeEventMulticaster.remove(sl, l1);
        sevt = new RuntimeEvent(runtime, RuntimeEvent.Type.ADDED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.added);
        assertEquals(1, l1.removed);
        assertEquals(2, l2.added);
        assertEquals(1, l2.removed);
    }

    private static class TestListener implements RuntimeListener {

        int added;
        int removed;

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
