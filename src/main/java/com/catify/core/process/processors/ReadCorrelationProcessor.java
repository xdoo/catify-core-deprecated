package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.apache.camel.component.hazelcast.HazelcastConstants;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.hazelcast.core.Hazelcast;

public class ReadCorrelationProcessor extends CorrelationProcessor {

	@Override
	public void process(Exchange ex) throws Exception {

		super.generateCorrelationId(ex);

		// get instance id from hazelcast
		String instanceId = (String) Hazelcast.getMap(
				CacheConstants.CORRELATION_CACHE).get(
				ex.getOut().getHeader(HazelcastConstants.OBJECT_ID));
		
		//put it into the message
		ex.getOut().setHeader(MessageConstants.INSTANCE_ID, instanceId);
	}

}
