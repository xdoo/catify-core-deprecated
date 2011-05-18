package com.catify.core.routes;

import org.apache.camel.builder.RouteBuilder;

import com.catify.core.constants.MessageConstants;
import com.catify.core.event.impl.MongoDbTimerEventService;
import com.catify.core.process.processors.TaskInstanceIdProcessor;

public class EventRoutes extends RouteBuilder {
	
	
	@Override
	public void configure() throws Exception {
		
		//=============================================
		// timer events
		//=============================================
		
		//set timer event
		//-----
		from("direct:set-timer-event")
		.routeId("set-timer-event")
		.beanRef("timerEventService", "register");
		
		//delete timer event
		//-----
		from("direct:delete-timer-event")
		.routeId("delete-timer-event")
		.beanRef("timerEventService", "unregister");

		//fire timer event for a given time
		//-----
		from("timer://event?fixedRate=true&period=1000")
		.routeId("fire-timer-event")
		.beanRef("timerEventService", "fire")
		.split(body())
		.to("seda:create-timer-event-message");
		
		//create event message
		//-----
		from("seda:create-timer-event-message?concurrentConsumers=5")
		.routeId("create-timer-event-message")
		.setHeader(MessageConstants.INSTANCE_ID, simple("${body[0]}"))
		.setHeader(MessageConstants.TASK_ID, simple("event_${body[1]}"))
		.process(new TaskInstanceIdProcessor())
		//------------
		//we use here a routing slip because we need a 
		//simple and flexible way to route the event messages
		//to the different queues
		//------------
		.setHeader("event-routing", simple("hazelcast:seda:event_${body[1]}"))
		.setBody(constant(""))
		.routingSlip("event-routing");

	}

}
