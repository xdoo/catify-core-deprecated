package com.catify.core.activemq;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.CamelSpringTestSupport;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ActiveMqNetworkOfBrokersTest extends CamelSpringTestSupport{

	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/test-network-context.xml");
	}
	
	public void testQueue() throws Exception{
		template.sendBody("direct:start", "foo");
		String body = consumer.receiveBody("seda:end", 5000, String.class);
		
		assertEquals("foo", body);
		
		Thread.sleep(60 * 1000);
	}
	
	@Override
	protected RouteBuilder createRouteBuilder(){
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:start")
				.to("log:A")
				.to("activemq:queue:foo");
				
				from("activemq:queue:foo")
				.to("log:B")
				.to("seda:end");
			}
		};
		
	}



}
