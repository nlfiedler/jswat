// An input/output test for JSwat.
// $Id: iotest.java 14 2007-06-02 23:50:55Z nfiedler $

import java.io.*;

/**
 * Class iotest tests I/O related stuff.
 *
 * @author  Nathan Fiedler
 */
public class iotest {

    /**
     * Opens the named file, then closes it. Waits for the user
     * before returning.
     *
     * @param  f  file to open.
     */
    protected static void openFileTest(String f) {
        try {
            File file = new File(f);
            System.out.println("Opening file " + file.getCanonicalPath());
            FileInputStream fis = new FileInputStream(file);
            int i = fis.read();
            System.out.println("Read one byte from file.");
            fis.close();
            System.out.println("Closed the file input stream.");
            System.out.println("Press Enter to continue...");
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            br.readLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    } // openFileTest

    /**
     * As the user for their name and then print it out a number of times.
     */
    protected static void inputTest() {
        System.out.print("Enter ");
        System.out.print("your ");
        System.out.print("name: ");
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        try {
            String name = br.readLine();
            for (int i = 0; i < 10; i++) {
                System.out.println(i + ": Hello " + name + '!');
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    } // inputTest

    /**
     * Displays the various 'path' values from File.
     */
    public static void filepaths() {
        File f = new File(".", "file.java");
        // Using new File("./file.java") works exactly the same.
        System.out.println("getName(): " + f.getName());
        // file.java
        System.out.println("getPath(): " + f.getPath());
        // ./file.java
        System.out.println("getAbsolutePath(): " + f.getAbsolutePath());
        // /home/me/java/./file.java
        try {
            System.out.println("getCanonicalPath(): " + f.getCanonicalPath());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // /home/me/java/file.java
        System.out.println("getParent(): " + f.getParent());
        // .
    }

    /**
     * Tests both input and output.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            openFileTest(args[0]);
        } else {
            inputTest();
        }
        filepaths();
    } // main
} // iotest
