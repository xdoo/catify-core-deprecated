package com.catify.core.process.processors;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import com.catify.core.constants.MessageConstants;

public class TestTransformDecisionPayloadProcessor extends CamelTestSupport {

	@Test
	public void testPaylodTransformation() throws Exception{
		context.addRoutes(getRoutes());
		String body = template.requestBody("direct:start", this.getPayload(), String.class);
		assertTrue(body.startsWith("<com.catify.tester.class_4711>"));
		assertTrue(body.endsWith("</com.catify.tester.class_4711>"));
		assertTrue(body.contains(this.getPayload()));
		
	}
	
	private RouteBuilder getRoutes(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:start")
				.setHeader(MessageConstants.ACCOUNT_NAME, constant("tester"))
				.setHeader(MessageConstants.TASK_ID, constant("4711"))
				.process(new TransformDecisionPayloadProcessor());
				
			}
		};
	}
	
	private String getPayload(){
		return "<foo>myfoo</foo>";
	}
}
