package com.catify.core.process.routers;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelTestSupport;
import org.apache.commons.codec.digest.DigestUtils;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.processors.TaskInstanceIdProcessor;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestReceiveRouter extends CamelTestSupport {
	
	public void testGo() throws Exception{
		context.addRoutes(getRoutes());
		String id = DigestUtils.md5Hex(String.format("%s%s", "123", "4711"));
		
		MockEndpoint out = super.getMockEndpoint("mock:out-go");
		out.setExpectedMessageCount(1);
		
		IMap<String, Integer> map = Hazelcast.getMap(CacheConstants.NODE_CACHE);
		map.put(id, ProcessConstants.STATE_WAITING);
		
		template.sendBody("direct:start", "foo");
		
		out.assertIsSatisfied();
		
		map.remove(id);
	}
	
	public void testWait() throws Exception{
		context.addRoutes(getRoutes());
		String id = DigestUtils.md5Hex(String.format("%s%s", "123", "4711"));
		
		MockEndpoint out = super.getMockEndpoint("mock:out-wait");
		out.setExpectedMessageCount(1);
		
		template.sendBody("direct:start", "foo");
		
		out.assertIsSatisfied();
		
		IMap<String, Integer> map = Hazelcast.getMap(CacheConstants.NODE_CACHE);
		assertTrue(map.containsKey(id));
		
		map.remove(id);
	}
	
	private RouteBuilder getRoutes(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:start")
				.setHeader(MessageConstants.INSTANCE_ID, constant("123"))
				.setHeader(MessageConstants.TASK_ID, constant("4711"))
				.process(new TaskInstanceIdProcessor())
				.dynamicRouter(bean(new ReceiveRouter("4711")))
				.to("log:A");
				
				from("direct:go-4711")
				.to("log:go")
				.to("mock:out-go");
				
				from("direct:wait-4711")
				.to("log:wait")
				.setHeader(ReceiveRouter.WAIT, constant("wait"))
				.to("mock:out-wait");
				
			}
		};
		
	}

}
