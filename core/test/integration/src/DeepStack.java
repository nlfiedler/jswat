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
 * $Id: DeepStack.java 15 2007-06-03 00:01:17Z nfiedler $
 */

/**
 * Code that has a stack including several methods, for testing the call
 * stack view in the debugger.
 *
 * @author  Nathan Fiedler
 */
public class DeepStack {

    /**
     * @return  an integer value.
     */
    public int subroutine_1() {
        int i = 10;
        i = subroutine_2(i);
        return i;
    }

    /**
     * @param  p  an integer value.
     * @return  an integer value.
     */
    public int subroutine_2(int p) {
        int q = p;
        q = subroutine_3(q);
        return q;
    }

    /**
     * @param  k  an integer value.
     * @return  an integer value.
     */
    public int subroutine_3(int k) {
        int j = k;
        j = subroutine_4(j);
        return j;
    }

    /**
     * @param  w  an integer value.
     * @return  an integer value.
     */
    public int subroutine_4(int w) {
        int x = w;
        x = x * 2;
        return x;
    }

    /**
     * Invoked by the Java virtual machine.
     *
     * @param  args  the command line arguments
     */
    public static void main(String[] args) {
        DeepStack ds = new DeepStack();
        int a = ds.subroutine_1();
        System.out.println("a = " + a);
    }
}
