package com.catify.core.routes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelTestSupport;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;
import com.catify.core.event.impl.beans.StateEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestProcessRoutes extends CamelTestSupport {

	@EndpointInject(uri = "mock://out")
	private MockEndpoint out;
	
	private IMap<String,StateEvent> cache = Hazelcast.getMap(CacheConstants.NODE_CACHE);
	
	@Override
	public void setUp() throws Exception{
		super.setUp();
		
//		deploy process routes
		context.addRoutes(new ProcessRoutes());
		
//		empty cache
		cache.clear();
	}
	
	public void tearDown() throws Exception{
		super.tearDown();
		
//		empty cache
		cache.clear();
	}
	
	public void testReadyState() throws InterruptedException{		
		this.testState("ready", ProcessConstants.STATE_READY);		
	}
	
	public void testWorkingState() throws InterruptedException{
		this.testState("working", ProcessConstants.STATE_WORKING);
	}
	
	public void testWaitingState() throws InterruptedException{
		this.testState("waiting", ProcessConstants.STATE_WAITING);
	}
	
	public void testDoneState() throws InterruptedException{
		this.testState("done", ProcessConstants.STATE_DONE);
	}
	
	public void testDestroy() throws InterruptedException{
		
		// fill the cache
		this.testState("done", ProcessConstants.STATE_DONE);
		
		//destroy object
		template.sendBodyAndHeaders("direct://destroy", "", this.getHeaders());
		assertEquals(0, cache.size());
	}
	
	public void testGetState() throws InterruptedException{
		
		//fill the cache
		this.testState("waiting", ProcessConstants.STATE_WAITING);
		
		//get state
		StateEvent event = template.requestBodyAndHeaders("direct://getState", "", this.getHeaders(), StateEvent.class);
		
		assertNotNull(event);
		assertEquals("5", event.getInstanceId());
		assertEquals(ProcessConstants.STATE_WAITING, event.getState());
	}
	
	public void testGetStateForInstance() throws InterruptedException{
		
		//fill the cache 
		this.fillCache();
		
		//get states		
		Set events = template.requestBodyAndHeaders("direct://getStateForInstance", "", this.getHeaders(), Set.class);
		assertEquals(3, events.size());
	}
	
	public void testForNoState(){
		
		//fill the cache
		this.fillCache();
		
		//get state
		Boolean r1 = template.requestBodyAndHeaders("direct://checkForNoState", "", this.getHeaders(), Boolean.class);
		assertFalse(r1);
		
		Boolean r2 = template.requestBodyAndHeaders("direct://checkForNoState", "", this.getHeaders("7", "1"), Boolean.class);
		assertTrue(r2);
		
	}
	
//	helper ---------------------------------------------

	private void fillCache(){
		cache.put("3", new StateEvent("5", ProcessConstants.STATE_WAITING));
		cache.put("6", new StateEvent("5", ProcessConstants.STATE_DONE));
		cache.put("9", new StateEvent("4", ProcessConstants.STATE_WORKING));
		cache.put("7", new StateEvent("5", ProcessConstants.STATE_WAITING));
	}
	
	private Map<String,Object> getHeaders(){
		return this.getHeaders("5", "1");
	}
	
	private Map<String,Object> getHeaders(String iid,String tiid){
		Map<String,Object> headers = new HashMap<String, Object>();
		
		headers.put(MessageConstants.INSTANCE_ID, iid);
		headers.put(MessageConstants.TASK_INSTANCE_ID, tiid);
		
		return headers;
	}
	
	private void testState(String route, int state,  String iid, String tiid) throws InterruptedException{
		out.setExpectedMessageCount(1);
		
		//send message
		template.sendBodyAndHeaders(String.format("direct://%s", route), "", this.getHeaders(iid, tiid));
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
		
		assertEquals(1, cache.size());
		
		//check if the correct state event was stored
		StateEvent body = out.getReceivedExchanges().get(0).getIn().getBody(StateEvent.class);
		assertNotNull(body);
		assertEquals("5", body.getInstanceId());
		assertEquals(state, body.getState());
	}
	
	private void testState(String route, int state) throws InterruptedException{
		this.testState(route, state, "5", "1");
	}
	
	@Override
	protected RouteBuilder createRouteBuilder(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				fromF("hazelcast:%s%s", HazelcastConstants.MAP_PREFIX, CacheConstants.NODE_CACHE)
				.to("log://reply?showAll=true")
				.to("mock://out");
				
			}
		};
	}
	
}
