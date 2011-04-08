package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.apache.commons.codec.digest.DigestUtils;

public abstract class CorrelationProcessor extends BaseProcessor {

	public abstract void process(Exchange ex) throws Exception;
	
	protected void generateCorrelationId(Exchange ex){
		
//		creates a md5 hash as correlation id for the given string
		String correlationId = DigestUtils.md5Hex(ex.getIn().getBody(String.class));
		
		super.copyBodyAndHeaders(ex);
		
		ex.getOut().setHeader(HazelcastConstants.OBJECT_ID, correlationId);
	}

}
