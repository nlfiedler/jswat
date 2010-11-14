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
 * are Copyright (C) 2009-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.event.Event;
import org.openide.util.NbBundle;

/**
 * Class HitCountCondition implements a breakpoint conditional that is
 * satisfied when the hit count reaches a certain value, either equal
 * to, greater than, or multiple of a user-specified value.
 *
 * @author  Nathan Fiedler
 */
public class HitCountCondition implements Condition {

    /** Hit count with which to compare. */
    private int count;
    /** The type of this condition. */
    private HitCountConditionType type;

    /**
     * Creates a new instance of HitCountCondition.
     */
    public HitCountCondition() {
    }

    @Override
    public String describe() {
        return NbBundle.getMessage(HitCountCondition.class,
                "HitCountCondition.describe", type, count);
    }

    /**
     * Returns the hit count test value.
     *
     * @return  test value for hit count.
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the type of this hit count condition.
     *
     * @return  hit count type.
     */
    public HitCountConditionType getType() {
        return type;
    }

    @Override
    public boolean isSatisfied(Breakpoint bp, Event event)
            throws ConditionException {
        int hit = bp.getHitCount();
        if (type == null) {
            // Make sure the type is set to something.
            type = HitCountConditionType.EQUAL;
        }
        return type.evaluate(count, hit);
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    /**
     * Set the hit count value to be compared.
     *
     * @param  count  hit count with which to compare.
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Sets the type of the hit count condition. If null, defaults to
     * 'equal to' condition.
     *
     * @param  type  type of the condition.
     */
    public void setType(HitCountConditionType type) {
        this.type = type;
    }
}
