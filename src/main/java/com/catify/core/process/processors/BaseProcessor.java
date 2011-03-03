package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public abstract class BaseProcessor implements Processor {

	protected void copyHeaders(Exchange ex){
		ex.getOut().setHeaders(ex.getIn().getHeaders());
	}
	
	protected void copyBody(Exchange ex){
		ex.getOut().setBody(ex.getIn().getBody());
	}
	
	protected void copyBodyAndHeaders(Exchange ex){
		this.copyHeaders(ex);
		this.copyBody(ex);
	}
	
}
