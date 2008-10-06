/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: PathManagerTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.path;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the PathManager class.
 */
public class PathManagerTest extends TestCase {

    public PathManagerTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(PathManagerTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_PathManger() {
//        Session session = SessionHelper.getSession();
//        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
//
//        //
//        // Test finding source for a non-public class.
//        //
//        String[] classes = new String[] {
//            "PathManagerTestCode",
//            "PathManagerTestCode$Inner",
//            "PathManagerTestCode$1",
//            "PMSecond"
//        };
//        String[] methods = new String[] {
//            "method1",
//            "method_I",
//            "run",
//            "method_PMS"
//        };
//        List<String> empty = Collections.emptyList();
//        try {
//            for (int ii = 0; ii < methods.length; ii++) {
//                Breakpoint bp = bf.createMethodBreakpoint(
//                        classes[ii], methods[ii], empty);
//                BreakpointHelper.prepareBreakpoint(bp, session);
//            }
//        } catch (MalformedMemberNameException mmne) {
//            fail(mmne.toString());
//        } catch (MalformedClassNameException mcne) {
//            fail(mcne.toString());
//        }
//
//        SessionHelper.launchDebuggee(session, "PathManagerTestCode");
//
//        PathManager pm = PathProvider.getPathManager(session);
//        String spath = SessionHelper.getTestSourcepath();
//        File file = FileUtil.normalizeFile(new File(spath));
//        // XXX: conversion to FileObject is failing, possibly need xtest
//        FileObject fo = FileUtil.toFileObject(file);
//        assertNotNull("failed to convert File to FileObject!", fo);
//        List<FileObject> roots = new ArrayList<FileObject>(1);
//        roots.add(fo);
//        pm.setSourcePath(roots);
//        roots = pm.getSourcePath();
//        assertNotNull("source path not defined!", roots);
//        for (int ii = 0; ii < 4; ii++) {
//            // Resume in order to hit the breakpoint.
//            SessionHelper.resumeAndWait(session);
//            // We are supposedly at a breakpoint, verify that this is so.
//            Location loc = BreakpointHelper.getLocation(session);
//            assertNotNull("missed hitting breakpoint", loc);
//            try {
//                assertEquals("PathManagerTestCode.java", loc.sourceName());
//                fo = pm.findSource(loc);
//                assertNotNull("source for location not found", fo);
//                assertEquals("PathManagerTestCode.java", fo.getNameExt());
//                fo = pm.findSource(loc.declaringType());
//                assertNotNull("source for class not found", fo);
//                assertEquals("PathManagerTestCode.java", fo.getNameExt());
//                fo = pm.findSource(loc.declaringType().name());
//                assertNotNull("source for name not found", fo);
//                assertEquals("PathManagerTestCode.java", fo.getNameExt());
//            } catch (AbsentInformationException aie) {
//                fail(aie.toString());
//            }
//        }
//
//        // Resume once more to let the program exit.
//        SessionHelper.resumeAndWait(session);
//        // The debuggee will have exited now and the session is inactive.
    }
}
