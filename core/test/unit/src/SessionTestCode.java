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
 * $Id: SessionTestCode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

/**
 * Unit test code for the SessionTest.
 *
 * @author  Nathan Fiedler
 */
public class SessionTestCode implements Runnable {
    /** The lock for forcing the main thread to wait. */
    private static Object waitingLock = new Object();

    /**
     * We run so the debugger can use our thread for invocation.
     */
    public void run() {
        long counter = 0;
        try {
            while (true) {
                Thread.sleep(1000);
                counter++; // breakpoint, line 35
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Stops the main thread from waiting on the lock.
     */
    public static void stopWaiting() {
        synchronized (waitingLock) {
            waitingLock.notifyAll();
        }
    }

    /**
     * Main entry point for the program.
     *
     * @param  args  command-line arguments.
     */
    public static void main(String[] args) {
        SessionTestCode stc = new SessionTestCode();
        // Start a thread for the debugger to invoke methods on. Make sure
        // this thread does not prevent the JVM from exiting.
        Thread th = new Thread(stc);
        th.setDaemon(true);
        th.start();
        try {
            // Now we wait for the debugger to cause us to exit.
            synchronized (waitingLock) {
                waitingLock.wait();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}
