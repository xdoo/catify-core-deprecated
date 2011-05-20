package com.catify.core.camel;

import java.util.concurrent.TimeUnit;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelTestSupport;

public class CamelTestRouteReplacement extends CamelTestSupport {

	public void testReplacement() throws Exception{
		
		MockEndpoint a = getMockEndpoint("mock:a");
		a.setExpectedMessageCount(1);
		
		template.sendBody("direct:a", "foo");
		
		RouteBuilder builder = new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:a")
				.routeId("myRoute")
				.to("mock:b");
				
			}
		};
		
		context.addRoutes(builder);
		
		MockEndpoint b = getMockEndpoint("mock:b");
		b.setExpectedMessageCount(1);
		
		template.sendBody("direct:a", "foo");
		
		assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);
		
	}
	
	public void testErrorReplacement() throws Exception{
		
		MockEndpoint a = getMockEndpoint("mock:a");
		a.setExpectedMessageCount(1);
		
		template.sendBody("direct:a", "foo");
		
		RouteBuilder builder = new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:a")
				.routeId("myRoute")
				.multicast()
				.to("mock:b");
				
			}
		};
		
		context.addRoutes(builder);
		
		MockEndpoint b = getMockEndpoint("mock:b");
		b.setExpectedMessageCount(1);
		
		template.sendBody("direct:a", "foo");
		
		assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);
		
	}
	
	protected RouteBuilder createRouteBuilder(){
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:a")
				.routeId("myRoute")
				.to("mock:a");
				
			}
		};
		
	}
	
}
