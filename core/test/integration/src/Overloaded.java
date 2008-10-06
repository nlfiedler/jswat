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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Overloaded.java 6 2007-05-16 07:14:24Z nfiedler $
 */

/**
 * Test code for the method breakpoints.
 *
 * @author Nathan Fiedler
 */
public class Overloaded {

    public void amethod() {
        System.out.println("in amethod");
    }

    public void amethod(int i) {
        System.out.println("in amethod(int)");
    }

    public void amethod(String s) {
        System.out.println("in amethod(String)");
    }

    public void amethod(boolean b) {
        System.out.println("in amethod(boolean)");
    }

    public void themethod() {        
        System.out.println("in themethod()");
    }

    public static void main(String[] args) {
        Overloaded o = new Overloaded();
        o.amethod();
        o.amethod(10);
        o.amethod("abc");
        o.amethod(true);
        o.themethod();
    }
}
