@echo off
REM The contents of this file are subject to the terms of the Common Development
REM and Distribution License (the License). You may not use this file except in
REM compliance with the License.
REM
REM You can obtain a copy of the License at http://www.netbeans.org/cddl.html
REM or http://www.netbeans.org/cddl.txt.

REM When distributing Covered Code, include this CDDL Header Notice in each file
REM and include the License file at http://www.netbeans.org/cddl.txt.
REM If applicable, add the following below the CDDL Header, with the fields
REM enclosed by brackets [] replaced by your own identifying information:
REM "Portions Copyrighted [year] [name of copyright owner]"
REM
REM The Original Software is the JSwat launcher. The Initial Developer of the
REM Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
REM are Copyright (C) 2005. All Rights Reserved.
REM
REM Contributor(s): Nathan L. Fiedler.
REM
REM $Id: jpdalaunch.cmd 2692 2006-12-24 10:40:51Z nfiedler $
REM

REM
REM This script is to be used as the argument for the 'launch' sub-option
REM in the JPDA connector arguments. For example:
REM
REM -agentlib:jdwp=transport=dt_socket,server=y,launch=c:\bin\jpdalaunch.cmd
REM
REM See the JPDA Connection and Invocation Details for more information.
REM

REM Argument 1 is the transport (e.g. dt_socket)
REM Argument 2 is the address (e.g. localhost:5000)

%~dp0\jswat -J-Djswat.transport=%1 -J-Djswat.address=%2
