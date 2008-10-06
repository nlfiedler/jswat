/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * $Id: NamesTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.bluemarsh.jswat.Defaults;
import java.util.prefs.Preferences;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the Names class.
 */
public class NamesTest extends TestCase {

    public NamesTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(NamesTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testNamesIsJavaIdentifier() {
        // boolean isJavaIdentifier(String s);
        assertTrue(!Names.isJavaIdentifier(null));
        assertTrue(!Names.isJavaIdentifier(""));
        assertTrue(Names.isJavaIdentifier("ident"));
        assertTrue(Names.isJavaIdentifier("_ident"));
        assertTrue(!Names.isJavaIdentifier("128_ident"));
        assertTrue(Names.isJavaIdentifier("ide_nt"));
    }

    public void testNamesJustTheName() {
        // Save the old setting first, then change it to true.
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/util");
        boolean old = prefs.getBoolean("shortClassNames",
                                       Defaults.SHORT_CLASS_NAMES);
        prefs.putBoolean("shortClassNames", true);

        // String justTheName(String cname);
        assertNull(Names.justTheName(null));
        assertEquals("", Names.justTheName(""));
        assertEquals("abc", Names.justTheName("abc"));
        assertEquals("abc", Names.justTheName("com.abc"));
        assertEquals("abc", Names.justTheName("com.package.abc"));

        // Restore the previous setting.
        prefs.putBoolean("shortClassNames", old);
    }
}
