#!/bin/sh
#
# Shell script to generate the list of modules that are to be disabled
# in the ide* cluster of NetBeans IDE. This is used to set the list of
# 'disabled.modules' in the nbproject/platform.properties file for the
# JSwat "product", which is based on NetBeans Platform, plus some of
# the IDE modules.
#
# The list of modules required by JSwat is in modules.txt.
#
# $Id$
#

# Argument should be the path where NetBeans is installed.
if [ -z "$1" ]; then
    echo "Missing required path for NetBeans module config files!"
    exit
fi
BASE=$1

script=`basename $0`
ALL=`mktemp -t ${script}` || exit 1

# Create the list of all modules found in NetBeans IDE clusters.
# Note that sed does not understand +, so must use * instead.
IDE=$BASE/ide11/config/Modules
JAVA=$BASE/java2/config/Modules
find "$IDE" "$JAVA" -name "*.xml" -print0 |\
    xargs -0 grep -h "module name" |\
    sed 's/^.*"\(.*\)">$/\1/' |\
    sort > $ALL

# Now create the list of disabled modules.
cat modules.txt $ALL |\
sort |\
uniq -u |\
sed 's/\(^.*$\)/    \1,\\/'

rm -f $ALL
