package com.catify.core.routes;

import org.apache.camel.builder.RouteBuilder;

import com.catify.core.constants.MessageConstants;
import com.catify.core.process.processors.TaskInstanceIdProcessor;


public class EventRoutes extends RouteBuilder {
	
	
	@Override
	public void configure() throws Exception {
		
		//=============================================
		// timer events
		//=============================================
		
		//set timer event
		//-----
		from("activemq:queue:set-timer-event")
		.transacted()
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
		from("seda:create-timer-event-message")
		.routeId("create-timer-event-message")
		.setHeader(MessageConstants.INSTANCE_ID, simple("${body[0]}"))
		.setHeader(MessageConstants.TASK_ID, simple("event_${body[1]}"))
		.setHeader(MessageConstants.ACCOUNT_NAME, simple("${body[2]}"))
		.setHeader(MessageConstants.PROCESS_NAME, simple("${body[3]}"))
		.setHeader(MessageConstants.PROCESS_VERSION, simple("${body[4]}"))
		.setHeader(MessageConstants.TASK_NAME, simple("${body[5]}"))
		.process(new TaskInstanceIdProcessor())
		//------------
		//we use here a routing slip because we need a 
		//simple and flexible way to route the event messages
		//to the different queues
		//------------
		.setHeader("event-routing", simple("activemq:queue:event.${body[2]}.${body[3]}.${body[4]}.${body[5]}?transacted=true"))
		.setBody(constant(""))
		.routingSlip("event-routing");

	}

}
