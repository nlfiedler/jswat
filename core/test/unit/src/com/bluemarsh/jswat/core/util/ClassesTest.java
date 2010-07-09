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
 * The Original Software is the JSwat Core Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.util;

import com.bluemarsh.jswat.core.SessionHelper;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Classes class.
 *
 * @author Nathan Fiedler
 */
public class ClassesTest {

    @Test
    public void testFindClasses() {
        SessionHelper.launchDebuggee("LineBreakpointTestCode",
                "LineBreakpointTestCode:53");
        Session session = SessionHelper.getSession();
        VirtualMachine vm = session.getConnection().getVM();

        // Exact name search.
        List<ReferenceType> result = Classes.findClasses(vm, "java.lang.String");
        assertEquals(1, result.size());
        assertEquals("java.lang.String", result.get(0).name());

        // Pattern with leading wildcard.
        result = Classes.findClasses(vm, "*.lang.String");
        assertEquals(1, result.size());
        assertEquals("java.lang.String", result.get(0).name());

        // Pattern in the middle should fail.
        try {
            Classes.findClasses(vm, "java.*.String");
            fail("should have failed");
        } catch (IllegalArgumentException iae) {
            // expected
        }

        // Pattern with trailing wildcard.
        result = Classes.findClasses(vm, "java.lang.String*");
        // Trim off the innner classes and array types.
        List<String> names = new ArrayList<String>();
        for (ReferenceType type : result) {
            String name = type.name();
            if (name.indexOf('$') == -1 && !name.endsWith("[]")) {
                names.add(name);
            }
        }
        Collections.sort(names);
        assertEquals(5, names.size());
        assertEquals("java.lang.String", names.get(0));
        assertEquals("java.lang.StringBuffer", names.get(1));
        assertEquals("java.lang.StringBuilder", names.get(2));
        assertEquals("java.lang.StringCoding", names.get(3));
        assertEquals("java.lang.StringValue", names.get(4));
        SessionHelper.resumeAndWait(session);
    }

    @Test
    public void testFindMethod() throws Exception {
        SessionHelper.launchDebuggee("MethodBreakpointTestCode",
                "MethodBreakpointTestCode:method_params(char,double)");
        Session session = SessionHelper.getSession();
        VirtualMachine vm = session.getConnection().getVM();
        List<ReferenceType> classes = vm.classesByName("MethodBreakpointTestCode");
        ReferenceType clazz = classes.get(0);

        // Overloaded method with no arguments provided should fail.
        List<String> args = new ArrayList<String>();
        try {
            Classes.findMethod(clazz, "method_params", args, false, false);
            fail("should have failed!");
        } catch (NoSuchMethodException ignored) {
            // expected
        }

        // Method name that does not exist at all.
        try {
            Classes.findMethod(clazz, "foobar", args, false, false);
            fail("should have failed!");
        } catch (NoSuchMethodException ignored) {
            // expected
        }

        // Ambiguous method specification.
        try {
            args.add("*");
            ReferenceType str_type = vm.classesByName("java.lang.String").get(0);
            Classes.findMethod(str_type, "valueOf", args, false, false);
            fail("should have failed!");
        } catch (AmbiguousMethodException ignored) {
            args.clear();
        }

        // Successful test with fuzzy matching of wrapper to primitive types.
        {
            args.add("Ljava/lang/Character;");
            args.add("Ljava/lang/Double;");
            Method result = Classes.findMethod(clazz, "method_params", args, true, false);
            assertEquals("method_params", result.name());
            assertEquals("(CD)V", result.signature());
            args.clear();
        }

        // Failure test without fuzzy matching of wrapper to primitive types.
        try {
            args.add("Ljava/lang/Character;");
            args.add("Ljava/lang/Double;");
            Classes.findMethod(clazz, "method_params", args, false, false);
            fail("should have failed!");
        } catch (NoSuchMethodException ignored) {
            args.clear();
        }

        {
            args.add("<null>");
            Method result = Classes.findMethod(clazz, "main", args, true, false);
            assertEquals("main", result.name());
            assertEquals("([Ljava/lang/String;)V", result.signature());
            args.clear();
        }

        // Success test case with exact matching of arguments.
        {
            args.add("C");
            args.add("D");
            Method result = Classes.findMethod(clazz, "method_params", args, false, false);
            assertEquals("method_params", result.name());
            assertEquals("(CD)V", result.signature());
            args.clear();
        }

        // Search for a constructor.
        {
            Method result = Classes.findMethod(clazz, "MethodBreakpointTestCode", args, false, false);
            assertEquals("<init>", result.name());
            assertEquals("()V", result.signature());
        }

        SessionHelper.resumeAndWait(session);
    }

    @Test
    public void testHotswap() throws Exception {
        String buildDir = System.getProperty("test.build.dir");
        SessionHelper.launchDebuggee("LineBreakpointTestCode",
                "LineBreakpointTestCode:53");
        Session session = SessionHelper.getSession();
        VirtualMachine vm = session.getConnection().getVM();
        ReferenceType clazz = vm.classesByName("LineBreakpointTestCode").get(0);
        File file = new File(buildDir, "LineBreakpointTestCode.class");
        InputStream code = new FileInputStream(file);
        Classes.hotswap(clazz, code, vm);
        SessionHelper.resumeAndWait(session);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvokeMethodNullThread() throws Exception {
        Classes.invokeMethod(null, null, null, null, null);
    }

    @Test
    public void testInvokeMethod() throws Exception {
        SessionHelper.launchDebuggee("MethodBreakpointTestCode",
                "MethodBreakpointTestCode:method_params(char,double)");
        Session session = SessionHelper.getSession();
        VirtualMachine vm = session.getConnection().getVM();
        List<ReferenceType> classes = vm.classesByName("MethodBreakpointTestCode");
        ClassType clazz = (ClassType) classes.get(0);
        DebuggingContext dc = ContextProvider.getContext(session);
        StackFrame frame = dc.getStackFrame();
        ObjectReference object = frame.thisObject();
        ThreadReference thread = dc.getThread();
        Method method = clazz.concreteMethodByName("method_params",
                "(Ljava/lang/String;IZ)Ljava/lang/String;");
        List<Value> arguments = new ArrayList<Value>();

        // Invalid argument cases.
        try {
            Classes.invokeMethod(object, clazz, thread, null, arguments);
            fail("should have failed");
        } catch (IllegalArgumentException iae) {
            // expected
        }
        try {
            Classes.invokeMethod(object, clazz, thread, method, null);
            fail("should have failed");
        } catch (IllegalArgumentException iae) {
            // expected
        }

        // Successful test case
        arguments.add(vm.mirrorOf("foobar"));
        arguments.add(vm.mirrorOf(101));
        arguments.add(vm.mirrorOf(true));
        Value result = Classes.invokeMethod(object, clazz, thread, method, arguments);
        assertTrue(result instanceof StringReference);
        StringReference sr = (StringReference) result;
        assertEquals("foobar101true", sr.value());
        SessionHelper.resumeAndWait(session);
    }
}
