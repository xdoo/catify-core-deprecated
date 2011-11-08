package com.catify.core.process.routers;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;
import com.catify.core.event.impl.beans.StateEvent;
import com.catify.core.process.processors.TaskInstanceIdProcessor;
import com.catify.core.process.routers.impl.ReceiveRouter;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestReceiveRouter extends CamelTestSupport {

	private String instanceId = "123";
	private String taskId 	  = "4711";
	
	@Test
	public void testGo() throws Exception{
		context.addRoutes(getRoutes());
		String id = String.format("%s-%s", instanceId, taskId);
		
		MockEndpoint out = super.getMockEndpoint("mock:out-go");
		out.setExpectedMessageCount(1);
		
		IMap<String, StateEvent> map = Hazelcast.getMap(CacheConstants.NODE_CACHE);
		map.put(id, new StateEvent(instanceId, ProcessConstants.STATE_WAITING, id));
		
		template.sendBody("direct:start", "foo");
		
		out.assertIsSatisfied();
		
		map.remove(id);
	}
	
	@Test
	public void testWait() throws Exception{
		context.addRoutes(getRoutes());
		String id = String.format("%s-%s", instanceId, taskId);
		
		MockEndpoint out = super.getMockEndpoint("mock:out-wait");
		out.setExpectedMessageCount(1);
		
		IMap<String, StateEvent> map = Hazelcast.getMap(CacheConstants.NODE_CACHE);
		map.put(id, new StateEvent(instanceId, ProcessConstants.STATE_WORKING, id));
		
		template.sendBody("direct:start", "foo");
		
		out.assertIsSatisfied();
		
		assertTrue(map.containsKey(id));
		
		map.remove(id);
	}
	
	private RouteBuilder getRoutes(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:start")
				.setHeader(MessageConstants.INSTANCE_ID, constant(instanceId))
				.setHeader(MessageConstants.TASK_ID, constant(taskId))
				.process(new TaskInstanceIdProcessor())
				.dynamicRouter(bean(new ReceiveRouter(taskId)))
				.to("log:A");
				
				from("direct:go-" + taskId)
				.to("log:go")
				.to("mock:out-go");
				
				from("direct:wait-" + taskId)
				.to("log:wait")
				.setHeader(ReceiveRouter.WAIT, constant("wait"))
				.to("mock:out-wait");
				
			}
		};
		
	}

}
