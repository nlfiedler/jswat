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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Steppable.java 6 2007-05-16 07:14:24Z nfiedler $
 */

/**
 * Tests the single-stepping features, such as exclusions and skipping
 * synthetic methods.
 *
 * @author Nathan Fiedler
 */
public class Steppable {

    public Steppable() {
    }

    public static void main(String[] args) {
        Singleton synth = Singleton.instance();
        synth.print();
    }
}

/**
 * Which pieces of code will we single-step through? That depends on how
 * the instance is initialized, it seems. If theInstance is set via the
 * declaration line (i.e. "private static final Synthesizer ...") then
 * you will not step through the constructor. However, if the instance
 * is created in instance() then you will step through the constructor
 * and field initializers.
 *
 * <p>The reason for all of this is that the debugger will not step
 * through the class loading and initialization of static field. This has
 * been the case for all versions of JSwat and apparently all versions of
 * the JDK (at least 1.3.1, 1.4.2, and 1.5.0). It seems to be a JDI feature
 * and is not something that JSwat can do anything about.</p>
 *
 * @author Nathan Fiedler
 */
class Singleton {
    private static Singleton theInstance;
    private static final double PI = 3.14d; // will not step here
    private static int integer;
    private int my_integer;
    private float my_float = 1.234f;
    private float my_long;

    {
        my_long = 1000l;
    }

    static {
        // Will not step here, however, you can set a breakpoint and
        // step through this code line by line.
        int value1 = 0;
        int value2 = 1;
        for (int ii = 0; ii < 10; ii++) {
            int sum = value1 + value2;
            value1 = value2;
            value2 = sum;
        }
        integer = value2;
    }

    private Singleton() {
        int value1 = 0;
        int value2 = 1;
        for (int ii = 0; ii < 8; ii++) {
            int sum = value1 + value2;
            value1 = value2;
            value2 = sum;
        }
        my_integer = value2;
    }

    public static Singleton instance() {
        if (theInstance == null) {
            theInstance = new Singleton();
        }
        return theInstance;
    }
    
    public void print() {
        System.out.println("PI = " + PI);
        System.out.println("integer = " + integer);
        System.out.println("my_integer = " + my_integer);
        System.out.println("my_float = " + my_float);
        System.out.println("my_long = " + my_long);
    }
}
