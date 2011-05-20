package com.catify.core.process.xml;

import java.util.Iterator;
import java.util.List;

public class CorrelationRuleBuilder {

	public String buildCorrelationDefinition(List<String> xpath){
		StringBuilder builder = new StringBuilder();
		
		if(xpath == null || xpath.size() == 0){
			throw new IllegalArgumentException("To create a correlation definition you need at least one xpath.");
		}
		
		//first part of the transformation file
		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" exclude-result-prefixes=\"xs\">\n" +
						"\t<xsl:output method=\"xml\" encoding=\"UTF-8\" indent=\"yes\"/>\n" +
						"\t<xsl:template match=\"/\">\n" +
						"\t\t<xsl:variable name=\"var1_instance\" select=\".\"/>\n" +
						"\t\t<correlation>\n" +
						"\t\t\t<xsl:value-of select=\"concat(");
		
		Iterator<String> it = xpath.iterator();
		while (it.hasNext()) {
			builder.append(String.format("string($var1_instance%s)", it.next()));
			if(it.hasNext()){
				builder.append(", ");
			}
		}
		
		builder.append(")\"/>\n" +
						"\t\t</correlation>\n" +
						"\t</xsl:template>\n" +
						"</xsl:stylesheet>");
		
		return builder.toString();
	}
	
}
