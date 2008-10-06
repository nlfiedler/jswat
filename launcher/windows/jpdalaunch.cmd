@echo off
REM                 Sun Public License Notice
REM
REM The contents of this file are subject to the Sun Public License
REM Version 1.0 (the "License"); you may not use this file except in
REM compliance with the License. A copy of the License is available at
REM http://www.sun.com/
REM
REM The Original Code is the JSwat launcher. The Initial Developer of the
REM Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
REM are Copyright (C) 2005. All Rights Reserved.
REM
REM Contributor(s): Nathan L. Fiedler.
REM
REM $Id: jpdalaunch.cmd 2161 2005-12-01 09:01:01Z nfiedler $
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
