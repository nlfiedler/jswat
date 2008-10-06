/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ThreadBreakpointTestCode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

/**
 * Test code for the ThreadBreakpointTest.
 *
 * @author Nathan Fiedler
 */
public class ThreadBreakpointTestCode {
    
    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            public void run() {
                Thread th = Thread.currentThread();
                String name = th.getName();
                System.out.println("in thread " + name);
            }
        };
        String[] names = new String[] {
            "thread1", "thread2", "thread3", "thread4"
        };
        try {
            for (String name : names) {
                Thread th = new Thread(runnable, name);
                th.start();
                // Force the threads to start in a particular order, so
                // the unit tests can verify the breakpoints work.
                th.join();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        runnable.run();
    }
}
