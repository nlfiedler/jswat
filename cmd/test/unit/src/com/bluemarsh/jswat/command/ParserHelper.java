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
 * $Id: ParserHelper.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.io.PrintWriter;
import java.io.StringWriter;
import junit.framework.TestCase;

/**
 * Utility class for the CommandParser unit tests.
 *
 * @author Nathan Fiedler
 */
public class ParserHelper {

    /**
     * Runs the given command parser tests.
     *
     * @param  tcase   test case running this tester.
     * @param  parser  command parser to which input is sent.
     * @param  datum   array of test data.
     */
    public void performTest(TestCase tcase, CommandParser parser, TestData[] datum) {
        for (TestData data : datum) {
            StringWriter sw = new StringWriter(80);
            PrintWriter pw = new PrintWriter(sw);
            parser.setOutput(pw);
            String result = null;
            try {
                parser.parseInput(data.getInput());
                result = sw.toString();
                if (data.shouldFail()) {
                    // was expected to fail
                    StringBuilder buf = new StringBuilder();
                    buf.append(data.getInput());
                    buf.append(" <<should have failed -- result>> ");
                    buf.append(result);
                    tcase.fail(buf.toString());
                }
            } catch (CommandException ce) {
                // This handles missing arguments, too.
                if (!data.shouldFail()) {
                    // was not expected to fail
                    StringBuilder buf = new StringBuilder();
                    buf.append(data.getInput());
                    buf.append(" <<should not have failed>> ");
                    sw = new StringWriter(256);
                    pw = new PrintWriter(sw);
                    ce.printStackTrace(pw);
                    buf.append(sw.toString());
                    tcase.fail(buf.toString());
                }
            } catch (Exception e) {
                StringBuilder buf = new StringBuilder();
                buf.append(data.getInput());
                buf.append(" <<unexpected exception>> ");
                sw = new StringWriter(256);
                pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                buf.append(sw.toString());
                tcase.fail(buf.toString());
            }

            boolean equals;
            if (result == null && data.getResult() == null) {
                equals = true;
            } else if (result == null || data.getResult() == null) {
                equals = false;
            } else {
                equals = result.equals(data.getResult());
            }

            if (!equals) {
                if (data.getMessage() == null) {
                    StringBuilder buf = new StringBuilder();
                    buf.append(data.getInput());
                    buf.append(" <<should have been>> ");
                    buf.append(data.getResult());
                    buf.append(" <<but got>> ");
                    buf.append(result);
                    tcase.fail(buf.toString());

                } else {
                    StringBuilder buf = new StringBuilder();
                    buf.append(data.getMessage());
                    buf.append(" <<expected>> ");
                    buf.append(data.getResult());
                    buf.append(" <<but got>> ");
                    buf.append(result);
                    tcase.fail(buf.toString());
                }
            }
        }
    }
}
