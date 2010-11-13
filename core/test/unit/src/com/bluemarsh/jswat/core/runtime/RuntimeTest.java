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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for the Runtime implementation.
 *
 * @author Nathan Fiedler
 */
public class RuntimeTest {

    @Test
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

        // Test cloning with the sources set to null.
        try {
            rt.clone();
            rt.setSources(null);
            rt.clone();
        } catch (Exception e) {
            fail("Failed to clone runtime: " + e.toString());
        }

        // Do not close the runtime manager, otherwise it will save the
        // test runtime properties to file.
    }
}
