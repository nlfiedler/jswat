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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: EvaluatorTestCode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Used in testing the expression evaluator.
 *
 * @author  Nathan Fiedler
 */
public class EvaluatorTestCode {
    // static members
    private static boolean static_boolean = true;
    private static byte static_byte = 1;
    private static char static_char = 'a';
    private static double static_double = 1.0;
    private static float static_float = 1.0f;
    private static int static_int = 1;
    private static long static_long = 1;
    private static short static_short = 1;
    private static String static_String = "static";
    private static String[] static_strarr = { "abc", "123", "!@#" };
    private static char[] static_char_array;
    private static int[] static_int_array;
    private static final int STATIC_CONST_INT = 5;
    private static Point static_point = new Point(10, 20);
    private static Object static_nullobj = null;
    private static Object[] static_nullarr = null;
    private static Object static_junk = Integer.MAX_VALUE;

    static {
        static_char_array = new char[] { 'a', 'b', 'c', '1', '2', '3' };
        static_int_array = new int[] { 0, 1, 2, 3, 4 };
    }

    // instance members
    private boolean inst_boolean = false;
    private byte inst_byte = 2;
    private char inst_char = 'A';
    private double inst_double = 2.0;
    private float inst_float = 2.0f;
    private int inst_int = 2;
    private long inst_long = 2;
    private short inst_short = 2;
    private String inst_String = "instance";
    private String[] inst_strarr = { "def", "456", "$%^" };
    private Object inst_Object = new Object();
    private char[] inst_char_array;
    private int[] inst_int_array;
    private String[] inst_Str_array;
    private String[] inst_Str_array_empty;
    private Object inst_StrAsObj = new String("string");
    private Object[] inst_Str_array_as_Obj;
    private Object inst_ArrayAsObj;
    private final int INST_CONST_INT = 8;
    private Point inst_point = new Point(15, 25);
    private Object inst_nullobj = null;
    private Object[] inst_nullarr = null;
    private Certificate[] certs;
    private static Object inst_junk = Integer.MIN_VALUE;

    /**
     * This is a convenient stopping point. Do not change the method name
     * because the unit test uses this name to set breakpoints.
     *
     * @param  p1  first parameter.
     * @param  p2  second parameter.
     * @param  p3  third parameter.
     */
    public static void staticMethod(int p1, char p2, String p3) {
        // Don't actually need any code here.
    }

    /**
     * This is a convenient stopping point. Do not change the method name
     * because the unit test uses this name to set breakpoints.
     *
     * @param  p1  first parameter.
     * @param  p2  second parameter.
     * @param  p3  third parameter.
     */
    public void instanceMethod(int p1, char p2, String p3) {
        // Don't actually need any code here.
    }

    /**
     * @param  arr  an array of strings
     * @return  returns the second array element, or null if there are
     *          less than two elements.
     */
    public String get2ndString(String[] arr) {
        return arr.length > 1 ? arr[1] : null;
    }

    /**
     * Creates a new instance of EvaluatorTestCode.
     */
    public EvaluatorTestCode() {
        inst_char_array = new char[] { 'A', 'B', 'C', '3', '2', '1' };
        inst_int_array = new int[] { 9, 8, 7, 6, 5 };
        inst_ArrayAsObj = inst_char_array;
        inst_Str_array = new String[] { "one", "two", "three", "four", "five" };
        inst_Str_array_empty = new String[0];
        inst_Str_array_as_Obj = inst_Str_array;

        // This block of code is to test bug 912 in which the evaluator
        // would get an exception when trying to acquire the type of a
        // variable whose class had not been loaded. Simply referring
        // to the 'certs' field should cause the bug.
        String root = System.getProperty("java.home");
        File f = new File(root, "lib/ext/sunjce_provider.jar");
        if (f.exists()) {
            try {
                JarFile jf = new JarFile(f);
                JarEntry jfe = jf.getJarEntry(
                        "com/sun/crypto/provider/JceKeyStore.class");
                InputStream is = jf.getInputStream(jfe);
                byte[] ba = new byte [8192];
                while (is.read(ba) != -1) { }
                certs = jfe.getCertificates();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * The program starts here.
     *
     * @param  args  the command line arguments
     */
    public static void main(String[] args) {
        staticMethod(10, 'd', "abc");
        EvaluatorTestCode etc = new EvaluatorTestCode();
        etc.instanceMethod(11, 'D', "ABC");
    }
}
