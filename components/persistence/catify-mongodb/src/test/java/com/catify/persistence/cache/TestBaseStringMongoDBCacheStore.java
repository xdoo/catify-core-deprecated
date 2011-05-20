package com.catify.persistence.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.catify.core.constants.CacheConstants;
import com.catify.persitence.constants.DataBaseConstants;
import com.catify.persistence.cache.MongoDBNodeCacheStore;
import com.catify.persistence.cache.MongoDBPayloadCacheStore;
import com.hazelcast.core.MapStore;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class TestBaseStringMongoDBCacheStore extends TestCase {

	private DB db;

	protected void setUp() throws Exception {
		Mongo m = new Mongo( "localhost" , 27017 );
		this.db = m.getDB( DataBaseConstants.MONGO_DB );
	}

	protected void tearDown() throws Exception {
		db.dropDatabase();
	}
	
	public void testLoad(){
		MapStore<String,String> store = new MongoDBNodeCacheStore();
		this.createEntryies(1);
		assertEquals("foo0", store.load("0"));
	}
	
	public void testLoadAll(){
		MapStore<String,String> store = new MongoDBNodeCacheStore();
		this.createEntryies(3);
		
		List<String> keys = new ArrayList<String>();
		
		keys.add("1");
		keys.add("2");
		Map<String, String> all = store.loadAll(keys);
		
		assertEquals("foo1", all.get("1"));
		assertEquals("foo2", all.get("2"));
		assertFalse(all.containsKey("0"));
	}

	public void testDelete() throws InterruptedException{
		MapStore<String,String> store = new MongoDBNodeCacheStore();
		this.createEntryies(3);
		
		assertEquals(3, db.getCollection(CacheConstants.NODE_CACHE).count());
		
		store.delete("1");
		
		Thread.sleep(50);
		
		assertEquals(2, db.getCollection(CacheConstants.NODE_CACHE).count());
		
	}
	
	public void testDeleteAll() throws InterruptedException{
		MapStore<String,String> store = new MongoDBNodeCacheStore();
		this.createEntryies(5);
		
		assertEquals(5, db.getCollection(CacheConstants.NODE_CACHE).count());
		
		List<String> keys = new ArrayList<String>();
		
		keys.add("1");
		keys.add("2");
		store.deleteAll(keys);
		
		Thread.sleep(50);
		assertEquals(3, db.getCollection(CacheConstants.NODE_CACHE).count());
	}
	
	public void testStore(){
		MapStore<String,String> store = new MongoDBNodeCacheStore();
		store.store("1", "foo1");
		
		DBObject one = db.getCollection(CacheConstants.NODE_CACHE).findOne();
		assertNotNull(one);
		assertEquals("1", one.get("k"));
		assertEquals("foo1", one.get("v"));
	}
	
	public void testStoreAll() throws InterruptedException{
		MapStore<String,String> store = new MongoDBNodeCacheStore();
		assertEquals(0, db.getCollection(CacheConstants.NODE_CACHE).count());
		
		Map<String,String> items = new HashMap<String, String>();
		items.put("0", "foo0");
		items.put("1", "foo1");
		items.put("2", "foo2");
		
		store.storeAll(items);
		
		Thread.sleep(50);
		assertEquals(3, db.getCollection(CacheConstants.NODE_CACHE).count());
	}
	
	private void createEntryies(int cnt){
		for (int i = 0; i < cnt; i++) {
			db.getCollection(CacheConstants.NODE_CACHE).save(new BasicDBObject().append("k", String.format("%s", i)).append("v", String.format("foo%s", i)));
		}
	}
}
