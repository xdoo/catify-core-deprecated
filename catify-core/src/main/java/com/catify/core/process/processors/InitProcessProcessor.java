package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.MessageConstants;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IdGenerator;

public class InitProcessProcessor extends BaseProcessor {
	
	private IdGenerator idGenerator = Hazelcast.getIdGenerator("ProcessInstanceIdGenerator");
	
	static final Logger LOG = LoggerFactory
			.getLogger(InitProcessProcessor.class);
	
	public void process(Exchange exchange) throws Exception {
		
		super.copyBodyAndHeaders(exchange);
		
//		if there is an instance id set in the header create no new one
		if(!exchange.getIn().getHeaders().containsKey(MessageConstants.INSTANCE_ID)){
			exchange.getOut().setHeader(MessageConstants.INSTANCE_ID, Long.toString(idGenerator.newId()));
		}
		
		if(LOG.isDebugEnabled()){
			LOG.debug(String.format("Process instance with id '%s' initialized."), exchange.getOut().getHeader(MessageConstants.INSTANCE_ID, String.class));
		}
	}

}
