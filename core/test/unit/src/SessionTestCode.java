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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionTestCode.java 6 2007-05-16 07:14:24Z nfiedler $
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
                counter++; // breakpoint, line 41
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
