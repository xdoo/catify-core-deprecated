package com.catify.core.routes;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.CamelSpringTestSupport;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.EventConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.QueueConstants;
import com.catify.core.event.impl.beans.TimerEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;


public class TestEventRoutes extends CamelSpringTestSupport {

	private IMap<String,TimerEvent> timerCache;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.timerCache = Hazelcast.getMap(CacheConstants.TIMER_CACHE);
		this.timerCache.clear();
	}

	protected void tearDown() throws Exception {		
		super.tearDown();
		this.timerCache.clear();
	}
	
	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	public void testRegister(){	
		assertNotNull(context.getRoute("set-timer-event"));
		assertEquals(0, this.timerCache.size());
		template.sendBody("direct:register", "foo");
		assertEquals(1, this.timerCache.size());
	}
	
	public void testUnRegister(){
		assertNotNull(context.getRoute("delete-timer-event"));
		
		this.insertEvent(1000, "123", "4711");
		assertEquals(1, this.timerCache.size());
		template.sendBody("direct:unregister", "foo");
		assertEquals(0, this.timerCache.size());
	}
	
	public void testTimer(){
		assertNotNull(context.getRoute("fire-timer-event"));
		
		this.insertEvent(100, "123", "4711");
		Exchange exchange = consumer.receive("hazelcast:seda:event_4711", 5000);
		assertNotNull(exchange);
		assertEquals("event_4711", exchange.getIn().getHeader(MessageConstants.TASK_ID));
	}
	
	public void testCreateeventMessage(){
		assertNotNull(context.getRoute("create-timer-event-message"));
		
		List<String> list = new ArrayList<String>();
		list.add("123");
		list.add("4711");
		
		template.sendBody("direct:createEventMessage", list);
		Exchange exchange = consumer.receive("hazelcast:seda:event_4711", 5000);
		assertNotNull(exchange);
		assertEquals("event_4711", exchange.getIn().getHeader(MessageConstants.TASK_ID));
	}
	
	protected RouteBuilder createRouteBuilder(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:register")
				.setHeader(EventConstants.EVENT_TIME, constant(100))
				.to("direct:setHeaders")
				.to("log:register")
				.to("direct:set-timer-event");
				
				from("direct:unregister")
				.to("direct:setHeaders")
				.to("log:unregister")
				.to("direct:delete-timer-event");
				
				from("direct:createEventMessage")
				.to("log:register")
				.to("seda:create-timer-event-message");
				
				from("direct:setHeaders")
				.setHeader(MessageConstants.INSTANCE_ID, constant( "123"))
				.setHeader(MessageConstants.TASK_ID, constant("4711"));
				
			}
		};
	}
	
	private void insertEvent(long time, String iid, String nid){
		this.timerCache.put(Long.toString(time), new TimerEvent(time, iid, nid));
	}

}
