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
