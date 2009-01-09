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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
