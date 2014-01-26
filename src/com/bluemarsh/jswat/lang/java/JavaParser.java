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
 *      nf      05/05/02        Fixed bug 521
 *      nf      05/07/02        Fixed bug 526
 *      nf      11/19/02        Fixed bug 668
 *      nf      11/30/02        Fixed bug 668, again
 *      mm      05/11/03        Fixed bug 768
 *      nf      11/17/03        Moved to new package
 *
 * $Id: JavaParser.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang.java;

import com.bluemarsh.jswat.lang.ClassDefinition;
import com.bluemarsh.jswat.lang.MethodDefinition;
import com.bluemarsh.jswat.parser.java.analysis.DepthFirstAdapter;
import com.bluemarsh.jswat.parser.java.lexer.Lexer;
import com.bluemarsh.jswat.parser.java.lexer.LexerException;
import com.bluemarsh.jswat.parser.java.node.*;
import com.bluemarsh.jswat.parser.java.parser.Parser;
import com.bluemarsh.jswat.parser.java.parser.ParserException;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Class JavaParser is responsible for parsing a Java source file and
 * generating a list of ClassDefinition objects.
 *
 * @author  Nathan Fiedler
 */
public class JavaParser extends DepthFirstAdapter {
    /** Name of anonymous classes, used for conveniently marking them. */
    private static final String ANONYMOUS_CLASS_NAME = "new";
    /** Source file reader. */
    private Reader reader;
    /** List of ClassDefinition objects. */
    private List classLines;
    /** List of MethodDefinition objects. */
    private List methodLines;
    /** Name of the package the parsed class is defined in, if any. */
    private String packageName;

    /**
     * Constructs a JavaParser to read from the given Reader.
     *
     * @param  r  input reader.
     */
    public JavaParser(Reader r) {
        reader = r;
        classLines = new ArrayList();
        methodLines = new ArrayList();
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
        int line = node.getLBrace().getLine();
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
     * Find the beginning line of the interface declaration.
     *
     * @param  node  class body.
     * @return  first line of something.
     */
    protected int findInterfaceBeginLine(AInterfaceBody node) {
        LinkedList list = node.getInterfaceMemberDeclaration();
        Iterator iter = list.iterator();
        // Start with a nice default.
        int line = node.getLBrace().getLine();
        // There should probably be more sophisticated code here.
        return line;
    } // findBeginLine

    /**
     * Correct the names of the classes to include the package name, if
     * any, and to include enclosing instances, if any.
     */
    protected void fixClassNames() {
        // Sort the class definitions so that they will appear in
        // ascending order based on the opening brace line. Note that
        // the opening brace line is more accurate in terms of computing
        // the names of inner classes. But we still use the first line
        // of code (beginLine) elsewhere.
        Collections.sort(classLines, new Comparator() {
                public int compare(Object o1, Object o2) {
                    ClassDefinition cd1 = (ClassDefinition) o1;
                    ClassDefinition cd2 = (ClassDefinition) o2;
                    return cd1.getBraceLine() - cd2.getBraceLine();
                }
            });

        // We will prepend the outer class names to the inner classes.
        // This has O(n^2) running time, which is not good but it's only
        // done once and the list is likely to be pretty short.
        // Anonymous inner classes fall within the widest containing
        // class, as opposed to the closest fitting inner class.

        List newClassNames = new ArrayList();
        int anonymousNum = 1;
        ClassDefinition outtermostClass = null;
        for (int ii = 0; ii < classLines.size(); ii++) {
            ClassDefinition cd = (ClassDefinition) classLines.get(ii);
            // Is this a new outtermost class (begin line after end line
            // of current outtermost class; if none, the answer is yes)?
            if (outtermostClass == null
                || outtermostClass.getEndLine() < cd.getBraceLine()) {
                // Yes, save reference to outtermostClass and reset the
                // anonymous class counter to one.
                outtermostClass = cd;
                anonymousNum = 1;
            }

            String classname = cd.getClassName();
            // Is this class an anonymous inner class?
            if (classname == ANONYMOUS_CLASS_NAME) {
                // Yes, give it the name of the outter most class plus
                // the anonymous number value.
                classname = outtermostClass.getClassName() + '$'
                    + anonymousNum;
                anonymousNum++;
            } else {
                // Not an inner class; have to prepend the names of all
                // containing classes to this one.
                for (int jj = ii - 1; jj >= 0; jj--) {
                    ClassDefinition cc = (ClassDefinition) classLines.get(jj);
                    if ((cc.getBraceLine() <= cd.getBraceLine())
                        && (cc.getEndLine() >= cd.getEndLine())) {
                        classname = cc.getClassName() + '$' + classname;
                    }
                }
            }

            // Append to another list so as not to pollute the original.
            newClassNames.add(classname);
        }

        boolean hasPackage = packageName != null && packageName.length() > 0;
        for (int ii = classLines.size() - 1; ii >= 0; ii--) {
            ClassDefinition cd = (ClassDefinition) classLines.get(ii);
            String newname = (String) newClassNames.get(ii);
            if (hasPackage) {
                // Add the package name to the front of the class names.
                cd.setClassName(packageName + '.' + newname);
            } else {
                cd.setClassName(newname);
            }
        }
    } // fixClassNames

    /**
     * Returns the list of ClassDefinition objects, indicating where
     * each class is defined in the source file.
     *
     * @return  list of class lines.
     */
    public List getClassLines() {
        return classLines;
    } // getClassLines

    /**
     * Get the method signature minus the modifiers.
     *
     * @param desc  method declaration, including the modifiers.
     *              e.g. public static void getMethodSign(String desc)
     * @return method signature excluding the modifiers.
     *         e.g. getMethodSign(String desc)
     */
    private static String getMethodDescShort(String desc) {
        if (desc == null || desc.length() == 0) {
            return desc;
        }
        int bracketIndex = desc.indexOf("(");
        if (bracketIndex < 0)  {
            return desc;
        }
        int startIndex = desc.lastIndexOf(' ', bracketIndex - 2);
        if (startIndex < 0)  {
            return desc;
        }
        return desc.substring(startIndex + 1);
    } // getMethodDescShort

    /**
     * Returns the list of MethodDefinition objects, indicating where
     * each method is defined in the source file.
     *
     * @return  list of method lines.
     */
    public List getMethodLines() {
        return methodLines;
    } // getMethodLines

    /**
     * Returns the name of the package that the parsed class is defined in.
     *
     * @return  package name, or null if none.
     */
    public String getPackageName() {
        return packageName;
    } // getPackageName

    /**
     * Parse the input source file and builds out a set of data
     * structures representing the file. Use the getter methods to
     * retrieve the data.
     *
     * @throws  IOException
     *          if an I/O error occurs.
     * @throws  LexerException
     *          if a lexer problem occurs.
     * @throws  ParserException
     *          if a parser problem occurs.
     */
    public void parse() throws IOException, LexerException, ParserException {
        classLines.clear();
        methodLines.clear();

        PushbackReader pbr = new PushbackReader(reader);
        Lexer lexer = new Lexer(pbr);
        Parser parser = new Parser(lexer);
        try {
            Start start = parser.parse();
            start.apply(this);
            fixClassNames();
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
    protected static String trimWhitespace(String s) {
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

    /**
     * In a class body.
     *
     * @param  node  class body node.
     */
    public void inAClassBody(AClassBody node) {
        // Determine the start and end line numbers.
        int begin = findBeginLine(node);
        int end = node.getRBrace().getLine();
        String cname = null;
        Node parentNode = node.parent();
        if (parentNode instanceof AClassDeclaration) {
            // This handles normal class declarations, as well as
            // named inner class declaractions.
            AClassDeclaration parent = (AClassDeclaration) parentNode;
            TIdentifier name = parent.getIdentifier();
            cname = name.getText();

        } else if (parentNode instanceof
                   ASimpleClassInstanceCreationExpression
                   || parentNode instanceof
                   AQualifiedClassInstanceCreationExpression
                   || parentNode instanceof
                   AInnerclassClassInstanceCreationExpression) {
            // This handles an anonymous inner class declaration.
            // Use special value of 'new' for anonymous classes.
            cname = ANONYMOUS_CLASS_NAME;
        }
        int braceLine = node.getLBrace().getLine();
        ClassDefinition cd = new ClassDefinition(cname, braceLine, begin, end);
        classLines.add(cd);
    } // inAClassBody

    /**
     * In a constructor declaration.
     *
     * @param  node  constructor declaration.
     */
    public void inAConstructorDeclaration(AConstructorDeclaration node) {
        // Get constructor descriptor.
        List modifiers = node.getModifier();
        StringBuffer desc = new StringBuffer();
        Iterator iter = modifiers.iterator();
        while (iter.hasNext()) {
            desc.append(iter.next());
            if (iter.hasNext()) {
                desc.append(' ');
            }
        }
        AConstructorDeclarator acd = (AConstructorDeclarator)
            node.getConstructorDeclarator();
        desc.append(acd);

        // Get the line number.
        AConstructorBody acb = (AConstructorBody) node.getConstructorBody();
        int line = acb.getLBrace().getLine();
        String descStr = desc.toString();
        String sign = getMethodDescShort(descStr);
        MethodDefinition md = new MethodDefinition(descStr, sign, line);
        methodLines.add(md);
    } // inAConstructorDeclaration

    /**
     * In a interface body. Treat it like a class so that inner classes
     * can be named properly.
     *
     * @param  node  interface body node.
     */
    public void inAInterfaceBody(AInterfaceBody node) {
        // Determine the start and end line numbers.
        int begin = findInterfaceBeginLine(node);
        int end = node.getRBrace().getLine();
        String cname = null;
        Node parentNode = node.parent();
        if (parentNode instanceof AInterfaceDeclaration) {
            // This handles normal class declarations, as well as
            // named inner class declaractions.
            AInterfaceDeclaration parent = (AInterfaceDeclaration) parentNode;
            TIdentifier name = parent.getIdentifier();
            cname = name.getText();

        }
        int braceLine = node.getLBrace().getLine();
        ClassDefinition cd = new ClassDefinition(cname, braceLine, begin, end);
        classLines.add(cd);
    } // inAInterfaceBody

    /**
     * In a method declaration.
     *
     * @param  node  method declaration.
     */
    public void inAMethodDeclaration(AMethodDeclaration node) {
        // Get the method header and toString() it.
        String desc = node.getMethodHeader().toString();

        // Get the LBrace line number.
        PMethodBody body = node.getMethodBody();
        if (body instanceof ABlockMethodBody) {
            ABlockMethodBody abody = (ABlockMethodBody) body;
            PBlock block = abody.getBlock();
            if (block instanceof ABlock) {
                ABlock ablock = (ABlock) block;
                int line = ablock.getLBrace().getLine();
                MethodDefinition md = new MethodDefinition(
                    desc, getMethodDescShort(desc), line);
                methodLines.add(md);
            }
            // else it's not a real method block.
        }
        // else it's not a real method definition.
    } // inAMethodDeclaration

    /**
     * In a package declaration.
     *
     * @param  node  package declaration.
     */
    public void inAPackageDeclaration(APackageDeclaration node) {
        // Save the package name.
        PName pname = node.getName();
        if (pname instanceof ASimpleNameName) {
            ASimpleNameName asimnname = (ASimpleNameName) pname;
            packageName = trimWhitespace(asimnname.toString());
        } else if (pname instanceof AQualifiedNameName) {
            AQualifiedNameName aqualnname = (AQualifiedNameName) pname;
            packageName = trimWhitespace(aqualnname.toString());
        }
    } // inAPackageDeclaration
} // JavaParser
