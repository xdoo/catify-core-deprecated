package com.catify.core.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.CamelTestSupport;

public class CamelSyncTest extends CamelTestSupport {

	public void testSyncCallOverSeda() throws Exception{
		
		String result = template.requestBody("restlet:http://localhost:9080/test?restletMethod=post", "foo", String.class);
		System.out.println(result);
		assertEquals("hello foo!", result);
	}
	
	public void testSyncCallWithProcessor(){
		String body = template.requestBody("direct:processor", "hans", String.class);
		assertEquals("Hello hans", body);
	}
	
	protected RouteBuilder createRouteBuilder(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("restlet:http://localhost:9080/test?restletMethod=post")
				.to("log:A")
				.to("seda:x");
				
				from("seda:x")
				.to("log:B")
				.to("direct:y")
				.to("log:C");
				
				from("direct:y")
				.setBody(simple("hello ${body}!"));
				
				from("direct:processor")
				.to("log:A")
				.process(new Processor() {
					
					public void process(Exchange ex) throws Exception {
						
						ex.getOut().setBody("Hello " + ex.getIn().getBody(String.class));
						ex.getOut().setHeaders(ex.getIn().getHeaders());
					}
				})
				.to("log:B");
				
				
			}
		};
		
	}
	
}
