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
 * The Original Software is JSwat Installer. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.installer;

import java.io.File;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit test for the JdkVerifier class.
 *
 * @author Nathan Fiedler
 */
public class JdkVerifierTest {

    @Test
    public void test_ScanPath() {
        String home = System.getProperty("java.home");
        if (home.endsWith("jre")) {
            // Trim the "/jre" part from the path.
            home = home.substring(0, home.length() - 4);
        }
        File dir = new File(home);
        JdkVerifier jv = new JdkVerifier();
        jv.scanPath(dir);
        assertTrue(jv.hasDebugInterface());
        assertTrue(jv.sufficientVersion());
        // Do not use user.home as that tends to be huge compared
        // to the current directory (this project directory).
        home = System.getProperty("user.dir");
        dir = new File(home);
        jv = new JdkVerifier();
        jv.scanPath(dir);
        assertFalse(jv.hasDebugInterface());
        assertFalse(jv.sufficientVersion());
    }
}
