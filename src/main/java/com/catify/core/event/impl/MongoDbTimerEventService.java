package com.catify.core.event.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.camel.Message;

import com.catify.core.constants.DataBaseConstants;
import com.catify.core.constants.EventConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.event.TimerEventService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoDbTimerEventService implements TimerEventService {
	
	private DBCollection collection;
	
	protected static final String TIME 			= "t";
	protected static final String EVENTS 		= "e";
	protected static final String INSTANCEID 	= "iid";
	protected static final String NODEID 		= "nid";

	public MongoDbTimerEventService() {
		try {
			
			//TODO --> make this configurable
			Mongo m = new Mongo( "localhost" , 27017 );
			DB db = m.getDB( DataBaseConstants.MONGO_DB );
			
			//collection is named as the cache itself
			this.collection = db.getCollection( "timer" );
			
			//create an index on the 'key' property
			this.collection.createIndex(new BasicDBObject(TIME, 1));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
	}
	
	public List<List<String>> fire(Message m) {
		
		long time = m.getHeader("firedTime", Date.class).getTime();
		
		List<List<String>> result = new ArrayList<List<String>>();
		
		DBObject query = new BasicDBObject().append(TIME, new BasicDBObject().append("$lte", time));
		Iterator<DBObject> it = this.collection.find(query).iterator();
		
		while (it.hasNext()) {
			DBObject event = it.next();
			
			//fill the 'row'
			List<String> row = new ArrayList<String>();
			row.add(((String)event.get(INSTANCEID))); 	//index 0
			row.add(((String)event.get(NODEID)));		//index 1
			
			//add row to result set
			result.add(row);
			
			//delete event
			this.collection.remove(event);
		}
		
		return result;
	}

	public void register(Message m) {
		
		long time  = new Date().getTime() + m.getHeader(EventConstants.EVENT_TIME, Long.class);
		String instanceId = m.getHeader(MessageConstants.INSTANCE_ID, String.class);
		String nodeId = m.getHeader(MessageConstants.TASK_ID, String.class);
		
		DBObject event = new BasicDBObject().append(TIME, time).append(INSTANCEID, instanceId).append(NODEID, nodeId);
		this.collection.insert(event);
	}

	public void unregister(Message m) {
		
		String instanceId = m.getHeader(MessageConstants.INSTANCE_ID, String.class);
		String nodeId = m.getHeader(MessageConstants.TASK_ID, String.class);
		
		DBObject event = new BasicDBObject().append(INSTANCEID, instanceId).append(NODEID, nodeId);
		this.collection.remove(event);
	}

}
