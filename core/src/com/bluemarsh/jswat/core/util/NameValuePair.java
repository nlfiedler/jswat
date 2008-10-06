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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: NameValuePair.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.util;

/**
 * Represents a named value.
 *
 * @author Nathan Fiedler
 */
public class NameValuePair<V> {
    /** The name. */
    private String name;
    /** The value. */
    private V value;

    /**
     * Creates a new instance of NameValuePair.
     *
     * @param  name   name of value (may be null).
     * @param  value  the value (may be null).
     */
    public NameValuePair(String name, V value) {
        this.name = name;
        this.value = value;
    }

    public boolean equals(Object obj) {
        NameValuePair<?> pair = (NameValuePair<?>) obj;
        if (pair.value == null && value == null) {
            return true;
        } else if (pair.value == null || value == null) {
            return false;
        } else {
            return pair.value.equals(value);
        }
    }

    /**
     * Returns the name.
     *
     * @return  name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value.
     *
     * @return  value.
     */
    public V getValue() {
        return value;
    }

    public int hashCode() {
        if (value != null) {
            return value.hashCode();
        } else {
            return -1;
        }
    }

    /**
     * Returns the name.
     *
     * @return  String of this.
     */
    public String toString() {
        return getName();
    }
}
