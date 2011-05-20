package com.catify.persistence.cache;

import com.catify.core.constants.CacheConstants;

public class MongoDBPayloadCacheStore extends BaseStringMongoDBCacheStore {

	public MongoDBPayloadCacheStore() {
		super(CacheConstants.PAYLOAD_CACHE);
	}

}
