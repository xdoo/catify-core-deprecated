package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import com.catify.core.constants.MessageConstants;

public class TestTaskInstanceIdProcessor extends CamelTestSupport {

	@Test
	public void testTaskInstanceIdCreation() throws Exception{
		
		context.addRoutes(getRoutes());
		String id = DigestUtils.md5Hex(String.format("%s%s", "47", "11"));
		
		template.sendBody("direct:in", "foo");
		Exchange exchange = consumer.receive("seda:out", 5000);
		
		assertNotNull(exchange);
		assertEquals(id, exchange.getIn().getHeader(MessageConstants.TASK_INSTANCE_ID, String.class));
	}
	
	private RouteBuilder getRoutes(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:in")
				.setHeader(MessageConstants.TASK_ID, constant("11"))
				.setHeader(MessageConstants.INSTANCE_ID, constant("47"))
				.process(new TaskInstanceIdProcessor())
				.to("seda:out");
				
			}
		};
		
	}
	
}
