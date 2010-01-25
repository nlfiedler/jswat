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

package com.bluemarsh.jswat.core;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for CoreSettings class.
 *
 * @author Nathan Fiedler
 */
public class CoreSettingsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConnectionTimeout() {
        CoreSettings instance = CoreSettings.getDefault();
        instance.setConnectionTimeout(-1);
    }

    @Test
    public void testConnectionTimeout() {
        CoreSettings instance = CoreSettings.getDefault();
        int result = instance.getConnectionTimeout();
        instance.setConnectionTimeout(result);
        assertEquals(result, instance.getConnectionTimeout());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInvocationTimeout() {
        CoreSettings instance = CoreSettings.getDefault();
        instance.setInvocationTimeout(-1);
    }

    @Test
    public void testInvocationTimeout() {
        CoreSettings instance = CoreSettings.getDefault();
        int result = instance.getInvocationTimeout();
        instance.setInvocationTimeout(result);
        assertEquals(result, instance.getInvocationTimeout());
    }

    @Test
    public void testShowAllThreads() {
        CoreSettings instance = CoreSettings.getDefault();
        boolean result = instance.getShowAllThreads();
        instance.setShowAllThreads(result);
        assertEquals(result, instance.getShowAllThreads());
    }

    @Test
    public void testShowHiddenFiles() {
        CoreSettings instance = CoreSettings.getDefault();
        boolean result = instance.getShowHiddenFiles();
        instance.setShowHiddenFiles(result);
        assertEquals(result, instance.getShowHiddenFiles());
    }

    @Test
    public void testSkipSynthetics() {
        CoreSettings instance = CoreSettings.getDefault();
        boolean result = instance.getSkipSynthetics();
        instance.setSkipSynthetics(result);
        assertEquals(result, instance.getSkipSynthetics());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullSourceExtension() {
        CoreSettings instance = CoreSettings.getDefault();
        instance.setSourceExtension(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptySourceExtension() {
        CoreSettings instance = CoreSettings.getDefault();
        instance.setSourceExtension("  ");
    }

    @Test
    public void testGetSourceExtension() {
        CoreSettings instance = CoreSettings.getDefault();
        String actual = instance.getSourceExtension();
        instance.setSourceExtension("foo");
        String result = instance.getSourceExtension();
        assertEquals(".foo", result);
        instance.setSourceExtension(actual);
        assertEquals(actual, instance.getSourceExtension());
    }

    @Test
    public void testGetSteppingExcludes() {
        CoreSettings instance = CoreSettings.getDefault();
        List<String> result = instance.getSteppingExcludes();
        instance.setSteppingExcludes(null);
        List<String> empty = instance.getSteppingExcludes();
        assertTrue(empty.isEmpty());
        instance.setSteppingExcludes(result);
        assertEquals(result, instance.getSteppingExcludes());
    }
}
