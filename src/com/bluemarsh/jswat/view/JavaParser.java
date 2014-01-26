/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * MODULE:      View
 * FILE:        JavaParser.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/19/02        Initial version
 *      nf      04/03/02        Let parse() throw exceptions
 *      nf      05/05/02        Fixed bug #521
 *      nf      05/07/02        Fixed bug #526
 *
 * DESCRIPTION:
 *      This file contains the JavaParser class definition.
 *
 * $Id: JavaParser.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.parser.java.analysis.DepthFirstAdapter;
import com.bluemarsh.jswat.parser.java.lexer.*;
import com.bluemarsh.jswat.parser.java.node.*;
import com.bluemarsh.jswat.parser.java.parser.*;
import java.io.*;
import java.util.*;
import javax.swing.text.BadLocationException;

/**
 * Class JavaParser is responsible for parsing a Java source file and
 * generating a list of ClassDefinition objects.
 *
 * @author  Nathan Fiedler
 */
public class JavaParser extends DepthFirstAdapter {
    /** Source file reader. */
    protected Reader reader;
    /** List of ClassDefinition objects. */
    protected List classLines;
    /** Name of the package the parsed class is defined in, if any. */
    protected String packageName;

    /**
     * Constructs a JavaParser to read from the given Reader.
     *
     * @param  r  input reader.
     */
    public JavaParser(Reader r) {
        reader = r;
        classLines = new ArrayList();
    } // JavaParser

     /**
     * Find the beginning line of the class declaration.
     *
     * @param  node  class body.
     * @return  first line of something.
     */
    protected int findBeginLine(AClassBody node) {
        LinkedList list = node.getClassBodyDeclaration();
        Iterator iter = list.iterator();
        // Start with a nice default.
        int line = node.getLBrace().getLine();;
        while (iter.hasNext()) {
            Object o = iter.next();

            if (o instanceof AClassMemberDeclarationClassBodyDeclaration) {
                AClassMemberDeclarationClassBodyDeclaration acmdcbd =
                    (AClassMemberDeclarationClassBodyDeclaration) o;
                PClassMemberDeclaration pcmd =
                    acmdcbd.getClassMemberDeclaration();

                if (pcmd instanceof AMethodDeclarationClassMemberDeclaration) {
                    AMethodDeclarationClassMemberDeclaration amdcmd =
                        (AMethodDeclarationClassMemberDeclaration) pcmd;
                    AMethodDeclaration amd = (AMethodDeclaration)
                        amdcmd.getMethodDeclaration();
                    PMethodHeader pmh = amd.getMethodHeader();

                    if (pmh instanceof ATypeMethodHeader) {
                        ATypeMethodHeader atmh = (ATypeMethodHeader) pmh;
                        PMethodDeclarator pmd = atmh.getMethodDeclarator();
                        if (pmd instanceof AIdentifierMethodDeclarator) {
                            AIdentifierMethodDeclarator aimd =
                                (AIdentifierMethodDeclarator) pmd;
                            TIdentifier ti = aimd.getIdentifier();
                            line = ti.getLine();
                            break;
                        }

                    } else if (pmh instanceof AVoidMethodHeader) {
                        AVoidMethodHeader avmh = (AVoidMethodHeader) pmh;
                        PMethodDeclarator pmd = avmh.getMethodDeclarator();
                        if (pmd instanceof AIdentifierMethodDeclarator) {
                            AIdentifierMethodDeclarator aimd =
                                (AIdentifierMethodDeclarator) pmd;
                            TIdentifier ti = aimd.getIdentifier();
                            line = ti.getLine();
                            break;
                        }
                    }
                }

            } else if (o instanceof
                       AConstructorDeclarationClassBodyDeclaration) {
                AConstructorDeclarationClassBodyDeclaration acdcbd =
                    (AConstructorDeclarationClassBodyDeclaration) o;
                AConstructorDeclaration acd = (AConstructorDeclaration)
                    acdcbd.getConstructorDeclaration();
                AConstructorDeclarator acdr = (AConstructorDeclarator)
                    acd.getConstructorDeclarator();
                ASimpleName asn = (ASimpleName) acdr.getSimpleName();
                TIdentifier ti = asn.getIdentifier();
                line = ti.getLine();
                break;

            } else if (o instanceof AStaticInitializerClassBodyDeclaration) {
                AStaticInitializerClassBodyDeclaration asicbd =
                    (AStaticInitializerClassBodyDeclaration) o;
                AStaticInitializer asi = (AStaticInitializer)
                    asicbd.getStaticInitializer();
                ABlock ab = (ABlock) asi.getBlock();
                TLBrace tlb = ab.getLBrace();
                line = tlb.getLine();
                break;

            } else if (o instanceof ABlockClassBodyDeclaration) {
                ABlockClassBodyDeclaration abcbd =
                    (ABlockClassBodyDeclaration) o;
                ABlock ab = (ABlock) abcbd.getBlock();
                TLBrace tlb = ab.getLBrace();
                line = tlb.getLine();
                break;
            }
        }
        return line;
    } // findBeginLine

    /**
     * Correct the names of the classes to include the package name,
     * if any, and to include enclosing instances, if any.
     */
    protected void fixClassNames() {
        // Sort the class definitions so that they will appear in
        // ascending order based on the beginning line.
        Collections.sort(classLines, new Comparator() {
                public int compare(Object o1, Object o2) {
                    ClassDefinition cd1 = (ClassDefinition) o1;
                    ClassDefinition cd2 = (ClassDefinition) o2;
                    return cd1.getBeginLine() - cd2.getBeginLine();
                }
            });

        // Classes named 'new' are anonymous inner classes.

        // We will prepend the outer class names to the inner classes.
        // This has O(n^2) running time, which isn't great but it's
        // only done once and the list is likely to be pretty short.
        // Anonymous inner classes fall within the widest containing
        // class, as opposed to the closet fitting inner class.

        int size = classLines.size();
        int anon = 1;
        String widestClass = null;
        // For each class definition...
        for (int i = 0; i < size; i++) {
            ClassDefinition cdi = (ClassDefinition) classLines.get(i);
            // 'new' signals an anonymous inner class
            if (cdi.getClassName().equals("new")) {
                // Consecutive anonymous inner classes will always
                // have the same containing class, so check first.
                if (anon == 1) {
                    // Need to find widest containing class.
                    int widestBegin = cdi.getBeginLine();
                    int widestEnd = cdi.getEndLine();
                    for (int j = i - 1; j >= 0; j--) {
                        ClassDefinition cdj = (ClassDefinition)
                            classLines.get(j);
                        if ((cdj.getBeginLine() <= widestBegin) &&
                            (cdj.getEndLine() >= widestEnd)) {
                            widestClass = cdj.getClassName();
                        }
                    }
                }
                cdi.setClassName(widestClass + "$" + anon);
                anon++;

            } else {
                // Reset anonymous inner class counter.
                anon = 1;
                for (int j = i - 1; j >= 0; j--) {
                    // If j overlaps i, prepend j's name to i's.
                    ClassDefinition cdj = (ClassDefinition)
                        classLines.get(j);
                    if ((cdj.getBeginLine() <= cdi.getBeginLine()) &&
                        (cdj.getEndLine() >= cdi.getEndLine())) {
                        cdi.setClassName(cdj.getClassName() + "$" +
                                         cdi.getClassName());
                    }
                }
            }
        }

        // Add the package name to the front of the class names.
        if ((packageName != null) && packageName.length() > 0) {
            for (int ii = 0; ii < size; ii++) {
                ClassDefinition cdi = (ClassDefinition) classLines.get(ii);
                cdi.setClassName(packageName + "." + cdi.getClassName());
            }
        }
    } // fixClassNames

    /**
     * Returns the name of the package that the parsed class is defined in.
     *
     * @return  package name, or null if none.
     */
    public String getPackageName() {
        return packageName;
    } // getPackageName

    /**
     * Parse the input source file and return a List of ClassDefinition
     * objects.
     *
     * @return  list of class definitions, or null if error.
     */
    public List parse() throws IOException, LexerException, ParserException {
        PushbackReader pbr = new PushbackReader(reader);
        Lexer lexer = new Lexer(pbr);
        Parser parser = new Parser(lexer);
        try {
            Start start = parser.parse();
            start.apply(this);
            fixClassNames();
            return classLines;
        } finally {
            try {
                pbr.close();
            } catch (IOException ioe) { /* ignore */ }
        }
    } // parse

    /**
     * Strips out all whitespace characters from string 's'.
     *
     * @param  s  string to be trimmed.
     * @return  string without whitespace.
     */
    protected String trimWhitespace(String s) {
        int len = s.length();
        StringBuffer buf = new StringBuffer(len);
        for (int ii = 0; ii < len; ii++) {
            char c = s.charAt(ii);
            if (!Character.isWhitespace(c)) {
                buf.append(c);
            }
        }
        return buf.toString();
    } // trimWhitespace

    //
    // These are the DepthFirstAdapter methods that we override.
    //

    public void inAClassBody(AClassBody node)
    {
        // Determine the start and end line numbers.
        TLBrace tlb = node.getLBrace();
        TRBrace trb = node.getRBrace();
        int begin = findBeginLine(node);
        int end = trb.getLine();
        String cname = null;
        Node parentNode = node.parent();
        if (parentNode instanceof AClassDeclaration) {
            // This handles normal class declarations, as well as
            // named inner class declaractions.
            AClassDeclaration parent = (AClassDeclaration) parentNode;
            TIdentifier name = parent.getIdentifier();
            cname = name.getText();

        } else if (parentNode instanceof
                   ASimpleClassInstanceCreationExpression ||
                   parentNode instanceof
                   AQualifiedClassInstanceCreationExpression ||
                   parentNode instanceof
                   AInnerclassClassInstanceCreationExpression) {
            // This handles an anonymous inner class declaration.
            ASimpleClassInstanceCreationExpression ascice =
                (ASimpleClassInstanceCreationExpression) parentNode;
            // Use special value of 'new' for anonymous classes.
            cname = "new";
        }
        ClassDefinition cd = new ClassDefinition(cname, begin, end);
        classLines.add(cd);
    }

    public void inAPackageDeclaration(APackageDeclaration node)
    {
        // Save the package name.
        PName pname = node.getName();
        if (pname instanceof ASimpleNameName) {
            ASimpleNameName asimnname = (ASimpleNameName) pname;
            packageName = trimWhitespace(asimnname.toString());
        } else if (pname instanceof AQualifiedNameName) {
            AQualifiedNameName aqualnname = (AQualifiedNameName) pname;
            packageName = trimWhitespace(aqualnname.toString());
        }
    }
} // JavaParser
