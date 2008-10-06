<?xml version='1.0' encoding='ISO-8859-1'?>
<!--
                      Sun Public License Notice.

  The contents of this file are subject to the Sun Public License
  Version 1.0 (the "License"); you may not use this file except in
  compliance with the License. A copy of the License is available at
  http://www.sun.com/

  The Original Code is the JSwat Help module. The Initial Developer of the
  Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
  are Copyright (C) 2004-2006. All Rights Reserved.

  Contributor(s): Nathan L. Fiedler.

  $Id: jswat.hs 2514 2006-06-03 07:25:56Z nfiedler $
-->
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
         "http://java.sun.com/products/javahelp/helpset_2_0.dtd">

<helpset version="2.0">

    <title>JSwat - Help</title>

    <maps>
        <homeID>jswat-welcome</homeID>
        <mapref location="jswat-map.xml"/>
    </maps>

    <view>
        <name>TOC</name>
        <label>Contents</label>
        <type>javax.help.TOCView</type>
        <data>jswat-toc.xml</data>
    </view>

    <view>
        <name>Index</name>
        <label>Index</label>
        <type>javax.help.IndexView</type>
        <data>jswat-idx.xml</data>
    </view>

    <view>
        <name>Search</name>
        <label>Search</label>
        <type>javax.help.SearchView</type>
        <data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</data>
    </view>
</helpset>
