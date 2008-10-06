/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      Unit Tests
 * FILE:        JavaParserTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/22/02        Initial version
 *      nf      10/19/02        Updated file path
 *      nf      11/17/03        Moved to new package
 *
 * $Id: JavaParserTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang.java;

import com.bluemarsh.jswat.lang.ClassDefinition;
import com.bluemarsh.jswat.parser.java.lexer.LexerException;
import com.bluemarsh.jswat.parser.java.parser.ParserException;
import java.io.*;
import java.util.*;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the JavaParser class.
 */
public class JavaParserTest extends TestCase {

    public JavaParserTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(JavaParserTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testJavaParserLines() {
        // Note that the test cases must be run from the top-level
        // directory for this file reference to work.
        File f = new File("test/classes/brktest.java");
        if (!f.exists()) {
            fail("unable to locate brktest.java file");
        }

        try {
            // Parse the file as Java source.
            FileReader fr = new FileReader(f);
            JavaParser jp = new JavaParser(fr);
            jp.parse();
            List lines = jp.getClassLines();
            // Display the class definitions.
            Iterator iter = lines.iterator();

            ClassDefinition cd = (ClassDefinition) iter.next();
            assertEquals("brktest", cd.getClassName());
            assertEquals(3, cd.getBeginLine());
            assertEquals(25, cd.getEndLine());

            cd = (ClassDefinition) iter.next();
            assertEquals("brktest$1", cd.getClassName());
            assertEquals(8, cd.getBeginLine());
            assertEquals(17, cd.getEndLine());

            cd = (ClassDefinition) iter.next();
            assertEquals("brktest$2", cd.getClassName());
            assertEquals(11, cd.getBeginLine());
            assertEquals(14, cd.getEndLine());

            cd = (ClassDefinition) iter.next();
            assertEquals("brktest$NotMuch", cd.getClassName());
            assertEquals(22, cd.getBeginLine());
            assertEquals(24, cd.getEndLine());

            cd = (ClassDefinition) iter.next();
            assertEquals("brktest2", cd.getClassName());
            assertEquals(28, cd.getBeginLine());
            assertEquals(31, cd.getEndLine());

            if (iter.hasNext()) {
                fail("parser returned too many class definitions");
            }

        } catch (NoSuchElementException nsee) {
            fail(nsee.toString());
        } catch (IOException ioe) {
            fail(ioe.toString());
        } catch (LexerException le) {
            fail(le.toString());
        } catch (ParserException pe) {
            fail(pe.toString());
        }
    }
}
