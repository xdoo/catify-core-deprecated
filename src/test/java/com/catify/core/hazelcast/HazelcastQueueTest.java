package com.catify.core.hazelcast;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.CamelTestSupport;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.collections.MapUtils;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IQueue;

public class HazelcastQueueTest extends CamelTestSupport {

	
	private IQueue<Object> queue;


	public void testMessage() throws InterruptedException{
		this.queue = Hazelcast.getQueue("foo");
		
		template.sendBody("direct:start", "my-foo");
		
		Object poll = this.queue.poll(5, TimeUnit.SECONDS);
		assertNotNull(poll);
//		assertTrue(poll instanceof HazelcastMessage);
	}
	
	public void testExchangeId(){
		template.sendBody("direct:a", "foo");
	}
	
	
	protected RouteBuilder createRouteBuilder(){
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:start")
				.process(new Processor() {
					
					public void process(Exchange ex) throws Exception {
						
						Map<String, Object> map = ExchangeHelper.createVariableMap(ex);
						
						queue.add(MapUtils.unmodifiableMap(map));
						
					}
				});
				
				from("direct:in")
				.to("seda:out");
				
				from("direct:marshal")
				.marshal().serialization()
				.to("log:A?showAll=true")
				.to("direct:unmarshal");
				
				from("direct:unmarshal")
				.unmarshal().serialization()
				.to("log:B?showAll=true");
				
				from("direct:a")
				.to("log:A?showExchangeId=true")
				.to("direct:b");
				
				from("direct:b")
				.to("log:B?showExchangeId=true");
				
			}
		};
	}
	
	class HazelcastMessage implements  Serializable{

		private static final long serialVersionUID = 58359209568173773L;
		
		private Map<String, String> headers;
		private Object body;
		
		public Map<String, String> getHeaders() {
			return headers;
		}
		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}
		public Object getBody() {
			return body;
		}
		public void setBody(Object body) {
			this.body = body;
		}
		
	}
	
	
}
