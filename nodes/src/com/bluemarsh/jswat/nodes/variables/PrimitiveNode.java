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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes.variables;

import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ShortValue;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

/**
 * Represents a PrimitiveValue variable.
 *
 * @author Nathan Fiedler
 */
public class PrimitiveNode extends VariableNode {
    /** The primitive value. */
    private PrimitiveValue pval;
    /** The user-specified display modifiers. */
    private String modifiers;

    /**
     * Creates a new PrimitiveNode object.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  pval  the PrimitiveValue (may be null).
     * @param  mods  display modifiers.
     */
    public PrimitiveNode(String name, String type, VariableNode.Kind kind,
            PrimitiveValue pval, String mods) {
        super(Children.LEAF, name, type, kind);
        this.pval = pval;
        this.modifiers = mods;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        String propvalue = "invalid";
        if (modifiers == null) {
            if (pval instanceof CharValue) {
                CharValue cv = (CharValue) pval;
                StringBuilder sb = new StringBuilder();
                if (!Character.isISOControl(cv.charValue())) {
                    // If it is a printable character, show it.
                    sb.append(pval.toString());
                    sb.append(' ');
                }
                // Always print the hexadecimal value of the character.
                sb.append("(\\u");
                sb.append(Strings.toHexString(cv.value()));
                sb.append(')');
                propvalue = sb.toString();
            } else if (pval != null) {
                propvalue = pval.toString();
                set.put(createProperty(PROP_VALUE,
                        pval.toString()));
            } else {
                propvalue = "null";
            }
        } else {
            propvalue = applyModifiers();
        }
        set.put(createProperty(PROP_VALUE, propvalue));
        return sheet;
    }

    /**
     * Apply the display modifiers to the value.
     *
     * @return  modified value.
     */
    private String applyModifiers() {
        if (pval != null) {
            boolean bInteger = false;
            long lValue = 0;
            if (pval instanceof CharValue) {
                CharValue cv = (CharValue) pval;
                if (modifiers.indexOf('u') >= 0 ||
                        Character.isISOControl(cv.charValue())) {
                    // Display Unicode for character.
                    return "(\\u" + Strings.toHexString(cv.value()) + ')';
                } else if (modifiers.indexOf('s') < 0) {
                    // 's' is of higher priority than any of 'xbo'.
                    lValue = cv.longValue();
                    bInteger = true;
                }
            } else if (pval instanceof ByteValue) {
                lValue = ((ByteValue) pval).longValue();
                bInteger = true;
            } else if (pval instanceof ShortValue) {
                lValue = ((ShortValue) pval).longValue();
                bInteger = true;
            } else if (pval instanceof IntegerValue) {
                lValue = ((IntegerValue) pval).longValue();
                bInteger = true;
            } else if (pval instanceof LongValue) {
                lValue = ((LongValue) pval).longValue();
                bInteger = true;
            }

            if (bInteger) {
                if (modifiers.indexOf('x') >= 0) {
                    return "0x" + Long.toHexString(lValue)
                    .toUpperCase();
                } else if (modifiers.indexOf('o') >= 0) {
                    return "0" + Long.toOctalString(lValue);
                } else if (modifiers.indexOf('b') >= 0) {
                    String s = Long.toBinaryString(lValue);
                    // Pad the value with zeros so it is obviously binary.
                    int b = Long.numberOfLeadingZeros(lValue);
                    if (b == 64) {
                        // The value was zero.
                        s = "00000000";
                    } else {
                        // Rather than padding to 64 characters, find
                        // the closest-fitting 8-bit boundary.
                        b = b % 8;
                        StringBuilder sb = new StringBuilder();
                        while (b > 0) {
                            sb.append('0');
                            b--;
                        }
                        s = sb.toString() + s;
                    }
                    return s;
                }
            }

            // Unknown or the 's' modifier.
            return pval.toString();
        } else {
            return "null";
        }
    }
}
