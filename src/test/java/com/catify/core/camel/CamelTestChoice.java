package com.catify.core.camel;

import java.util.concurrent.TimeUnit;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelTestSupport;

public class CamelTestChoice extends CamelTestSupport {

	public void testChoice() throws InterruptedException{
		
		MockEndpoint bar = getMockEndpoint("mock:bar");
		bar.expectedMessageCount(1);
		
		template.sendBodyAndHeader("direct:in", "bar", "foo", "bar");

		assertMockEndpointsSatisfied(5000, TimeUnit.MILLISECONDS);
	}
	
	protected RouteBuilder createRouteBuilder(){
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:in")
				.choice()
					.when(header("foo").isEqualTo("bar"))
						.to("mock:bar")
					.otherwise()
						.log("fail");
				
			}
		};
		
	}
	
}
