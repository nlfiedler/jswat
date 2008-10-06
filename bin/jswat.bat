@echo off

REM Copyright (c) 2003 Nathan Fiedler
REM $Id: jswat.bat 1099 2003-12-10 03:44:48Z nfiedler $

if "%OS%"=="Windows_NT" @setlocal

REM Use the expanded path to this batch file as the default home.
if "%JSWAT_HOME%"=="" set JSWAT_HOME=%~dp0

REM Retrieve the arguments to JSwat.
set JSWAT_ARGS=%1
if ""%1""=="""" goto checkJSwat
shift
:setupArgs
if ""%1""=="""" goto checkJSwat
set JSWAT_ARGS=%JSWAT_ARGS% %1
shift
goto setupArgs

:checkJSwat
if exist "%JSWAT_HOME%\jswat.jar" goto checkJava
echo Set JSWAT_HOME to the location of the JSwat files.
echo JSWAT_HOME = %JSWAT_HOME%
goto end

:checkJava
set _JAVACMD=%JAVACMD%
set LOCALCLASSPATH=%CLASSPATH%
for %%i in ("%JSWAT_HOME%\*.jar") do call "%JSWAT_HOME%\buildcp.bat" %%i

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_HOME%\lib\tools.jar" set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%LOCALCLASSPATH%
goto runJSwat

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo Warning: JAVA_HOME environment variable is not set; JSwat may fail
echo to find the JPDA classes, in which case you should set JAVA_HOME
echo to the installation directory of the JDK.

:runJSwat
"%_JAVACMD%" -classpath "%LOCALCLASSPATH%" com.bluemarsh.jswat.Main %JSWAT_ARGS%
goto end

:end
set LOCALCLASSPATH=
set _JAVACMD=
set JSWAT_ARGS=

if "%OS%"=="Windows_NT" @endlocal
