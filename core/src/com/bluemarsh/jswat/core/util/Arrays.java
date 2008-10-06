/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Arrays.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.util;

import java.lang.reflect.Array;

/**
 * Utility methods for arrays.
 *
 * @author Nathan Fiedler
 */
public class Arrays {
    
    /**
     * Creates a new instance of Arrays.
     */
    private Arrays() {
    }

    /**
     * Joins two arrays of objects of the same type. If one is null, the other
     * is returned. If both are null, null is returned. Otherwise, a new array
     * of size equal to the length of both arrays is returned, with the elements
     * of arr1 appearing before the elements of arr2.
     *
     * @param  arr1  first array.
     * @param  arr2  second array.
     * @return  joined arrays, or null if both arrays are null.
     */
    public static Object[] join(Object[] arr1, Object[] arr2) {
        if (arr1 == null || arr1.length == 0) {
            return arr2;
        } else if (arr2 == null || arr2.length == 0) {
            return arr1;
        } else {
            int size = arr1.length + arr2.length;
            Object[] arr = (Object[]) Array.newInstance(
                    arr1.getClass().getComponentType(), size);
            System.arraycopy(arr1, 0, arr, 0, arr1.length);
            System.arraycopy(arr2, 0, arr, arr1.length, arr2.length);
            return arr;
        }
    }
}
