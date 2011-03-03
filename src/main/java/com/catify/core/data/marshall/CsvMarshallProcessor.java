package com.catify.core.data.marshall;

import java.util.regex.Pattern;

import org.apache.camel.Exchange;

import com.catify.core.process.processors.BaseProcessor;

public class CsvMarshallProcessor extends BaseProcessor{

	public void process(Exchange ex) throws Exception {
		
		String body = ex.getIn().getBody(String.class);
		
		ex.getOut().setBody(this.splitBody(body, ";"));
		
		super.copyHeaders(ex);
		
	}
	
	private String splitBody(String body, String delimiter){
		
		StringBuilder result = new StringBuilder("<set>");
		
		String[] strings = body.split(Pattern.quote( delimiter ));
		int cnt = strings.length;
		for (int i = 0; i < cnt; i++) {
			String string = strings[i];
			result.append("<column_").append(i).append(">").append(string).append("</column_").append(i).append(">");
		}
		
		result.append("</set>");
		
		return result.toString();
	}

}
