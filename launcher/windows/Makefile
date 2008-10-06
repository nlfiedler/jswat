# -*- Makefile -*-
#
#                     Sun Public License Notice.
#
# The contents of this file are subject to the Sun Public License
# Version 1.0 (the "License"); you may not use this file except in
# compliance with the License. A copy of the License is available at
# http://www.sun.com/
#
# The Original Code is the JSwat Core module. The Initial Developer of the
# Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
# are Copyright (C) 2005. All Rights Reserved.
#
# Contributor(s): Nathan L. Fiedler.
#
# $Id: $
#

CFLAGS = -s -mno-cygwin

all : clean jswat.exe jswatw.exe

jswat.exe : jswat.cpp jswat.res
	gcc $(CFLAGS) -mwindows -DWINMAIN jswat.cpp -o jswat.exe jswat.res

jswatw.exe : jswat.cpp
	gcc $(CFLAGS) jswat.cpp -o jswatw.exe

jswat.res : jswat.rc
	windres -o$@ -Ocoff jswat.rc

clean :
	rm -f *.res *.obj *.exe 2>NUL
