/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: TestData.java 15 2007-06-03 00:01:17Z nfiedler $
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
