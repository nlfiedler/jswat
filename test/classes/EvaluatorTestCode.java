/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: EvaluatorTestCode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

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
    private static final int STATIC_CONST_INT = 5;
    private static Point static_point = new Point(10, 20);
    private static Object static_nullobj = null;
    private static Object[] static_nullarr = null;

    static {
        static_char_array = new char[] { 'a', 'b', 'c', '1', '2', '3' };
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
    private String[] inst_Str_array;
    private String[] inst_Str_array_empty;
    private Object inst_StrAsObj = new String("string");
    private Object[] inst_Str_array_as_Obj;
    private Object inst_ArrayAsObj;
    private final int INST_CONST_INT = 8;
    private Point inst_point = new Point(15, 25);
    private Object inst_nullobj = null;
    private Object[] inst_nullarr = null;

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
        inst_ArrayAsObj = inst_char_array;
        inst_Str_array = new String[] { "one", "two", "three", "four", "five" };
        inst_Str_array_empty = new String[0];
        inst_Str_array_as_Obj = inst_Str_array;
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
    
    protected static class Point {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
