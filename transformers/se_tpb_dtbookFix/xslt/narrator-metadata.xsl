<?xml version="1.0" encoding="UTF-8"?>
<!--
    Add dublin core metadata as required by the Narrator Script
    (these elements are not required by the core DTBook grammar).
    
    Version
    2008-03-31
    
    Description
    Adds dc:Language, dc:Date and dc:Publisher to dtbook, if not present in input,
    or given but with null/whitespace only content values.
    
    dc:Language is taken from xml:lang if set, else inparam
    dc:Date is taken as inparam
    dc:Publisher is taken as inparam
    
    Nodes
    dtbook/head
    
    Namespaces
    (x) "http://www.daisy.org/z3986/2005/dtbook/"
    
    Doctype
    (x) DTBook
    
    Author
    Markus Gylling, DAISY
	
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" xmlns="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="dtb">
    
    <xsl:include href="recursive-copy2.xsl"/>
    <xsl:include href="output2.xsl"/>
    
    <xsl:param name="dateValue"/>
    <xsl:param name="langValue"/>
    <xsl:param name="publisherValue"/>
    

    <xsl:template match="dtb:head">
     
        <xsl:copy>
            <xsl:copy-of select="@*"/>
       
            <xsl:if test="count(dtb:meta[@name='dc:Date'])=0">
            	<xsl:message terminate="no">Adding dc:Date metadata element</xsl:message>
            	<xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Date</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:value-of select="$dateValue"/>
					</xsl:attribute>
				</xsl:element>
            </xsl:if>
            
            <xsl:if test="count(dtb:meta[@name='dc:Publisher'])=0">
            	<xsl:message terminate="no">Adding dc:Publisher metadata element</xsl:message>
            	<xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Publisher</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:value-of select="$publisherValue"/>
					</xsl:attribute>
				</xsl:element>
            </xsl:if>
                        
            <xsl:if test="count(dtb:meta[@name='dc:Language'])=0">
            	<xsl:message terminate="no">Adding dc:Language metadata element</xsl:message>
            	<xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Language</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:choose>
							<xsl:when test="//dtb:dtbook/@xml:lang">
								<xsl:value-of select="//dtb:dtbook/@xml:lang"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$langValue"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
				</xsl:element>
            
            </xsl:if>
        	<xsl:apply-templates/>         
        </xsl:copy>
    </xsl:template>
     
    <xsl:template match="dtb:meta[@name='dc:Publisher']">
    	<xsl:choose>
	    	<xsl:when test="translate(@content, ' ', '')=''">
	        	<xsl:message terminate="no">Adding value to empty dc:Publisher metadata element</xsl:message>
	            <xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Publisher</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:value-of select="$publisherValue"/>
					</xsl:attribute>
				</xsl:element>
	        </xsl:when>
	       	<xsl:otherwise>
	        	<xsl:copy-of select="."/>
	        </xsl:otherwise>
        </xsl:choose>        
    </xsl:template>
    
    <xsl:template match="dtb:meta[@name='dc:Language']">
    	<xsl:choose>
	    	<xsl:when test="translate(@content, ' ', '')=''">
	        	<xsl:message terminate="no">Adding value to empty dc:Language metadata element</xsl:message>
	            <xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Language</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
							<xsl:choose>
								<xsl:when test="//dtb:dtbook/@xml:lang">
									<xsl:value-of select="//dtb:dtbook/@xml:lang"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$langValue"/>
								</xsl:otherwise>
							</xsl:choose>
					</xsl:attribute>
				</xsl:element>
	        </xsl:when>
	        <xsl:otherwise>
	        	<xsl:copy-of select="."/>
	        </xsl:otherwise>	        
        </xsl:choose>        
    </xsl:template>
    
    <xsl:template match="dtb:meta[@name='dc:Date']">
    	<xsl:choose>
	    	<xsl:when test="translate(@content, ' ', '')=''">
	        	<xsl:message terminate="no">Adding value to empty dc:Date metadata element</xsl:message>
	            <xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Date</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:value-of select="$dateValue"/>
					</xsl:attribute>
				</xsl:element>
	        </xsl:when>   
	        <xsl:otherwise>
	        	<xsl:copy-of select="."/>
	        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>       
</xsl:stylesheet>
