<?xml version="1.0" encoding="UTF-8"?>
<!--
 * WordML2DTBook
 * Copyright © 2006 The Swedish Library of Talking Books and Braille, TPB (www.tpb.se)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 -->
<!--
		Pagenum fix
		Removes
			* empty (or nested) p
			* otherwise empty p or li around pagenum
		Moves
			* pagenum inside h[x] before h[x]
			* pagenum inside a word after the word

		Joel Håkansson, TPB
		Version 2007-02-20
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="dtb">

	<xsl:include href="./modules/recursive-copy.xsl"/>
	<xsl:include href="./modules/output.xsl"/>

 <!-- pagenum-fix -->
	<xsl:template match="dtb:p[count(text())=0 and 
						((count(descendant::dtb:pagenum)=count(descendant::*)) or 
						 (count(descendant::dtb:p)=      count(descendant::*)))]">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="dtb:li[count(text())=0 and 
						((count(descendant::dtb:pagenum)=count(descendant::*)))]">
		<xsl:apply-templates/>
	</xsl:template>
 <!-- /pagenum-fix -->
	
 <!-- move pagenum -->
	<xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6|dtb:hd">
		<!-- Move all pagenums inside hx before hx -->
		<xsl:for-each select="descendant::dtb:pagenum">
			<xsl:copy-of select="."/>
		</xsl:for-each>
		<xsl:call-template name="copy"/>
	</xsl:template>
	
	<!-- Ignore pagenums inside hx, they are processed above -->
	<xsl:template match="dtb:pagenum[ancestor::dtb:h1|ancestor::dtb:h2|ancestor::dtb:h3|
	ancestor::dtb:h4|ancestor::dtb:h5|ancestor::dtb:h6|ancestor::dtb:hd]"/>

	<!-- Ignore these text nodes, they are processed below -->
	<xsl:template match="text()[preceding-sibling::node()[1][self::dtb:pagenum] and
							preceding-sibling::node()[2][self::text()] and
not(ancestor::dtb:h1|ancestor::dtb:h2|ancestor::dtb:h3|ancestor::dtb:h4|ancestor::dtb:h5|ancestor::dtb:h6|ancestor::dtb:hd)]"/>
							
	<!-- Process pagenum with text nodes on both sides -->
	<xsl:template match="dtb:pagenum[preceding-sibling::node()[1][self::text()] and 
							 following-sibling::node()[1][self::text()] and
not(ancestor::dtb:h1|ancestor::dtb:h2|ancestor::dtb:h3|ancestor::dtb:h4|ancestor::dtb:h5|ancestor::dtb:h6|ancestor::dtb:hd)]">
		<xsl:variable name="A1" select="following-sibling::node()[1]"/>
		<xsl:variable name="A2" select="preceding-sibling::node()[1]"/>
		<xsl:choose>
			<!-- 
          ends-with: substring($A, string-length($A) - string-length($B) + 1) = $B
                     Se XSLT programmers reference, second edition, Michael Kay, sidan 541
      -->
			<!-- 
         Om föregående textnod slutar med mellanslag eller om nästkommande textnod börjar med mellanslag
         så ska denna tagg inte flyttas.
      -->
			<xsl:when test="starts-with($A1, ' ') or substring($A2, string-length($A2))=' '">
				<xsl:call-template name="copy"/>
				<xsl:value-of select="$A1"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="contains($A1,' ')">
						<xsl:value-of select="substring-before($A1,' ')"/>
						<xsl:call-template name="copy"/>
						<xsl:value-of select="concat(' ',substring-after($A1,' '))"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$A1"/>
						<xsl:call-template name="copy"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
 <!-- /move pagenum -->
	
</xsl:stylesheet>