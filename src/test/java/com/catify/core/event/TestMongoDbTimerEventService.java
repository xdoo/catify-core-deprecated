package com.catify.core.event;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.CamelTestSupport;

import com.catify.core.constants.DataBaseConstants;
import com.catify.core.constants.EventConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.event.impl.MongoDbTimerEventService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class TestMongoDbTimerEventService extends CamelTestSupport{

	private DBCollection collection;
	private DB db;

	protected void setUp() throws Exception {
		
		super.setUp();
		
		Mongo m = new Mongo( "localhost" , 27017 );
		this.db = m.getDB( DataBaseConstants.MONGO_DB );
		this.collection = this.db.getCollection( "timer" );
		
	}

	protected void tearDown() throws Exception {
		this.db.dropDatabase();
		
		super.tearDown();
	}
	
	public void testRegisterEvent() throws Exception{
		context.addRoutes(this.getRoutes());
		
		assertEquals(0, this.collection.count());
		
		this.registerEvent(12345, "1", "1");
		
		assertEquals(1, this.collection.count());
		
		this.registerEvent(12345, "2", "2");
		this.registerEvent(12345, "3", "3");
		
		Thread.sleep(50);
		
		assertEquals(3, this.collection.count());
		Object[] e1 = {"1","2","3"};
		this.checkDb(e1);
	}
	
	public void testUnregisterEvent() throws Exception{
		context.addRoutes(getRoutes());
		
		this.registerEvent(22345, "1", "1");
		this.registerEvent(22345, "2", "2");
		this.registerEvent(22345, "3", "3");
		
		assertEquals(3, this.collection.count());		
		Object[] e1 = {"1","2","3"};
		this.checkDb(e1);
		
		this.unregisterEvent("2", "2");
		
		Thread.sleep(50);
		
		assertEquals(2, this.collection.count());
		Object[] e2 = {"1","3"};
		this.checkDb(e2);
	}
	
	public void testFireAndDeleteEvent() throws Exception{
		context.addRoutes(getRoutes());
		
		this.registerEvent(50, "1", "1");
		this.registerEvent(50, "2", "2");
		this.registerEvent(50, "3", "3");
		this.registerEvent(50, "4", "4");
		this.registerEvent(50, "5", "5");
		this.registerEvent(500, "6", "6");
		this.registerEvent(500, "7", "7");
		this.registerEvent(500, "8", "8");
		this.registerEvent(500, "9", "9");
		
		assertEquals(9, this.collection.count());		
		Object[] e1 = {"1","2","3", "4", "5", "6", "7", "8", "9"};
		this.checkDb(e1);
		
		Thread.sleep(60);
		
		template.sendBodyAndHeader("direct:fire", "", Exchange.TIMER_FIRED_TIME, new Date() );
		List<List<String>> events = consumer.receiveBody("seda:out", 5000, List.class);
		
		assertEquals(4, this.collection.count());		
		Object[] e2 = {"6", "7", "8", "9"};
		this.checkDb(e2);
		
		assertEquals(5, events.size());
	}
	
	private void checkDb(Object[] events){
		
		List<Object> x = Arrays.asList(events);
		Iterator<DBObject> it = this.collection.find().iterator();
		
		while (it.hasNext()) {	
			DBObject event = (DBObject) it.next();
			assertTrue(x.contains(event.get("iid")));
			assertTrue(x.contains(event.get("nid")));
		}
	}
	
	private RouteBuilder getRoutes(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:register")
				.bean(MongoDbTimerEventService.class, "register");
				
				from("direct:unregister")
				.bean(MongoDbTimerEventService.class, "unregister");
				
				from("direct:fire")
				.bean(MongoDbTimerEventService.class, "fire")
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
