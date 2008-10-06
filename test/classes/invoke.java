// JSwat test case for the invoke command
// $Id: invoke.java 14 2007-06-02 23:50:55Z nfiedler $

public class invoke {

    public static void main(String[] args) {
        System.out.println("stop here");
    }

    public static boolean method(boolean b) {
        return !b;
    }

    public static boolean method(Boolean b) {
        return b.booleanValue();
    }

    public static byte method(byte b) {
        byte b2 = 1;
        b += b2;
        return b;
    }

    public static byte method(Byte b) {
        return b.byteValue();
    }

    public static char method(char c) {
        return Character.toUpperCase(c);
    }

    public static char method(Character c) {
        return c.charValue();
    }

    public static double method(double d) {
        return Math.sqrt(d);
    }

    public static double method(Double d) {
        return d.doubleValue();
    }

    public static float method(float f) {
        float f2 = 0.333f;
        f *= f2;
        return f;
    }

    public static float method(Float f) {
        return f.floatValue();
    }

    public static int method(int i) {
        return i * 2;
    }

    public static int method(Integer i) {
        return i.intValue();
    }

    public static long method(long l) {
        return l * 10;
    }

    public static long method(Long l) {
        return l.longValue();
    }

    public static short method(short s) {
        short s2 = 2;
        s /= s2;
        return s;
    }

    public static short method(Short s) {
        return s.shortValue();
    }

    public static Number method(Number n) {
        return new Integer(n.intValue() * 3);
    }

    public static String method(String s) {
        return '"' + s + '"';
    }

    public static String method(Integer I, char c, int i, boolean b) {
        StringBuffer buf = new StringBuffer(80);
        buf.append(I);
        buf.append(", ");
        buf.append(c);
        buf.append(", ");
        buf.append(i);
        buf.append(", ");
        buf.append(b);
        return buf.toString();
    }

    public static String method(String s, char c, int i, boolean b) {
        StringBuffer buf = new StringBuffer(80);
        buf.append(s);
        buf.append(", ");
        buf.append(c);
        buf.append(", ");
        buf.append(i);
        buf.append(", ");
        buf.append(b);
        return buf.toString();
    }

    // Put these here so changes will not shift the code lines above.
    // Use these to test invoking the methods above. DO NOT CHANGE VALUES,
    // as the unit tests use these.
    private static boolean z_val = true;
    private static byte b_val = 8;
    private static char c_val = 'a';
    private static double d_val = 1.23456789;
    private static float f_val = 1.234f;
    private static int i_val = 1048576;
    private static int ic_val = 65; // value of 'a'
    private static long l_val = 72057594037927936L;
    private static short s_val = 256;

    private static Boolean z_Value = Boolean.TRUE;
    private static Byte b_Value = new Byte((byte) 8);
    private static Character c_Value = new Character('a');
    private static Double d_Value = new Double(1.23456789);
    private static Float f_Value = new Float(1.234f);
    private static Integer i_Value = new Integer(1048576);
    private static Long l_Value = new Long(72057594037927936L);
    private static Short s_Value = new Short((short) 256);
}
