package com.catify.persistence.cache;

import com.catify.core.constants.CacheConstants;

public class MongoDBNodeCacheStore extends BaseStringMongoDBCacheStore {

	public MongoDBNodeCacheStore(){
		super(CacheConstants.NODE_CACHE);
	}

}
