package com.catify.persistence.cache;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.catify.persitence.constants.DataBaseConstants;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class BaseStringMongoDBCacheStore implements MapLoader<String, String>, MapStore<String, String> {

	protected DBCollection collection;
	
	protected final static String KEY 	= "k";
	protected final static String VALUE = "v";
	
	/**
	 * 
	 * 
	 * @param collectionName
	 */
	public BaseStringMongoDBCacheStore(String collectionName){
		try {
			
			//TODO --> make this configurable
			Mongo m = new Mongo( "localhost" , 27017 );
			DB db = m.getDB( DataBaseConstants.MONGO_DB );
			
			//collection is named as the cache itself
			this.collection = db.getCollection( collectionName );
			
			//create an index on the 'key' property
			this.collection.createIndex(new BasicDBObject(KEY, 1));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * load the the item for the given key
	 */
	public String load(String key) {
		
		DBObject one = this.collection.findOne(new BasicDBObject(KEY, key));		
		return (String) one.get(VALUE);
	}

	/**
	 * load all items with the given keys
	 */
	public Map<String, String> loadAll(Collection<String> keys) {
		
		Map<String, String> result = new HashMap<String, String>();
		
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			result.put(key, this.load(key));
		}
		
		return result;
	}
	
	/**
	 * delete one item from the db
	 */
	public void delete(String key) {
		this.collection.remove(new BasicDBObject(KEY, key));
	}
	
	/**
	 * delete all given values from the db
	 */
	public void deleteAll(Collection<String> keys) {
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			this.delete(it.next());
		}
	}

	/**
	 * store one value into the db
	 */
	public void store(String key, String value) {
		this.collection.save(new BasicDBObject(KEY, key).append(VALUE, value));
	}

	/**
	 * store all given values into the db
	 */
	public void storeAll(Map<String, String> values) {
		List<DBObject> objects = new ArrayList<DBObject>();
		
		Iterator<String> it = values.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			objects.add(new BasicDBObject(KEY, key).append(VALUE, values.get(key)));
		}
		
		this.collection.insert(objects);
	}

	@Override
	public Set<String> loadAllKeys() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
