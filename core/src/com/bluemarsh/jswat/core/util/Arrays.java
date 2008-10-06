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
 * are Copyright (C) 2004-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Arrays.java 6 2007-05-16 07:14:24Z nfiedler $
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
        if (arr1 == null && arr2 != null) {
            return arr2;
        } else if (arr2 == null) {
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
