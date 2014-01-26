// An output-only test for JSwat.
// $Author: nfiedler $ $Date: 2002-10-13 00:03:33 -0700 (Sun, 13 Oct 2002) $ $Rev: 604 $

import java.io.*;

public class output {

    protected static void printStuff(int sleepLen, int numLoops) {
        try {
            for (int i = 0; i < numLoops; i++) {
                System.out.println("out 1");
                Thread.sleep((long) (Math.random() * sleepLen));
                System.err.println("err 1");
                Thread.sleep((long) (Math.random() * sleepLen));
                System.out.println("out 2");
                Thread.sleep((long) (Math.random() * sleepLen));
                System.err.println("err 2");
                Thread.sleep((long) (Math.random() * sleepLen));
                System.out.println("out 3");
                Thread.sleep((long) (Math.random() * sleepLen));
                System.err.println("err 3");
                Thread.sleep((long) (Math.random() * sleepLen));
                System.out.println("out 4");
                Thread.sleep((long) (Math.random() * sleepLen));
                System.err.println("err 4");
            }
        } catch (InterruptedException ie) {
            // yeah, like that'll ever happen
            ie.printStackTrace();
        }
    }

    public static void main(String[] args) {

        int sleepLen = 2000;
        int numLoops = 10;
        if (args.length > 0) {
            try {
                sleepLen = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
                System.err.println("First argument must be an integer!");
                System.exit(1);
            }
            if (args.length > 1) {
                try {
                    numLoops = Integer.parseInt(args[1]);
                } catch (NumberFormatException nfe) {
                    System.err.println("Second argument must be an integer!");
                    System.exit(1);
                }
            }
        }
        printStuff(sleepLen, numLoops);
    }
}
