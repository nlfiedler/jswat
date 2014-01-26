/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: OperatorNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;


/**
 * Class OperatorNode is the base class for all operators.
 *
 * @author  Nathan Fiedler
 */
abstract class OperatorNode extends ParentNode {

    /**
     * Determines the type of 'o' and returns true if it is a boolean.
     *
     * @param  o  object to test.
     * @return  true if 'o' is a boolean.
     */
    public static boolean isBoolean(Object o) {
        return (o instanceof Boolean || o instanceof BooleanValue);
    } // isBoolean

    /**
     * Determines the type of 'o' and returns true if it is a character.
     *
     * @param  o  object to test.
     * @return  true if 'o' is a character.
     */
    public static boolean isCharacter(Object o) {
        return (o instanceof Character || o instanceof CharValue);
    } // isCharacter

    /**
     * Determines the type of 'o' and returns true if it is a double.
     *
     * @param  o  object to test.
     * @return  true if 'o' is a double.
     */
    public static boolean isDouble(Object o) {
        return (o instanceof Double || o instanceof DoubleValue);
    } // isDouble

    /**
     * Determines the type of 'o' and returns true if it is a floating
     * point number.
     *
     * @param  o  object to test.
     * @return  true if 'o' is a number.
     */
    public static boolean isFloating(Object o) {
        return (o instanceof Double || o instanceof DoubleValue
                || o instanceof Float || o instanceof FloatValue);
    } // isFloating

    /**
     * Determines the type of 'o' and returns true if it is a long.
     *
     * @param  o  object to test.
     * @return  true if 'o' is a long.
     */
    public static boolean isLong(Object o) {
        return (o instanceof Long || o instanceof LongValue);
    } // isLong

    /**
     * Determines the type of 'o' and returns true if it is some type of
     * number, either integral, floating, or character.
     *
     * @param  o  object to test.
     * @return  true if 'o' is a numeric value.
     */
    public static boolean isNumber(Object o) {
        return (o instanceof Number || o instanceof ByteValue
                || o instanceof DoubleValue || o instanceof FloatValue
                || o instanceof IntegerValue || o instanceof LongValue
                || o instanceof ShortValue || o instanceof Character
                || o instanceof CharValue);
    } // isNumber

    /**
     * Determines the type of 'o' and returns true if it is either a
     * String or a StringReference.
     *
     * @param  o  object to test.
     * @return  true if 'o' is a numeric value.
     */
    public static boolean isString(Object o) {
        return (o instanceof String || o instanceof StringReference);
    }

    /**
     * Constructs a OperatorNode associated with the given token.
     *
     * @param  node  lexical token.
     */
    public OperatorNode(Token node) {
        super(node);
    } // OperatorNode

    /**
     * Returns the boolean value from the given object.
     *
     * @param  o  object that had better be a Boolean type.
     * @return  boolean value of object.
     * @throws  EvaluationException
     *          if o is not a boolean type.
     */
    public boolean getBooleanValue(Object o) throws EvaluationException {
        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        } else if (o instanceof BooleanValue) {
            return ((BooleanValue) o).value();
        } else {
            throw new EvaluationException(
                Bundle.getString("error.oper.bool"), getToken(), o);
        }
    } // getBooleanValue

    /**
     * Returns the char value from the given object.
     *
     * @param  o  object that had better be a Character type.
     * @return  char value of object.
     * @throws  EvaluationException
     *          if o is not a character type.
     */
    public char getCharValue(Object o) throws EvaluationException {
        if (o instanceof Character) {
            return ((Character) o).charValue();
        } else if (o instanceof CharValue) {
            return ((CharValue) o).value();
        } else {
            throw new EvaluationException(
                Bundle.getString("error.oper.char"), getToken(), o);
        }
    } // getCharValue

    /**
     * Returns the double value from the given object.
     *
     * @param  o  object that had better be a number.
     * @return  double value of object.
     * @throws  EvaluationException
     *          if o is not a number.
     */
    public double getDoubleValue(Object o) throws EvaluationException {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else if (o instanceof Character) {
            return (double) ((Character) o).charValue();
        } else if (o instanceof PrimitiveValue) {
            return ((PrimitiveValue) o).doubleValue();
        } else {
            throw new EvaluationException(
                Bundle.getString("error.oper.floating"), getToken(), o);
        }
    } // getDoubleValue

    /**
     * Returns the float value from the given object.
     *
     * @param  o  object that had better be a number.
     * @return  float value of object.
     * @throws  EvaluationException
     *          if o is not a number.
     */
    public float getFloatValue(Object o) throws EvaluationException {
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else if (o instanceof Character) {
            return (float) ((Character) o).charValue();
        } else if (o instanceof PrimitiveValue) {
            return ((PrimitiveValue) o).floatValue();
        } else {
            throw new EvaluationException(
                Bundle.getString("error.oper.floating"), getToken(), o);
        }
    } // getFloatValue

    /**
     * Returns the integer value from the given object.
     *
     * @param  o  object that had better be an integral value.
     * @return  integer value of object.
     * @throws  EvaluationException
     *          if o is not an integral number.
     */
    public int getIntValue(Object o) throws EvaluationException {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        } else if (o instanceof Character) {
            return (int) ((Character) o).charValue();
        } else if (o instanceof PrimitiveValue) {
            return ((PrimitiveValue) o).intValue();
        } else {
            throw new EvaluationException(
                Bundle.getString("error.oper.intval"), getToken(), o);
        }
    } // getIntValue

    /**
     * Returns the long value from the given object.
     *
     * @param  o  object that had better be an integral value.
     * @return  long value of object.
     * @throws  EvaluationException
     *          if o is not an integral number.
     */
    public long getLongValue(Object o) throws EvaluationException {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        } else if (o instanceof Character) {
            return (long) ((Character) o).charValue();
        } else if (o instanceof PrimitiveValue) {
            return ((PrimitiveValue) o).longValue();
        } else {
            throw new EvaluationException(
                Bundle.getString("error.oper.intval"), getToken(), o);
        }
    } // getLongValue

    /**
     * Returns the String value from the given object.
     *
     * @param  o  object that had better be an integral value.
     * @return  long value of object.
     * @throws  EvaluationException
     *          if o is not an integral number.
     */
    public String getStringValue(Object o) throws EvaluationException {
        if (o instanceof String) {
            return (String) o;
        } else if (o instanceof StringReference) {
            return ((StringReference) o).value();
        } else {
            throw new EvaluationException(
                Bundle.getString("error.oper.strval"), getToken(), o);
        }
    }

    /**
     * Returns true if this operator does not do any operation but
     * instead acts as a sentinel, delineating portions of an
     * expression. This includes (), [], and commas.
     *
     * @return  true if sentinel, false otherwise.
     */
    public boolean isSentinel() {
        return false;
    } // isSentinel

    /**
     * Returns this operator's precedence value. The lower the value the
     * higher the precedence. The values are equivalent to those
     * described in the Java Language Reference book (2nd ed.), p 106.
     *
     * @return  precedence value.
     */
    public abstract int precedence();
} // OperatorNode
