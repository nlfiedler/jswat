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
 * $Id: ExpressionWatch.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

/**
 * An ExpressionWatch represents a watch based on an expression.
 *
 * @author Nathan Fiedler
 */
public interface ExpressionWatch extends Watch {

    /**
     * Returns the watch expression to be evaluated.
     *
     * @return  watch expression.
     */
    String getExpression();

    /**
     * Sets the watch expression to be evaluated.
     *
     * @param  expr  new watch expression.
     */
    void setExpression(String expr);
}
