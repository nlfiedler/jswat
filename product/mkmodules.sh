#!/bin/sh
#
# Shell script to generate the list of modules that are to be disabled
# in the ideN cluster of NetBeans IDE. This is used to set the list of
# 'disabled.modules' in the nbproject/platform.properties file for the
# JSwat "product", which is based on NetBeans Platform, plus IDE some
# of the modules.
#
# The golden list of modules required by JSwat is in modules.txt.
#
# $Id: mkmodules.sh 2714 2007-01-04 09:41:23Z nfiedler $
#

# Argument should be the path where NetBeans module config files are found.
# (e.g. /opt/netbeans-5.5/ide7/config/Modules/)
if [ -z "$1" ]; then
    echo "Missing required path for NetBeans module config files!"
    exit
fi
DIR=$1
ALL=/tmp/all_modules.txt

# Create the list of all modules found in NetBeans IDE cluster.
# Note that sed does not understand +, so must use * instead.
find $DIR -name "*.xml" -print |\
xargs grep -h "module name" |\
sed 's/^.*"\(.*\)">$/\1/' |\
sort > $ALL

# Now create the list of disabled modules.
cat modules.txt $ALL |\
sort |\
uniq -u |\
sed 's/\(^.*$\)/    \1,\\/'

rm -f $ALL
