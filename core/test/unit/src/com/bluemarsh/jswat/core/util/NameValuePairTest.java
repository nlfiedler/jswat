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
 * are Copyright (C) 2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the NameValuePair class.
 *
 * @author Nathan Fiedler
 */
public class NameValuePairTest {

    @Test
    public void testEquals() {
        NameValuePair<Integer> a1 = new NameValuePair<Integer>("a", 123);
        assertFalse(a1.equals("foo"));
        NameValuePair<Integer> a2 = new NameValuePair<Integer>("a", 123);
        assertTrue(a1.equals(a2));
        NameValuePair<Integer> a3 = new NameValuePair<Integer>("a", 321);
        assertFalse(a1.equals(a3));
        NameValuePair<Integer> b = new NameValuePair<Integer>("b", 123);
        assertTrue(a1.equals(b));
        NameValuePair<Integer> n1 = new NameValuePair<Integer>("a", null);
        NameValuePair<Integer> n2 = new NameValuePair<Integer>("a", null);
        assertTrue(n1.equals(n2));
        assertFalse(n1.equals(a1));
        assertFalse(a1.equals(n1));
    }

    @Test
    public void testGetName() {
        String expected = "a";
        NameValuePair<Integer> instance = new NameValuePair<Integer>(expected, 123);
        String actual = instance.getName();
        assertEquals(expected, actual);
    }

    @Test
    public void testGetValue() {
        Integer expected = 123;
        NameValuePair<Integer> instance = new NameValuePair<Integer>("a", expected);
        Integer actual = instance.getValue();
        assertEquals(expected, actual);
    }

    @Test
    public void testHashCode() {
        Integer v = 123;
        NameValuePair<Integer> instance = new NameValuePair<Integer>("a", v);
        int result = instance.hashCode();
        assertEquals(v.hashCode(), result);
        instance = new NameValuePair<Integer>("a", null);
        result = instance.hashCode();
        assertEquals(-1, result);
    }

    @Test
    public void testToString() {
        String expected = "a";
        NameValuePair<Integer> instance = new NameValuePair<Integer>(expected, 123);
        String actual = instance.toString();
        assertEquals(expected, actual);
    }
}
