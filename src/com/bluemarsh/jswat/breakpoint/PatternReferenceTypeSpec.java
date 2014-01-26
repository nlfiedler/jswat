/*********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
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
 * MODULE:      Breakpoints
 * FILE:        PatternReferenceTypeSpec.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/30/99        Initial version
 *      nf      02/25/01        Added getIdentifier() and isExact()
 *      nf      04/29/01        Added support for pre/suffix wildcards
 *      nf      03/08/02        Moved isJavaIdentifier to ClassUtils
 *
 * DESCRIPTION:
 *      This file defines the class for handling class specifications.
 *
 * $Id: PatternReferenceTypeSpec.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.util.ClassUtils;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.ClassPrepareRequest;
import java.util.StringTokenizer;

/**
 * Class PatternReferenceTypeSpec is used for specifying classes.
 * It can accept class name patterns that are either prefixed or
 * suffixed with an asterisk wildcard.
 *
 * @author  Nathan Fiedler
 */
public class PatternReferenceTypeSpec implements ReferenceTypeSpec {
    /** Name of the class specified, without wildcards. */
    protected String className;
    /** Name of the class specified, possibly with wild cards. */
    protected String classPattern;
    /** True if class specification is suffixed with a wildcard. */
    protected boolean postWild;
    /** True if class specification is prefixed with a wildcard. */
    protected boolean preWild;
    /** serial version */
    static final long serialVersionUID = -8892772293980731676L;

    /**
     * Constructs a new PatternReferenceTypeSpec for the given
     * class name pattern.
     *
     * @param  classId  class identifier string.
     * @exception  ClassNotFoundException
     *             Thrown if classId is not a valid identifier.
     */
    public PatternReferenceTypeSpec(String classId)
        throws ClassNotFoundException {
        // Save away the original pattern, wildcards and all.
        classPattern = classId;

        // Determine the wildcardedness of the class name pattern.
        // Will either start or end with an asterisk.
        preWild = classId.startsWith("*");
        postWild = classId.endsWith("*");
        if (preWild) {
            className = classId.substring(1);
        } else if (postWild) {
            className = classId.substring(0, classId.length() - 1);
        } else {
            className = classId;
        }

        // Do strict checking of class name validity because if the
        // name is invalid, it will never match a future loaded class.
        StringTokenizer tokenizer = new StringTokenizer(className, ".");
        int numTokens = tokenizer.countTokens();
        int curToken = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            curToken++;
            if (token.length() == 0) {
                if (curToken == 1) {
                    // The first element can be empty when wildcarded.
                    continue;
                } else if (curToken == numTokens) {
                    // The last element can be empty when wildcarded.
                    break;
                }
            }
            // Each dot-separated piece must be a valid identifier.
            if (!ClassUtils.isJavaIdentifier(token)) {
                throw new ClassNotFoundException(classId +
                                                 ": invalid part = " + token);
            }
        }
    } // PatternReferenceTypeSpec

    /**
     * Create a class prepare request appropriate for this
     * reference type specification.
     *
     * @param  vm  VirtualMachine to use for creating request.
     * @return  ClassPrepareRequest.
     */
    public ClassPrepareRequest createPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest request = 
            vm.eventRequestManager().createClassPrepareRequest();
        request.addClassFilter(classPattern);
        // Work around for JPDA bug 4331522.
        //request.addCountFilter(1);
        return request;
    } // createPrepareRequest

    /**
     * Returns the class identifier this specification is specifying.
     * This pattern includes any leading or trailing wildcards.
     *
     * @return  class identifier.
     */
    public String getIdentifier() {
        return classPattern;
    } // getIdentifier

    /**
     * Returns true if this type specification is an exact name.
     *
     * @return  true if exact name, false if wildcard.
     */
    public boolean isExact() {
        return !preWild && !postWild;
    } // isExact

    /**
     * Determines if the given class name matches this specification.
     *
     * @param  classname  name of class to match against.
     * @return  true if name matches this specification.
     */
    public boolean matches(String classname) {
        if (preWild) {
            return classname.endsWith(this.className);
        } else if (postWild) {
            return classname.startsWith(this.className);
        } else {
            return classname.equals(this.className);
        }
    } // matches

    /**
     * Determines if the given ReferenceType matches this specification.
     *
     * @param  refType  ReferenceType to match against.
     * @return  true if type matches this specification.
     */
    public boolean matches(ReferenceType refType) {
        if (preWild) {
            return refType.name().endsWith(className);
        } else if (postWild) {
            return refType.name().startsWith(className);
        } else {
            return refType.name().equals(className);
        }
    } // matches

    /**
     * Returns a String representation of this. This returns the
     * class name pattern, including wildcards.
     *
     * @return  String.
     */
    public String toString() {
        return classPattern;
    } // toString
} // PatternReferenceTypeSpec
