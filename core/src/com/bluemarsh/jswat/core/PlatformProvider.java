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
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core;

import org.openide.util.Lookup;

/**
 * Class PlatformProvider provides convenient accessor methods for
 * retrieving an instance of the PlatformService.
 *
 * @author Nathan Fiedler
 */
public class PlatformProvider {
    /** The PlatformService instance, if it has already been retrieved. */
    private static PlatformService platformService;

    /**
     * Creates a new instance of PlatformProvider.
     */
    private PlatformProvider() {
    }

    /**
     * Retrieve the PlatformService instance, creating one if necessary.
     *
     * @return  PlatformService instance.
     */
    public static synchronized PlatformService getPlatformService() {
        if (platformService == null) {
            // Perform lookup to find a PlatformService instance.
            platformService = Lookup.getDefault().lookup(PlatformService.class);
        }
        return platformService;
    }
}
