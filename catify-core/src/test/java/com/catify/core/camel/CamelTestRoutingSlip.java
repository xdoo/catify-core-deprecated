package com.catify.core.camel;

import org.apache.camel.EndpointInject;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelSpringTestSupport;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CamelTestRoutingSlip extends CamelSpringTestSupport {	
	
	@EndpointInject(uri = "mock:result")
	protected MockEndpoint resultEndpoint;

	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	public void testRoutingSlip() throws InterruptedException{
		// Expected: headers contain them
		resultEndpoint.expectedHeaderReceived("a", "_a");
		resultEndpoint.expectedHeaderReceived("b", "_b");
		
		template.sendBody("direct:foo", "foo");
		
		// Then: we meet the expectation
		resultEndpoint.assertIsSatisfied();

	}
	
	 protected RouteBuilder createRouteBuilder() throws Exception {
		 
		final Foo foo = new Foo();
		 
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:foo")
				.setHeader("routing", constant("activemq:queue:a,activemq:queue:b"))
//				.setExchangePattern(ExchangePattern.InOut)
				.routingSlip("routing")
				.to("log:MAIN?showAll=true")
				.to("mock:result");
				
				from("activemq:queue:a")
				.to("log:A")
				.bean(foo, "doA");
				
				from("activemq:queue:b")
				.to("log:B")
				.bean(foo, "doB");
				
			}
		};
	}
	 
	public class Foo {
		
		public void doA(Message msg){	
			msg.setHeader("a", "_a");
		}
		
		public void doB(Message msg){
			msg.setHeader("b", "_b");
		}
		
	}

}
