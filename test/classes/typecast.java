// Test class for type-cast operator node in expression evaluator.
// $Id: typecast.java 14 2007-06-02 23:50:55Z nfiedler $

public class typecast {

    public static void main(String[] args) {
        System.out.println("stop here");
    }

    // Put these here so changes will not shift the code lines above.
    private static boolean z_val = true;
    private static byte b_val = 8;
    private static char c_val = 'a';
    private static double d_val = 1.234567890;
    private static float f_val = 1.234f;
    private static int i_val = 1048576;
    private static long l_val = 2 ^ 56;
    private static short s_val = 256;

    private static Boolean z_Value = Boolean.FALSE;
    private static Byte b_Value = new Byte((byte) 8);
    private static Character c_Value = new Character('a');
    private static Double d_Value = new Double(1.234567890);
    private static Float f_Value = new Float(1.234f);
    private static Integer i_Value = new Integer(1048576);
    private static Long l_Value = new Long(2 ^ 56);
    private static Short s_Value = new Short((short) 256);
}
