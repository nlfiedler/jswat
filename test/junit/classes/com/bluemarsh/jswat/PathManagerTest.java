/*********************************************************************
 *
 *      Copyright (C) 2004-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: PathManagerTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.sun.jdi.*;
import java.io.*;
import java.util.*;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the PathManager class.
 */
public class PathManagerTest extends TestCase {

    public PathManagerTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(PathManagerTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    protected static void canonicalize(String[] arr) {
        for (int ii = 0; ii < arr.length; ii++) {
            try {
                arr[ii] = new File(arr[ii]).getCanonicalPath();
            } catch (IOException ioe) {
                fail(ioe.toString());
            }
        }
    }

    protected static void prepBreakpoint(Session session, Breakpoint bp) {
        BreakpointManager bm = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        try {
            bp.deleteOnExpire();
            bp.setExpireCount(1);
            bm.addNewBreakpoint(bp);
        } catch (ResolveException re) {
            fail(re.toString());
        }
    }

    protected static String arrayToString(String[] arr) {
        if (arr.length == 0) {
            return "";
        }
        StringBuffer buf = new StringBuffer(64);
        buf.append(arr[0]);
        for (int ii = 1; ii < arr.length; ii++) {
            buf.append(File.pathSeparatorChar);
            buf.append(arr[ii]);
        }
        return buf.toString();
    }

    protected static boolean compareArrays(String[] arr1, String[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        } else {
            for (int ii = 0; ii < arr1.length; ii++) {
                if (!arr1[ii].equals(arr2[ii])) {
                    System.err.println("Not equal: " + arr1[ii]
                                       + " and " + arr2[ii]);
                    return false;
                }
            }
            return true;
        }
    }

    public void test_PathManager_findPackageName() {
        Session session = SessionManager.beginSession();
        PathManager pm = (PathManager) session.getManager(PathManager.class);
        String origCP = pm.getClassPathAsString();
        String origSP = pm.getSourcePathAsString();
        String newCP = "/usr/java/classes:/home/java/build/classes";
        newCP = newCP.replace(':', File.pathSeparatorChar);
        newCP = newCP.replace('/', File.separatorChar);
        pm.setClassPath(newCP);
        String newSP = "/usr/java/src:/home/java/source";
        newSP = newSP.replace(':', File.pathSeparatorChar);
        newSP = newSP.replace('/', File.separatorChar);
        pm.setSourcePath(newSP);

        String fn = "/usr/java/src/com/bluemarsh/jswat";
        fn = fn.replace('/', File.separatorChar);
        String pkg = pm.findPackageName(fn);
        assertEquals("findPackageName() failed", "com.bluemarsh.jswat", pkg);

        fn = "/home/java/source/com/bluemarsh/jswat";
        fn = fn.replace('/', File.separatorChar);
        pkg = pm.findPackageName(fn);
        assertEquals("findPackageName() failed", "com.bluemarsh.jswat", pkg);

        fn = "/home/java/source";
        fn = fn.replace('/', File.separatorChar);
        pkg = pm.findPackageName(fn);
        assertEquals("findPackageName() failed", "", pkg);

        fn = "/home/java/build/classes/com/bluemarsh/jswat";
        fn = fn.replace('/', File.separatorChar);
        pkg = pm.findPackageName(fn);
        assertEquals("findPackageName() failed", "com.bluemarsh.jswat", pkg);

        fn = "/does/not/exist".replace('/', File.separatorChar);
        assertNull(pm.findPackageName(fn));

        pm.setClassPath(origCP);
        pm.setSourcePath(origSP);
        SessionManager.endSession();
    }

    public void test_PathManager_ClassPath() {
        Session session = SessionManager.beginSession();
        PathManager pm = (PathManager) session.getManager(PathManager.class);
        String origCP = pm.getClassPathAsString();

        String[] cpArray = new String[] {
            "/home/java/classes",
            "/usr/java/classes",
            "/share/java/classes"
        };
        canonicalize(cpArray);
        String cpString  = arrayToString(cpArray);

        pm.setClassPath(cpString);
        String[] cpArray2 = pm.getClassPath();
        assertTrue(compareArrays(cpArray, cpArray2));

        pm.setClassPath(origCP);
        SessionManager.endSession();
    }

    public void test_PathManager_SourcePath() {
        Session session = SessionManager.beginSession();
        PathManager pm = (PathManager) session.getManager(PathManager.class);
        String origSP = pm.getSourcePathAsString();

        String[] spArray = new String[] {
            "/home/java/source",
            "/usr/java/source",
            "/share/java/source"
        };
        canonicalize(spArray);
        String spString = arrayToString(spArray);

        pm.setSourcePath(spString);
        String[] spArray2 = pm.getSourcePath();
        assertTrue("set/getSourcePath() failed",
                   compareArrays(spArray, spArray2));

        pm.setSourcePath(origSP);
        SessionManager.endSession();
    }

    public void test_PathManager_mapSource() {
        Session session = SessionManager.beginSession();
        PathManager pm = (PathManager) session.getManager(PathManager.class);
        pm.setClassPath("test/build/classes");
        pm.setSourcePath("test/classes");

        // Trivial public class defined in like-named file.
        try {
            SourceSource src = pm.mapSource("locals");
            assertTrue(src != null);
            assertTrue(!(src instanceof ByteCodeSource));
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }

        // Trivial case of public class with a package name.
        try {
            SourceSource src = pm.mapSource("pkg.Watches");
            assertTrue(src != null);
            assertTrue(!(src instanceof ByteCodeSource));
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }

        // Look for class that is non-public and defined in a source
        // file with another name.
        try {
            SourceSource src = pm.mapSource("pkg.MutableInteger");
            assertTrue(src != null);
            assertTrue(!(src instanceof ByteCodeSource));
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }

        SessionManager.launchSimple("pkg.Watches");
        Breakpoint bp = null;
        try {
            bp = new LineBreakpoint("pkg.Watches", 15);
        } catch (ClassNotFoundException cnfe) {
            fail(cnfe.toString());
        }
        prepBreakpoint(session, bp);
        SessionManager.resumeAndWait();

        // Test using Location method.
        ContextManager conman = (ContextManager)
            session.getManager(ContextManager.class);
        Location loc = conman.getCurrentLocation();
        try {
            SourceSource src = pm.mapSource(loc);
            assertTrue(src != null);
            assertTrue(!(src instanceof ByteCodeSource));
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }

        // Test using ReferenceType method.
        try {
            SourceSource src = pm.mapSource(loc.declaringType());
            assertTrue(src != null);
            assertTrue(!(src instanceof ByteCodeSource));
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }

        // Test using Location with non-public class with different-named
        // source file.
        try {
            bp = new LineBreakpoint("pkg.MutableInteger", 42);
        } catch (ClassNotFoundException cnfe) {
            fail(cnfe.toString());
        }
        prepBreakpoint(session, bp);
        SessionManager.resumeAndWait();
        loc = conman.getCurrentLocation();
        try {
            SourceSource src = pm.mapSource(loc);
            assertTrue(src != null);
            assertTrue(!(src instanceof ByteCodeSource));
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }

        SessionManager.deactivate(true);

        SessionManager.endSession();
    }
}
