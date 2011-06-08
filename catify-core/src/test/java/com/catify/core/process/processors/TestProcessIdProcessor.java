package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import com.catify.core.constants.MessageConstants;

public class TestProcessIdProcessor extends CamelTestSupport {

	@Test
	public void testProcessIdCreation() throws Exception{
		
		context.addRoutes(getRoutes());
		template.sendBody("direct:start", "foo");
		Exchange exchange = consumer.receive("seda:out", 5000);
		assertNotNull(exchange);
		assertEquals(DigestUtils.md5Hex(String.format("%s%s%s", "tester", "process01", "1.0")), exchange.getIn().getHeader(MessageConstants.PROCESS_ID));
	}
	
	private RouteBuilder getRoutes(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:start")
				.setHeader(MessageConstants.ACCOUNT_NAME, constant("tester"))
				.setHeader(MessageConstants.PROCESS_NAME, constant("process01"))
				.setHeader(MessageConstants.PROCESS_VERSION, constant("1.0"))
				.process(new ProcessIdProcessor())
				.to("seda:out");
				
				
			}
		};
		
	}
	
}
