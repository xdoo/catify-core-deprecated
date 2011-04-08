package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public abstract class BaseProcessor implements Processor {

	/**
	 * copies all 'in' headers to the 'out' property
	 * 
	 * @param ex
	 */
	protected void copyHeaders(Exchange ex){
		ex.getOut().setHeaders(ex.getIn().getHeaders());
	}
	
	/**
	 * copies the 'in' body to the 'out' body property
	 * 
	 * @param ex
	 */
	protected void copyBody(Exchange ex){
		ex.getOut().setBody(ex.getIn().getBody());
	}
	
	/**
	 * copies both - body and headers - from 'in'
	 * to 'out'
	 * 
	 * @param ex
	 */
	protected void copyBodyAndHeaders(Exchange ex){
		this.copyHeaders(ex);
		this.copyBody(ex);
	}
	
}
