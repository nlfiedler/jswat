/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * MODULE:      Breakpoints
 * FILE:        ReferenceTypeSpec.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/30/99        Initial version
 *      nf      02/25/01        Added getIdentifier() and isExact()
 *      nf      04/29/01        Removed equals()
 *      nf      06/14/01        Extends Serializable
 *
 * DESCRIPTION:
 *      This file defines the abstract class for all reference type
 *      specifications.
 *
 * $Id: ReferenceTypeSpec.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.ClassPrepareRequest;
import java.io.Serializable;

/**
 * Defines the interface for all reference type specifications.
 *
 * @author  Nathan Fiedler
 */
public interface ReferenceTypeSpec extends Serializable {
    /** serial version */
    static final long serialVersionUID = 6780914391435488964L;

    /**
     * Create a class prepare request appropriate for this
     * reference type specification.
     *
     * @param  vm  VirtualMachine to use for creating request.
     * @return  ClassPrepareRequest.
     */
    public ClassPrepareRequest createPrepareRequest(VirtualMachine vm);

    /**
     * Returns the class identifier this specification is specifying.
     *
     * @return  Class identifier.
     */
    public String getIdentifier();

    /**
     * Returns true if this type specification is an exact name
     * or uses a wildcard pattern.
     *
     * @return  True if exact, false if wildcard.
     */
    public boolean isExact();

    /**
     * Determines if the given class name matches this specification.
     *
     * @param  classname  name of class to match against.
     * @return  true if name matches this specification.
     */
    public boolean matches(String classname);

    /**
     * Determines if the given ReferenceType matches this specification.
     *
     * @param  refType  ReferenceType to match against.
     * @return  true if type matches this specification.
     */
    public boolean matches(ReferenceType refType);
} // ReferenceTypeSpec
