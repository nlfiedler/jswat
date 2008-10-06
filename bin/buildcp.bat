REM Copyright (c) 2003 Nathan Fiedler
REM $Id: buildcp.bat 1099 2003-12-10 03:44:48Z nfiedler $

set _CPPART=%1
if ""%1""=="""" goto gotAllArgs
shift

REM Arguments might have spaces in them.
:argCheck
if ""%1""=="""" goto gotAllArgs
set _CPPART=%_CPPART% %1
shift
goto argCheck

:gotAllArgs
set LOCALCLASSPATH=%LOCALCLASSPATH%;%_CPPART%
