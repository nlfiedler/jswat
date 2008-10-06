// Test class for variable handling in JSwat
// $Id: vartest.java 14 2007-06-02 23:50:55Z nfiedler $

public class vartest {

    private static void byteArray() {
        String str = "Hello world bytes!";
        byte[] bytes = str.getBytes();
        System.out.println("str = " + str);
        System.out.println("bytes = " + bytes);
    }

    private static void charArray() {
        String str = "Hello world chars!";
        char[] chars = new char[str.length()];
        str.getChars(0, chars.length, chars, 0);
        System.out.println("str = " + str);
        System.out.println("chars = " + chars);
    }

    public static void main(String[] args) {
        byteArray();
        charArray();
    }
}
