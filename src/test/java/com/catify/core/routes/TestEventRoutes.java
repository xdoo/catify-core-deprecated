package com.catify.core.routes;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.CamelSpringTestSupport;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.core.constants.DataBaseConstants;
import com.catify.core.constants.EventConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.QueueConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class TestEventRoutes extends CamelSpringTestSupport {

	protected DBCollection timerCollection;
	private DB db;

	protected void setUp() throws Exception {
		
		super.setUp();
		
		context.addRoutes(getRoutes());
		
		Mongo m = new Mongo( "localhost" , 27017 );
		this.db = m.getDB( DataBaseConstants.MONGO_DB );
		this.timerCollection = this.db.getCollection( "timer" );
		
	}

	protected void tearDown() throws Exception {
		this.db.dropDatabase();
		
		super.tearDown();
	}
	
	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	public void testRegister(){	
		assertNotNull(context.getRoute("set-timer-event"));
		assertEquals(0, this.timerCollection.count());
		template.sendBody("direct:register", "foo");
		assertEquals(1, this.timerCollection.count());
	}
	
	public void testUnRegister(){
		assertNotNull(context.getRoute("delete-timer-event"));
		
		this.insertEvent(1000, "123", "4711");
		assertEquals(1, this.timerCollection.count());
		template.sendBody("direct:unregister", "foo");
		assertEquals(0, this.timerCollection.count());
	}
	
	public void testTimer(){
		assertNotNull(context.getRoute("fire-timer-event"));
		
		this.insertEvent(100, "123", "4711");
		Exchange exchange = consumer.receive("activemq:queue:event_4711", 5000);
		assertNotNull(exchange);
		assertEquals("event_4711", exchange.getIn().getHeader(MessageConstants.TASK_ID));
	}
	
	public void testCreateeventMessage(){
		assertNotNull(context.getRoute("create-timer-event-message"));
		
		List<String> list = new ArrayList<String>();
		list.add("123");
		list.add("4711");
		
		template.sendBody("direct:createEventMessage", list);
		Exchange exchange = consumer.receive("activemq:queue:event_4711", 5000);
		assertNotNull(exchange);
		assertEquals("event_4711", exchange.getIn().getHeader(MessageConstants.TASK_ID));
	}
	
	private RouteBuilder getRoutes(){
		
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
		DBObject event = new BasicDBObject().append("t", time).append("iid", iid).append("nid", nid);
		this.timerCollection.insert(event);
	}

}
