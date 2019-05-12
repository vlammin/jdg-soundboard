<?xml version="1.0" encoding="UTF-8"?>
<!--
  libreoffice_path_export.xslt
  JDGSoundboard

  Copyright (c) 2019 Vincent Lammin
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
    xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
    exclude-result-prefixes="office table text">

    <xsl:output method = "xml" indent = "yes" encoding = "UTF-8" omit-xml-declaration = "no"/>

    <xsl:template match="/">
        <paths xmlns:android="http://schemas.android.com/apk/res/android">
            <xsl:apply-templates select="//table:table"/>
        </paths>
    </xsl:template>

    <xsl:template match="table:table">

        <xsl:for-each select="table:table-row">
            <xsl:if test="position() &gt; 1">
                <raw-resource>
                    <xsl:for-each select="table:table-cell">
                        <xsl:choose>
                            <xsl:when test="position()=4">
                                <xsl:attribute name="name">jdg_<xsl:value-of select="substring(text:p, 6)"/>.mp3</xsl:attribute>
                                <xsl:attribute name="path"><xsl:value-of select="substring(text:p, 6)"/></xsl:attribute>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:for-each>
                </raw-resource>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>