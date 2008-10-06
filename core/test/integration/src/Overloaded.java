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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Overloaded.java 15 2007-06-03 00:01:17Z nfiedler $
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
