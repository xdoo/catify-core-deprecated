package com.catify.core.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.CamelTestSupport;

public class CamelRestTest extends CamelTestSupport {

	
	public void testSyncRestCall() throws Exception{
		
		context.addRoutes(getRoutes());
		String result = template.requestBody("restlet:http://localhost:9080/rest/test?restletMethod=post", "foo", String.class);
		System.out.println(result);
		assertEquals("hello foo!", result);
	}
	
	private RouteBuilder getRoutes(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("restlet:http://localhost:9080/rest/test?restletMethod=post")
				.to("log:A")
				.setBody(simple("hello ${body}!"))
				.to("log:B");				
				
			}
		};
	}
	
}
