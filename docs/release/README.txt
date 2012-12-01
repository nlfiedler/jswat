JSwat Debugger README
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
JSwat is a Java debugger front-end, written for the Java Platform,
utilizing the Java Platform Debugger Architecture (JPDA). The source
code is licensed under the Common Development and Distribution License.

Requirements
----------------------------------------------------------------------
JSwat requires the Java Development Kit (JDK) version 6.0 or higher,
along with the JPDA library. The JPDA classes are defined in the
com.sun.jdi package and generally found in a file called tools.jar
somewhere in the JDK, usually in the lib folder. The JDK on Mac OS X
includes the necessary classes in the default classpath so no extra
steps are needed to start JSwat.

Starting JSwat
----------------------------------------------------------------------
To start JSwat when the JPDA classes are included in the default
classpath, invoke the java command like so:

   java -jar com-bluemarsh-jswat-console.jar

If you are using a system in which the JPDA classes are not in the
default classpath, then you will need to ensure the necessary classes
are included. Typically the JPDA classes are in the JDK lib directory,
so starting JSwat as shown below should work (replace JDK_HOME with the
path to your JDK):

   java -Djava.ext.dirs=JDK_HOME/lib -jar com-bluemarsh-jswat-console.jar

If neither of these approaches work, you have an uncommon JDK and will
need to find where the com.sun.jdi package is located and make sure it
is included in the classpath. Another approach might look like this:

   java -Xbootclasspath/a:JDK_HOME/lib/tools.jar -jar \
        com-bluemarsh-jswat-console.jar

If all else fails, try the graphical version of JSwat which has an
intelligent launcher (a part of NetBeans) that finds the tools classes
and adds them to the classpath at runtime.

Documentation
----------------------------------------------------------------------
Information about JSwat commands can be found via the help command. Once
JSwat is running, typing 'help' at the prompt will provide guidance to
begin debugging your Java application.

JSwat supports several command line arguments, which can be found by
starting JSwat with a -h or --help argument. Some further explanation is
provided below.

Attaching to a port: this tells JSwat that it should connect to a
debuggee that is waiting for a connection from a debugger on the port
given following the -attach argument.

Setting the sourcepath: this sets the path which JSwat uses to find
source files. The sourcepath is identical in format to the classpath,
with the difference being that it is used to find .java files instead of
.class files. The same rules apply to finding source code as to finding
byte code. Source code may be located in an archive (.jar or .zip) or in
a directory structure, just as the compiled classes would be.

Command Prompt
----------------------------------------------------------------------
The JSwat command prompt (>) may become separated from the input cursor
if output is displayed from a background thread (i.e. not output from
a command invocation). This is harmless and typing a command will work
as usual. If you like, you can press Enter to get a prompt again.

Stopping JSwat
----------------------------------------------------------------------
Typing the 'exit' command should do the trick. If not, try Ctrl-c. If
neither option works, then you may need to terminate the java process
for the debuggee that is attached to JSwat (if the debuggee freezes, so
does JSwat, an unfortunate situation due to socket communcations used
between the debugger front-end and back-end). If that does not help,
then terminate the JSwat process.

Removing JSwat
----------------------------------------------------------------------
To remove JSwat from your system, simply remove the installation
directory, which contains the jar files and this README. You may also
want to remove the settings directory, located in your user home
directory, and typically named .jswat-console.

Getting help
----------------------------------------------------------------------
For help on using JSwat, write an email to the mailing list
jswat-discuss@googlegroups.com, which can also be found on the web at
http://groups.google.com/group/jswat-discuss

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
JSwat and related documentation are Copyright (C) 1999-2012
