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
package com.bluemarsh.jswat.command;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the RepeaterInputProcessor class.
 *
 * @author Nathan Fiedler
 */
public class RepeaterInputProcessorTest {
    
    @Test
    public void testCanProcessNull() {
        RepeaterInputProcessor instance = new RepeaterInputProcessor();
        boolean result = instance.canProcess(null, null);
        Assert.assertFalse(result);
    }
    
    @Test
    public void testCanProcessEmpty() {
        RepeaterInputProcessor instance = new RepeaterInputProcessor();
        boolean result = instance.canProcess("", null);
        Assert.assertFalse(result);
    }

    @Test
    public void testCanProcessNegative() {
        RepeaterInputProcessor instance = new RepeaterInputProcessor();
        boolean result = instance.canProcess("foobar", null);
        Assert.assertFalse(result);
    }

    @Test
    public void testCanProcessPositive() {
        RepeaterInputProcessor instance = new RepeaterInputProcessor();
        boolean result = instance.canProcess("10 foobar", null);
        Assert.assertTrue(result);
    }

    @Test
    public void testExpandsInput() {
        RepeaterInputProcessor instance = new RepeaterInputProcessor();
        boolean result = instance.expandsInput();
        Assert.assertFalse(result);
    }

    @Test
    public void testProcessNull() throws Exception {
        RepeaterInputProcessor instance = new RepeaterInputProcessor();
        List<String> result = instance.process(null, null);
        Assert.assertNull(result);
    }

    @Test
    public void testProcessEmpty() throws Exception {
        RepeaterInputProcessor instance = new RepeaterInputProcessor();
        List<String> result = instance.process("", null);
        Assert.assertNull(result);
    }

    @Test
    public void testProcessNegative() throws Exception {
        RepeaterInputProcessor instance = new RepeaterInputProcessor();
        List<String> result = instance.process("foobar", null);
        Assert.assertNull(result);
    }

    @Test
    public void testProcessPositive() throws Exception {
        RepeaterInputProcessor instance = new RepeaterInputProcessor();
        List<String> result = instance.process("3 foobar", null);
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
        for (String s : result) {
            Assert.assertEquals("foobar", s);
        }
    }
}
