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
 * Test code for the ExceptionBreakpointTest.
 *
 * @author Nathan Fiedler
 */
public class ExceptionBreakpointTestCode {

    public static void main(String[] args) {
        throwIllArg();
        throwNullPt();
        throwIndexBounds();
    }

    private static void throwIllArg() {
        try {
            throw new IllegalArgumentException();
        } catch (Exception e) {
            // do nothing and continue on with the test
        }
    }

    private static void throwNullPt() {
        try {
            throw new NullPointerException();
        } catch (Exception e) {
            // do nothing and continue on with the test
        }
    }
    
    private static void throwIndexBounds() {
        try {
            throw new IndexOutOfBoundsException();
        } catch (Exception e) {
            // do nothing and continue on with the test
        }
    }
}
