/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.catify.persistence.cache;

import java.util.Iterator;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.catify.persistence.beans.CorrelationCache;

public class JpaCorrelationCacheStore extends BaseJpaCacheStore {

	public static final String LOAD_BY_KEY 		= "correlationCache_LoadByKey";
	public static final String LOAD_ALL_KEYS 	= "correlationCache_LoadAllKeys";
	
	public JpaCorrelationCacheStore() {
		super(LOAD_BY_KEY, LOAD_ALL_KEYS);
	}

	@Override
	public void store(String key, Object value) {
		
		if(value instanceof String) {
			EntityManager em = emf.createEntityManager();
			EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
				em.merge(new CorrelationCache(key, (String) value));
				tx.commit();
			} catch ( RuntimeException ex ) {
				if( tx != null && tx.isActive() ) tx.rollback();
		        throw ex;
			} finally {
				em.close();
			}
		} 

	}

	@Override
	public void storeAll(Map<String, Object> map) {
		Iterator<String> it = map.keySet().iterator();
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			
			while (it.hasNext()) {
				String key = (String) it.next();
				em.merge(new CorrelationCache(key, (String) map.get(key)));
			}
			
			tx.commit();
		} catch ( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
	        throw ex;
		} finally {
			em.close();
		}

	}
	
	@Override public String load(String key) {
		CorrelationCache result = (CorrelationCache) super.load(key);
		
		if (result != null) {
			return result.getBeanValue();
		} else {
			// TODO --> log
			return null;
		}
	}

}
