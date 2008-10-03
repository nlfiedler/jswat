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
 * $Id: TestData.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.command;

/**
 * Structure to hold test parameters.
 *
 * @author Nathan Fiedler
 */
public class TestData {
    /** EOL character sequence. */
    private static final String EOL;
    /** Input to be parsed. */
    private String input;
    /** The expected result as the expected type. */
    private String result;
    /** The error message to display if the input does not
     * evaluate to be equal to the result. */
    private String message;
    /** True if the input is expected to cause an exception. */
    private boolean fail;

    static {
        EOL = System.getProperty("line.separator");
    }

    public TestData(String input) {
        this.input = input;
        fail = true;
    }

    public TestData(String input, String result) {
        this.input = input;
        setResult(result);
    }

    public TestData(String input, String result, String message) {
        this.input = input;
        this.message = message;
        setResult(result);
    }

    public String getInput() {
        return input;
    }

    public String getMessage() {
        return message;
    }

    public String getResult() {
        return result;
    }

    private void setResult(String result) {
        if (result != null) {
            result = result.replace("\n", EOL);
        }
        this.result = result;
    }

    public boolean shouldFail() {
        return fail;
    }
}
