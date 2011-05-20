package com.catify.core.camel;

import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelTestSupport;

public class CamelRouterTest extends CamelTestSupport {

	public void testRouterWithExchange() throws InterruptedException{
		
		MockEndpoint a = getMockEndpoint("mock:a");
		MockEndpoint b = getMockEndpoint("mock:b");
		MockEndpoint c = getMockEndpoint("mock:c");
		
		a.setExpectedMessageCount(1);
		b.setExpectedMessageCount(2);
		c.setExpectedMessageCount(1);
		
		template.sendBody("direct:a", "bar");
		template.sendBodyAndHeader("direct:c", "bar", "foo", true);
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
	
	protected RouteBuilder createRouteBuilder(){
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:a")
				.dynamicRouter(bean(new MyRouter(), "route1"))
				.to("mock:a");
				
				from("direct:c")
				.dynamicRouter(bean(new MyRouter(), "route2"))
				.to("mock:c");
				
				from("direct:b")
				.log("-----------> ${exchangeId}")
				.setHeader("foo", constant(false))
				.to("mock:b");
				
			}
		};
		
	}
	
	public class MyRouter{
		
		public String route1(String body, @Header(Exchange.SLIP_ENDPOINT) String previous){
			
			if(previous == null){
				return "direct:b";
			} else {
				return null;
			}
			
		}
		
		public String route2(String body, @Header("foo") boolean state){
			
			if(state){
				return "direct:b";
			} else {
				return null;
			}
			
		}
	}
	
}
