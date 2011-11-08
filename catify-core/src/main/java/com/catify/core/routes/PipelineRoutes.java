package com.catify.core.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.event.impl.beans.PayloadEvent;
import com.catify.core.process.ProcessDeployer;

public class PipelineRoutes extends RouteBuilder {

	static final Logger LOG = LoggerFactory.getLogger(ProcessDeployer.class);
	static final LoggingLevel DEBUG = LoggingLevel.DEBUG;
	
	@Override
	public void configure() throws Exception {
		
		from("direct:savePayload")
		.routeId("savePayload")
		.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}", MessageConstants.INSTANCE_ID)))
    	.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
    	.bean(PayloadEvent.class)
    	.to(String.format("hazelcast:%s%s", HazelcastConstants.MAP_PREFIX, CacheConstants.PAYLOAD_CACHE));

	}

}
