/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: NameValuePair.java 15 2007-06-03 00:01:17Z nfiedler $
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
