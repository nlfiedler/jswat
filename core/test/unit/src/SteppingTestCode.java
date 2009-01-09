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
