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
 * $Id: SteppingTestCode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

/**
 * Test code for the StepperTest.
 *
 * @author  Nathan Fiedler
 */
public class SteppingTestCode {

    public void stepMethod() {
        System.out.println("in SteppingTestCode.stepMethod()");
    }

    public static void main(String[] args) {
        Inner inn = new Inner();
        inn.method_I();
        STSecond.method_STS();
        SteppingTestCode stc = new SteppingTestCode();
        stc.stepMethod();
    }

    protected static class Inner {

        public void method_I() {
            System.out.println("in Inner.method_I()");
        }
    }
}

class STSecond {

    public static void method_STS() {
        System.out.println("in STSecond.method_STS()");
    }
}
