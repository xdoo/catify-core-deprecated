package com.catify.core.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.CamelTestSupport;

public class CamelFileTest extends CamelTestSupport {
	
	public void testWriteDrlFile() throws Exception{
		
		context.addRoutes(getRoutes());
		template.sendBody("direct:write", "foo");
		
		
	}
	
	private RouteBuilder getRoutes(){
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:write")
				.to("file:rules/decision_node/?fileName=rule.drl");
				
			}
		};
	}
}
