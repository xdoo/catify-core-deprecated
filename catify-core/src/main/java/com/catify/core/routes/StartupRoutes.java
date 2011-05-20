package com.catify.core.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.CacheConstants;
import com.catify.core.process.ProcessDeployer;

public class StartupRoutes extends RouteBuilder {

	static final Logger LOG = LoggerFactory.getLogger(ProcessDeployer.class);
	static final LoggingLevel INFO = LoggingLevel.INFO;
	
	@Override
	public void configure() throws Exception {
		
		from("timer://startup?delay=5000&repeatCount=1")
		.routeId("startup_initializer").startupOrder(100)
		.log(INFO, "initializing node startup.")
		.to("seda://load_process_definitions");
		
		from("seda://load_process_definitions")
		.routeId("load_process_definitions").startupOrder(101)
		.log(INFO, "loading process definitions")
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.QUERY_OPERATION))
		.setHeader(HazelcastConstants.QUERY, constant("processId LIKE '%'"))
		.toF("hazelcast:%s:%s", HazelcastConstants.MAP_PREFIX, CacheConstants.PROCESS_CACHE)
		.split(body())
			.log(INFO, "starting to deploy process.")
			.processRef("processDeploymentProcessor");
		
	}

}
