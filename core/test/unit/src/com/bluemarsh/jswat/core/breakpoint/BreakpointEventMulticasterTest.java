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
 * Tests the BreakpointEventMulticaster class.
 *
 * @author Nathan Fiedler
 */
public class BreakpointEventMulticasterTest {

    @Test
    public void testMulticaster() {
        BreakpointEventMulticaster bem = new BreakpointEventMulticaster();
        Assert.assertNotNull(bem);

        // nothing should happen
        bem.add(null);
        bem.remove(null);

        TestListener l1 = new TestListener();
        bem.add(l1);

        Assert.assertEquals(0, l1.added);
        Assert.assertEquals(0, l1.removed);
        Assert.assertEquals(0, l1.stopped);

        TestListener l2 = new TestListener();
        bem.add(l2);
        bem.breakpointAdded(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(0, l1.removed);
        Assert.assertEquals(0, l1.stopped);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(0, l2.removed);
        Assert.assertEquals(0, l2.stopped);

        bem.remove(l2);
        bem.breakpointRemoved(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(1, l1.removed);
        Assert.assertEquals(0, l1.stopped);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(0, l2.removed);
        Assert.assertEquals(0, l2.stopped);

        bem.add(l2);
        bem.breakpointStopped(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(1, l1.removed);
        Assert.assertEquals(1, l1.stopped);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(0, l2.removed);
        Assert.assertEquals(1, l2.stopped);

        bem.breakpointRemoved(null);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(2, l1.removed);
        Assert.assertEquals(1, l1.stopped);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(1, l2.removed);
        Assert.assertEquals(1, l2.stopped);
    }

    private static class TestListener implements BreakpointListener {

        public int added;
        public int removed;
        public int stopped;
        public int error;
        public int property;

        @Override
        public void breakpointAdded(BreakpointEvent e) {
            added++;
        }

        @Override
        public void breakpointRemoved(BreakpointEvent e) {
            removed++;
        }

        @Override
        public void breakpointStopped(BreakpointEvent e) {
            stopped++;
        }

        @Override
        public void errorOccurred(BreakpointEvent e) {
            error++;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            property++;
        }
    }
}
