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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultExpressionWatch.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

/**
 * A default implementation of the ExpressionWatch interface.
 *
 * @author Nathan Fiedler
 */
public class DefaultExpressionWatch extends AbstractWatch
        implements ExpressionWatch {
    /** Name of 'expression' property. */
    public static final String PROP_EXPRESSION = "expression";
    /** The expression being watched. */
    private String expression;

    /**
     * Creates a new instance of DefaultExpressionWatch.
     */
    public DefaultExpressionWatch() {
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expr) {
        String old = expression;
        expression = expr;
        propSupport.firePropertyChange(PROP_EXPRESSION, old, expression);
    }
}
