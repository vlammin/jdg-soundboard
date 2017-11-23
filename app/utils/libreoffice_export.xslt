<?xml version="1.0" encoding="UTF-8"?>
<!--
  libreoffice_export.xslt
  JDGSoundboard

  Copyright (c) 2017 Vincent Lammin
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
   xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
   xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
   exclude-result-prefixes="office table text">

    <xsl:output method = "xml" indent = "yes" encoding = "UTF-8" omit-xml-declaration = "no"/>

    <xsl:template match="/">
        <sounds>
            <xsl:apply-templates select="//table:table"/>
        </sounds>
    </xsl:template>

    <xsl:template match="table:table">

        <xsl:for-each select="table:table-row">
            <xsl:if test="position() &gt; 1">
                <sound>
                    <xsl:for-each select="table:table-cell">
                        <xsl:choose>
                            <xsl:when test="position()=1">
                                <xsl:attribute name="category"><xsl:value-of select="text:p"/></xsl:attribute>
                            </xsl:when>
                            <xsl:when test="position()=2">
                                <xsl:attribute name="custom_id"><xsl:value-of select="text:p"/></xsl:attribute>
                            </xsl:when>
                            <xsl:when test="position()=3">
                                <xsl:attribute name="new"><xsl:value-of select="text:p"/></xsl:attribute>
                            </xsl:when>
                            <xsl:when test="position()=4">
                                <xsl:attribute name="resource"><xsl:value-of select="text:p"/></xsl:attribute>
                            </xsl:when>
                            <xsl:when test="position()=5">
                                <xsl:attribute name="title"><xsl:value-of select="text:p"/></xsl:attribute>
                            </xsl:when>
                            <xsl:when test="position()=6">
                                <xsl:attribute name="video-youtube-id"><xsl:value-of select="text:p"/></xsl:attribute>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:for-each>
                </sound>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>