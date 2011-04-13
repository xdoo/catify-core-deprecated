package com.catify.core.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;

public class ProcessRoutes extends RouteBuilder {
	
	//cache
	private String hazelcastNodeCache = String.format("hazelcast:%s%s", 
			HazelcastConstants.MAP_PREFIX,
			CacheConstants.NODE_CACHE);

	@Override
	public void configure() throws Exception {

		
		//=============================================
		// states
		//=============================================
		
		//ready (1)
		from("direct:ready")
		.routeId("readyState")
		.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}", MessageConstants.TASK_INSTANCE_ID)))
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
		.setBody(constant(ProcessConstants.STATE_READY))
		.to(hazelcastNodeCache);
		
		//working (2)
		from("direct:working")
		.routeId("workingState")
		.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}", MessageConstants.TASK_INSTANCE_ID)))
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
		.setBody(constant(ProcessConstants.STATE_WORKING))
		.to(hazelcastNodeCache);
		
		//waiting (3)
		from("direct:waiting")
		.routeId("waitingState")
		.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}", MessageConstants.TASK_INSTANCE_ID)))
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
		.setBody(constant(ProcessConstants.STATE_WAITING))
		.to(hazelcastNodeCache);
		
		//done (4)
		from("direct:done")
		.routeId("doneState")
		.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}", MessageConstants.TASK_INSTANCE_ID)))
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
		.setBody(constant(ProcessConstants.STATE_DONE))
		.to(hazelcastNodeCache);
		
		//destroy state
		from("direct:destroy")
		.routeId("destroyState")
		.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}", MessageConstants.TASK_INSTANCE_ID)))
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.DELETE_OPERATION))
		.to(hazelcastNodeCache);
		
		//get state
		from("direct:getState")
		.routeId("getState")
		.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}", MessageConstants.TASK_INSTANCE_ID)))
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.GET_OPERATION))
		.to(hazelcastNodeCache);
		

	}

}
