package com.catify.persistence.cache;

import com.catify.core.constants.CacheConstants;
import com.catify.persitence.constants.DataBaseConstants;
import com.mongodb.DB;
import com.mongodb.Mongo;

import junit.framework.TestCase;

public class TestMongoDBCacheStoreImplementations extends TestCase {

	private DB db;

	protected void setUp() throws Exception {
		Mongo m = new Mongo( "localhost" , 27017 );
		this.db = m.getDB( DataBaseConstants.MONGO_DB );
	}

	protected void tearDown() throws Exception {
		db.dropDatabase();
	}
	
	public void testNodeCacheCollectionCreation(){
		new MongoDBNodeCacheStore();		
		assertTrue(this.db.collectionExists(CacheConstants.NODE_CACHE));
	}
	
	public void testPayloadCacheCollectionCreation(){
		new MongoDBPayloadCacheStore();		
		assertTrue(this.db.collectionExists(CacheConstants.PAYLOAD_CACHE));
	}
	
	public void testTransformationCacheCollectionCreation(){
		new MongoDBTransformationCacheStore();
		assertTrue(this.db.collectionExists(CacheConstants.TRANSFORMATION_CACHE));
	}
	
}
