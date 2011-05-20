package com.catify.core.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;

public class PipelineRoutes extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		from("direct:savePayload")
		.routeId("savePayload")
		.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}", MessageConstants.INSTANCE_ID)))
    	.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
    	.to(String.format("hazelcast:%s%s", HazelcastConstants.MAP_PREFIX, CacheConstants.PAYLOAD_CACHE));

	}

}
