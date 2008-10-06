/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Snoozer.java 15 2007-06-03 00:01:17Z nfiedler $
 */

/**
 * Test code for the thread breakpoint.
 *
 * @author Nathan Fiedler
 */
public class Snoozer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Runner r = new Runner();
        String[] names = { "a", "ab", "abc", "abcd", "abcde" };
        for (String name : names) {
            System.out.println("thread " + name + " starting");
            Thread th = new Thread(r, name);
            th.start();
        }
        System.out.println("done creating threads");
    }
}

/**
 * This class is useful for testing that line breakpoints can resolve
 * against non-public classes in source files whose names do not match
 * the name of the class.
 *
 * In particular, set a line breakpoint in this code, then close the
 * editor window and run the debuggee -- it should stop at the breakpoint
 * and open this file.
 */
class Runner implements Runnable {

    /**
     * Sleep for a few seconds and then return.
     */
    public void run() {
        String name = Thread.currentThread().getName();
        System.out.println("thread " + name + " running");
        long time = (long) Math.random() * 1000;
        try {
            Thread.sleep(time);
        } catch (InterruptedException ie) {
            // ignore
        }
        System.out.println("thread " + name + " done");
    }
}
