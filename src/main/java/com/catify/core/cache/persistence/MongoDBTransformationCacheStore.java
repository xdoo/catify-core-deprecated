package com.catify.core.cache.persistence;

import com.catify.core.constants.CacheConstants;

public class MongoDBTransformationCacheStore extends
		BaseStringMongoDBCacheStore {

	public MongoDBTransformationCacheStore() {
		super(CacheConstants.TRANSFORMATION_CACHE);
	}

}
