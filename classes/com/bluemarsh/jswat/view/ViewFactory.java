/*********************************************************************
 *
 *      Copyright (C) 2003 Nathan Fiedler
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
 * PROJECT:     JSwat
 * MODULE:      View
 * FILE:        ViewFactory.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/06/03        Initial version
 *
 * $Id: ViewFactory.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.lang.java.JavaSourceView;
import java.io.File;

/**
 * Class ViewFactory is a singleton that creates the appropriate
 * concrete implementation of a View. It does this based on the
 * arguments passed to one of the <code>create()</code> methods.
 *
 * @author  Nathan Fiedler
 */
public class ViewFactory {
    /** The one instance of this class. */
    private static ViewFactory theInstance;

    static {
        theInstance = new ViewFactory();
    }

    /**
     * This class cannot be instantiated.
     */
    private ViewFactory() {
    } // ViewFactory

    /**
     * Create an instance of a View based on the given source.
     *
     * @param  src  source view data.
     * @return  a new view instance.
     */
    public View create(SourceSource src) {
        View view = null;
        if (src.isByteCode()) {
            view = new ByteCodeView(src);
        } else {
            String name = src.getName();
            if (name.toLowerCase().endsWith(".java")) {
                // It seems to be a Java source file.
                view = new JavaSourceView(src);
            } else {
                view = new SourceView(src);
            }
        }
        return view;
    } // create

    /**
     * Returns the instance of this class.
     *
     * @return  a ViewFactory instance.
     */
    public static ViewFactory getInstance() {
        return theInstance;
    } // getInstance
} // ViewFactory
