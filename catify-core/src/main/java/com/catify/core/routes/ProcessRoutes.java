package com.catify.core.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;
import com.catify.core.event.impl.beans.StateEvent;

public class ProcessRoutes extends RouteBuilder {
	
	//cache
	private String hazelcastNodeCache = String.format("hazelcast:%s%s", 
			HazelcastConstants.MAP_PREFIX,
			CacheConstants.NODE_CACHE);

	@Override
	public void configure() throws Exception {

		//=============================================
		// helper
		//=============================================
		
		//put operation
		from("direct://set_put_headers")
		.routeId("set_put_headers")
		.to("direct://set_state_id")
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION));
		
		from("direct://set_state_id")
		.routeId("set_state_id")
		.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}-${header.%s}", MessageConstants.INSTANCE_ID, MessageConstants.TASK_ID)));
		
		//=============================================
		// states
		//=============================================
		
		//ready (1)
		from("direct:ready")
		.routeId("readyState")
		.to("direct://set_put_headers")
		.setHeader(ProcessConstants.STATE, constant(ProcessConstants.STATE_READY))
		.bean(StateEvent.class)
		.to(hazelcastNodeCache);
		
		//working (2)
		from("direct:working")
		.routeId("workingState")
		.to("direct://set_put_headers")
		.setHeader(ProcessConstants.STATE, constant(ProcessConstants.STATE_WORKING))
		.bean(StateEvent.class)
		.to(hazelcastNodeCache);
		
		//waiting (3)
		from("direct:waiting")
		.routeId("waitingState")
		.to("direct://set_put_headers")
		.setHeader(ProcessConstants.STATE, constant(ProcessConstants.STATE_WAITING))
		.bean(StateEvent.class)
		.to(hazelcastNodeCache);
		
		//done (4)
		from("direct:done")
		.routeId("doneState")
		.to("direct://set_put_headers")
		.setHeader(ProcessConstants.STATE, constant(ProcessConstants.STATE_DONE))
		.bean(StateEvent.class)
		.to(hazelcastNodeCache);
		
		//destroy state
		from("direct:destroy")
		.routeId("destroyState")
		.to("direct://set_state_id")
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.DELETE_OPERATION))
		.to(hazelcastNodeCache);
		
		from("seda://destroy")
		.routeId("destroyState_async")
		.to("direct:destroy");
		
		from("direct://destroy_with_given_id")
		.routeId("destroy_with_given_id")
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.DELETE_OPERATION))
		.to(hazelcastNodeCache);
		
		//get state
		from("direct:getState")
		.routeId("getState")
		.to("direct://set_state_id")
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.GET_OPERATION))
		.to(hazelcastNodeCache);
		
		//get state for instance
		from("direct:getStateForInstance")
		.routeId("getStateForInstance")
		.setHeader(HazelcastConstants.QUERY, simple(String.format("instanceId = '${header.%s}'", MessageConstants.INSTANCE_ID)))
		.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.QUERY_OPERATION))
		.to(hazelcastNodeCache);
		
		//check for no state
		from("direct:checkForNoState")
		.routeId("checkForNoState")
		.to("direct:getStateForInstance")
		.setBody(simple("${body.size}"))
//		.to("log://STATE?showAll=true")
		.choice()
			.when(body().isGreaterThan(0))
				.setBody(constant(false))
			.otherwise()
				.setBody(constant(true));

	}

}
