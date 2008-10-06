/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License 
 * Version 1.0 (the "License"); you may not use this file except in 
 * compliance with the License. A copy of the License is available at 
 * http://www.sun.com/
 *
 * The Original Code is JSwat Installer. The Initial Developer of the 
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: JdkVerifierTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.installer;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the JdkVerifier class.
 *
 * @author Nathan Fiedler
 */
public class JdkVerifierTest extends TestCase {

    public JdkVerifierTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(JdkVerifierTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_ScanPath() {
        String home = System.getProperty("java.home");
        if (home.endsWith("jre")) {
            // Trim the "/jre" part from the path.
            home = home.substring(0, home.length() - 4);
        }
        File dir = new File(home);
        boolean found = JdkVerifier.scanPath(dir);
        assertTrue("java.home has no JPDA?", found);
        home = System.getProperty("user.home");
        dir = new File(home);
        found = JdkVerifier.scanPath(dir);
        assertFalse("user.home has JPDA?", found);
    }
}
