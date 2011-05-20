package com.catify.core.camel;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.CamelTestSupport;

public class CamelSimpleTest extends CamelTestSupport {

	public void testListValues() throws Exception{
		context.addRoutes(getRoutes());
		
		List<String> list = new ArrayList<String>();
		list.add("foo-");
		list.add("bar-");
		
		template.sendBody("direct:list", list);
		
		Exchange ex = consumer.receive("seda:out", 5000);
		
		assertEquals("foo-", ex.getIn().getHeader("foo"));
		assertEquals("bar-", ex.getIn().getHeader("bar"));
	}
	
	private RouteBuilder getRoutes(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:list")
				.setHeader("foo", simple("${body[0]}"))
				.setHeader("bar", simple("${body[1]}"))
				.to("log:A")
				.to("seda:out");
				
			}
		};
	}
}
