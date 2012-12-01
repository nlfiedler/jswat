#!/bin/bash
#
# Locate the modules within the NetBeans installation. Used to create
# the list of files to be included in the build script of JSwat.
#
# The list of modules required by JSwat is in modules.txt.
#

# Argument should be the path where NetBeans is installed.
if [ -z "$1" ]; then
    echo "Missing required path to NetBeans module config files!"
    exit
fi
BASE=$1

# Get a temporary file for collecting the output.
script=`basename $0`
RESULTS=`mktemp -t ${script}` || exit 1
if [ -f modules.txt ]; then
    MODS='modules.txt'
elif [ -d product ]; then
    MODS='product/modules.txt'
fi

# Find the module jars...
for MODULE in `sed -e 's/\./-/g; s/$/.jar/' $MODS`; do
    # Is there a better way to get the line count and nothing else?
    LC1=`wc -l $RESULTS | sed -e 's/^[ \t]*//' | cut -d ' ' -f 1`
    find "$BASE" -name $MODULE >> $RESULTS
    LC2=`wc -l $RESULTS | sed -e 's/^[ \t]*//' | cut -d ' ' -f 1`
    # Make sure find actually found something for this module.
    if [ $LC1 = $LC2 ]; then
        echo "$MODULE not found!"
    fi
done

# Simplify the results for use in the build script.
sed -e "s|^$BASE/||" $RESULTS | sort

# Create a new temporary file for the XML results.
rm -f $RESULTS
RESULTS=`mktemp -t ${script}` || exit 1

# Find the module configuration files...
for MODULE in `sed -e 's/\./-/g; s/$/.xml/' $MODS`; do
    # Is there a better way to get the line count and nothing else?
    LC1=`wc -l $RESULTS | sed -e 's/^[ \t]*//' | cut -d ' ' -f 1`
    find "$BASE" -path '*/config/Modules/*' -name $MODULE -print >> $RESULTS
    LC2=`wc -l $RESULTS | sed -e 's/^[ \t]*//' | cut -d ' ' -f 1`
    # Make sure find actually found something for this module.
    if [ $LC1 = $LC2 ]; then
        echo "$MODULE not found!"
    fi
done

# Simplify the results for use in the build script.
sed -e "s|^$BASE/||" $RESULTS | sort

rm -f $RESULTS
