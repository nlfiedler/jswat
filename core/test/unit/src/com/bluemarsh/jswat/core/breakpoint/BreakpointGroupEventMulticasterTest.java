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
 * are Copyright (C) 2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.core.breakpoint;

import java.beans.PropertyChangeEvent;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the BreakpointGroupEventMulticaster class.
 *
 * @author Nathan Fiedler
 */
public class BreakpointGroupEventMulticasterTest {

    @Test
    public void testMulticaster() {
        BreakpointGroupEventMulticaster bgem = new BreakpointGroupEventMulticaster();
        Assert.assertNotNull(bgem);

        // nothing should happen
        bgem.add(null);
        bgem.remove(null);

        TestListener l1 = new TestListener();
        bgem.add(l1);

        Assert.assertEquals(0, l1.added);
        Assert.assertEquals(0, l1.removed);

        TestListener l2 = new TestListener();
        bgem.add(l2);
        bgem.groupAdded(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(0, l1.removed);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(0, l2.removed);

        bgem.remove(l2);
        bgem.groupRemoved(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(1, l1.removed);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(0, l2.removed);

        bgem.add(l2);
        bgem.errorOccurred(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(1, l1.removed);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(0, l2.removed);
        Assert.assertEquals(1, l1.error);
        Assert.assertEquals(1, l2.error);

        bgem.groupRemoved(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(2, l1.removed);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(1, l2.removed);
    }

    private static class TestListener implements BreakpointGroupListener {

        public int added;
        public int removed;
        public int error;
        public int property;

        @Override
        public void groupAdded(BreakpointGroupEvent e) {
            added++;
        }

        @Override
        public void groupRemoved(BreakpointGroupEvent e) {
            removed++;
        }

        @Override
        public void errorOccurred(BreakpointGroupEvent e) {
            error++;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            property++;
        }
    }
}
