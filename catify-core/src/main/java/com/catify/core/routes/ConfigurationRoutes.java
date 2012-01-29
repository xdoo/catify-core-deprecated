package com.catify.core.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.GlobalConstants;
import com.catify.core.constants.MessageConstants;

public class ConfigurationRoutes extends RouteBuilder {

	static final Logger LOG = LoggerFactory.getLogger(ConfigurationRoutes.class);
	static final LoggingLevel LEVEL = LoggingLevel.INFO;
	
	@Override
	public void configure() throws Exception {
		
		DataFormat jaxb = new JaxbDataFormat("com.catify.core.process.xml.model");
		
		//---------------------------------------------
		// processes
		//---------------------------------------------
		
		//create process definition and
		//put into process cache (that 
		//happens inside the processor).
		from("restlet:http://localhost:9080/catify/deploy_process?restletMethod=post")
		.routeId("put_process_into_cache")
		//TODO --> transformation + validation
		.unmarshal(jaxb)
		.processRef("processRegistrationProcessor")
		.marshal(jaxb);
		
		//listen to new process definitions
		fromF("hazelcast:%s%s", HazelcastConstants.MAP_PREFIX, CacheConstants.PROCESS_CACHE)
		.routeId("deploy_process")
		.log("process...")
		.choice()
			.when(header(HazelcastConstants.LISTENER_ACTION).isEqualTo(HazelcastConstants.ADDED))
				.log("...added to cache")
				.processRef("processDeploymentProcessor");
		
		//---------------------------------------------
		// pipelines
		//---------------------------------------------
		
		//put the pipeline definition into the cache
		from("restlet:http://localhost:9080/catify/deploy_pipeline/{nodeid}?restletMethod=post")
		.routeId("put_pipeline_into_cache")
		//TODO --> transform + validate
		//unmarshal
		.marshal().string("UTF-8")
		//put it into cache
		.setHeader(HazelcastConstants.OBJECT_ID, header("nodeid"))
        .setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
        .toF("hazelcast:%s%s", HazelcastConstants.MAP_PREFIX, CacheConstants.PIPELINE_CACHE);
		
		//listen to new pipeline definitions and deploy them
		fromF("hazelcast:%s%s", HazelcastConstants.MAP_PREFIX, CacheConstants.PIPELINE_CACHE)
		.routeId("deploy_pipeline")
		.log(	LEVEL,
				"DEPLOY PIPELINE",
				"Received message to deploy pipeline.")
		.choice()
			.when(header(HazelcastConstants.LISTENER_ACTION).isEqualTo(HazelcastConstants.ADDED))
			.processRef("pipelineDeploymentProcessor");
		
		//---------------------------------------------
		// validation
		//---------------------------------------------
		
		//put schema (xsd) into the cache
		from("restlet:http://localhost:9080/catify/deploy_schema/{nodeid}?restletMethod=post")
		.routeId("put_schema_into_cache")
		//TODO --> validate
		//unmarshal
		.marshal().string("UTF-8")
		//put it into the cache
		.setHeader(HazelcastConstants.OBJECT_ID, header("nodeid"))
        .setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
        .toF("hazelcast:%s%s", HazelcastConstants.MAP_PREFIX, CacheConstants.VALIDATION_CACHE);
		
		//get schema (xsd) out of the cache
		from("restlet:http://localhost:9080/catify/get_schema/{nodeid}?restletMethod=get")
		.routeId("read_schema")
		//read it from cache
		.setHeader(HazelcastConstants.OBJECT_ID, header("nodeid"))
        .setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.GET_OPERATION))
        .toF("hazelcast:%s%s", HazelcastConstants.MAP_PREFIX, CacheConstants.VALIDATION_CACHE);
		
		//---------------------------------------------
		// correlation
		//---------------------------------------------
		
		//put correlation rule (xslt) into the cache
		fromF("restlet:http://localhost:%s/catify/deploy_correlation_rule/{nodeid}?restletMethod=post", GlobalConstants.HTTP_PORT)
		.routeId("put_correlation_rule_into_cache")
		//TODO --> validate
		//unmarshal
		.marshal().string("UTF-8")
		//put it into the cache
		.setHeader(HazelcastConstants.OBJECT_ID, header("nodeid"))
        .setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
        .toF("hazelcast:%s%s", HazelcastConstants.MAP_PREFIX, CacheConstants.CORRELATION_RULE_CACHE);
		
		//get the correlation rule (xslt) out of the cache
		fromF("restlet:http://localhost:%s/catify/get_correlation_rule/{nodeid}?restletMethod=get", GlobalConstants.HTTP_PORT)
		.routeId("read_correlation_rule")
		//read it from cache
		.setHeader(HazelcastConstants.OBJECT_ID, header("nodeid"))
        .setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.GET_OPERATION))
        .toF("hazelcast:%s%s", HazelcastConstants.MAP_PREFIX, CacheConstants.CORRELATION_RULE_CACHE);
		
		//---------------------------------------------
		// caches
		//---------------------------------------------
		from("restlet:http://localhost:9080/catify/empty_all_caches?restletMethod=post")
		.routeId("clean_all_caches")
		.processRef("emptyCacheProcessor");

	}

}
