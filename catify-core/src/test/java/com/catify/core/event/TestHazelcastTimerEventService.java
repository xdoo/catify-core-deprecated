package com.catify.core.event;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.EventConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.event.impl.HazelcastTimerEventService;
import com.catify.core.event.impl.beans.TimerEvent;
import com.catify.core.process.ProcessHelper;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestHazelcastTimerEventService extends CamelTestSupport {

	private IMap<String, TimerEvent> cache = Hazelcast.getMap(CacheConstants.TIMER_CACHE);
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		cache.clear();
	}
	
	@Override
	public void tearDown() throws Exception {
		cache.clear();
		super.tearDown();
	}
	
	@Test
	public void testRegister(){
		this.registerEvent(1000, "1", "4711");
		
		
		TimerEvent event = cache.get(ProcessHelper.createTaskInstanceId("1", "4711"));
		
		assertNotNull(event);
		assertEquals("1", event.getInstanceId());
		assertEquals("4711", event.getTaskId());
	}
	
	@Test
	public void testUnregister(){
		
		assertEquals(0, cache.size());
		this.registerEvent(1000, "1", "4711");
		assertEquals(1, cache.size());
		
		this.unregisterEvent("1", "4711");
		assertEquals(0, cache.size());
	}
	
	@Test
	public void testFire() throws InterruptedException{
		
		assertEquals(0, cache.size());
		
		this.registerEvent(50, "1", "1");
		this.registerEvent(50, "2", "1");
		this.registerEvent(50, "3", "1");
		this.registerEvent(50, "4", "2");
		this.registerEvent(50, "5", "1");
		this.registerEvent(500, "6", "1");
		this.registerEvent(500, "7", "2");
		this.registerEvent(500, "8", "2");
		this.registerEvent(500, "9", "1");
		
		assertEquals(9, cache.size());
		
		Thread.sleep(60);
		
		template.sendBodyAndHeader("direct:fire", "", Exchange.TIMER_FIRED_TIME, new Date() );
		List<List<String>> events = consumer.receiveBody("seda:out", 5000, List.class);
		
		assertEquals(4, cache.size());
		assertEquals(5, events.size());
	}
	
	@Override
	protected RouteBuilder createRouteBuilder(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:register")
				.bean(HazelcastTimerEventService.class, "register");
				
				from("direct:unregister")
				.bean(HazelcastTimerEventService.class, "unregister");
				
				from("direct:fire")
				.bean(HazelcastTimerEventService.class, "fire")
				.to("seda:out");

				
			}
		};
	}
	
	private void registerEvent(long time, String instanceId, String nodeId){
		template.sendBodyAndHeaders("direct:register", "", this.getHeaders(time, instanceId, nodeId));
	}
	
	private void unregisterEvent(String instanceId, String nodeId){
		template.sendBodyAndHeaders("direct:unregister", "", this.getHeaders(-1, instanceId, nodeId));
	}
	
	private Map<String, Object> getHeaders(long time, String instanceId, String nodeId){
		
		Map<String, Object> headers = new HashMap<String, Object>();
		
		if(time > 0){
			headers.put(EventConstants.EVENT_TIME, time);
		}
		
		headers.put(MessageConstants.INSTANCE_ID, instanceId);
		headers.put(MessageConstants.TASK_ID, nodeId);
		
		return headers;
	}
	
}
