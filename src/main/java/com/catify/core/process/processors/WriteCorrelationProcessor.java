package com.catify.core.process.processors;

import org.apache.camel.Exchange;

import com.catify.core.constants.MessageConstants;

public class WriteCorrelationProcessor extends CorrelationProcessor {

	public void process(Exchange ex) throws Exception {
		
		super.generateCorrelationId(ex);
		
		//the hazelcast component need the instance id as body
		ex.getOut().setBody(ex.getIn().getHeader(MessageConstants.INSTANCE_ID, String.class));
		
	}

}
