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
# $Id: mkmodules.sh 2800 2007-02-13 07:37:41Z nfiedler $
#

# Argument should be the path where NetBeans is installed.
if [ -z "$1" ]; then
    echo "Missing required path for NetBeans module config files!"
    exit
fi
DIR=$1
ALL=/tmp/all_modules.txt

# Create the list of all modules found in NetBeans IDE clusters.
# Note that sed does not understand +, so must use * instead.
find "$DIR/ide9/config/Modules" "$DIR/java2/config/Modules" -name "*.xml" -print0 |\
xargs -0 grep -h "module name" |\
sed 's/^.*"\(.*\)">$/\1/' |\
sort > $ALL

# Now create the list of disabled modules.
cat modules.txt $ALL |\
sort |\
uniq -u |\
sed 's/\(^.*$\)/    \1,\\/'

rm -f $ALL
