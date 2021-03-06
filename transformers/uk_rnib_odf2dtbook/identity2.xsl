<?xml version="1.0" encoding="utf-8"?>

<!-- The Identity Transformation -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
  <xsl:output method="xml" indent="yes"/>
<xsl:template match="*">
  <xsl:copy>
    <xsl:apply-templates select="@*" />
    <xsl:apply-templates />
  </xsl:copy>
</xsl:template>


<xsl:template match="@*">
  <xsl:copy-of select="." />
</xsl:template>



</xsl:stylesheet>

