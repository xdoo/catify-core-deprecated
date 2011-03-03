package com.catify.core.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.CamelTestSupport;

public class CamelFluentApiTest extends CamelTestSupport {

	
	private RouteBuilder getRoutes(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				
				RouteDefinition from = from("direct:x");
				
				RouteDefinition routeDefinition = from.to("direct:y");
				
			}
		};
	}
	
}
