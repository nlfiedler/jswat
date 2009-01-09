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
 * $Id$
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
