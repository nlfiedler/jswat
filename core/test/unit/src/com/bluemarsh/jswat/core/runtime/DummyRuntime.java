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
 * are Copyright (C) 2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.runtime;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Dummy implementation of the JavaRuntime interface, for testing.
 *
 * @author  Nathan Fiedler
 */
public class DummyRuntime implements JavaRuntime {

    /**
     * Creates a new instance of DummyRuntime.
     */
    public DummyRuntime() {
    }

    public JavaRuntime clone() {
        return this;
    }

    public File findExecutable(File base, String exec) {
        return base;
    }

    public String getBase() {
        return "dummy";
    }

    public String getExec() {
        return "dummy.exe";
    }

    public String getIdentifier() {
        return "dummy";
    }

    public String getName() {
        return "Dummy";
    }

    public List<String> getSources() {
        return Collections.emptyList();
    }

    public boolean isValid() {
        return true;
    }

    public void setBase(String base) {
    }

    public void setExec(String exec) {
    }

    public void setIdentifier(String id) {
    }

    public void setSources(List<String> sources) {
    }
}
