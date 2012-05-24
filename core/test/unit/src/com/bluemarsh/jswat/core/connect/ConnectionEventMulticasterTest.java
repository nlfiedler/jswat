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
package com.bluemarsh.jswat.core.connect;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the ConnectionEventMulticaster class.
 *
 * @author Nathan Fiedler
 */
public class ConnectionEventMulticasterTest {

    @Test
    public void testMulticaster() {
        ConnectionEventMulticaster cem = new ConnectionEventMulticaster();
        Assert.assertNotNull(cem);

        // nothing should happen
        cem.add(null);
        cem.remove(null);

        TestListener l1 = new TestListener();
        cem.add(l1);

        Assert.assertEquals(0, l1.connected);

        TestListener l2 = new TestListener();
        cem.add(l2);
        cem.connected(null);
        Assert.assertEquals(1, l1.connected);
        Assert.assertEquals(1, l2.connected);

        cem.remove(l2);
        cem.connected(null);
        Assert.assertEquals(2, l1.connected);
        Assert.assertEquals(1, l2.connected);

        cem.add(l2);
        cem.connected(null);
        Assert.assertEquals(3, l1.connected);
        Assert.assertEquals(2, l2.connected);
    }

    private static class TestListener implements ConnectionListener {

        public int connected;

        @Override
        public void connected(ConnectionEvent e) {
            connected++;
        }
    }
}
