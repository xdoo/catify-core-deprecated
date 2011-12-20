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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;

public abstract class BaseJpaCacheStore implements MapLoader<String, Object>, MapStore<String, Object>  {

	protected EntityManagerFactory emf;
	
	protected final static String PU = "CatifyJpaPU";
	
	private String NamedQueryLoadByKey;
	private String NamedQueryLoadAllKeys;
	
	public BaseJpaCacheStore(String loadByKey, String LoadAllKeys) {
		
		// store name queries
		this.NamedQueryLoadByKey 	= loadByKey;
		this.NamedQueryLoadAllKeys 	= LoadAllKeys;
		
		// create central entity manager factory
		emf = Persistence.createEntityManagerFactory(PU);
	}
	
	/**
	 * load the object with the given named query and key.
	 */
	@Override public Object load(String key) {
		
		List results = this.queryWithKey(NamedQueryLoadByKey, key).getResultList();
		
		if(results.isEmpty()) {
			return null;
		} else if (results.size() > 1) {
			
			// TODO --> log here
			
			return results.get(0);
		} else {
			return results.get(0);
		}
	}

	/**
	 * load the object with the given named query and keyset.
	 */
	@Override public Map<String, Object> loadAll(Collection<String> keys) {
		
		Map<String, Object> results = new HashMap<String, Object>();
		
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			results.put(key, this.load(key));
		}
		
		return results;
	}

	/**
	 * load all keys from the database and give them back as
	 * {@link java.util.Set}
	 */
	@Override public Set<String> loadAllKeys(){
		
		Query query = emf.createEntityManager().createNamedQuery( this.NamedQueryLoadAllKeys );
		List resultList = query.getResultList();
		
		return new HashSet(resultList);
	}

	/**
	 * it's difficult to store an object in a generic way, because
	 * the structure differs from object to object. we decided to
	 * implement this in every single persistence implementation.
	 */
	@Override public abstract void store(String key, Object value);
	@Override public abstract void storeAll(Map<String, Object> map);

	/**
	 * removes cascading the cache object.
	 */
	@Override public void delete(String key) {	
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.remove(this.queryWithKey(NamedQueryLoadByKey, key, em).getSingleResult());
			tx.commit();
		} catch ( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
	        throw ex;
		} finally {
			em.close();
		}
	}

	@Override public void deleteAll(Collection<String> keys) {
		
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			
			this.delete(key);
		}
		
	}
	
	/**
	 * helper to run query with key
	 * 
	 * @param nq
	 * @param key
	 * @return
	 */
	private Query queryWithKey(String nq, String key) {
		Query query = emf.createEntityManager().createNamedQuery( nq );
		query.setParameter( "param", key );
		
		return query;
	}
	
	/**
	 * the em is important to delete entities
	 * in one single em.
	 * 
	 * @param nq
	 * @param key
	 * @param em
	 * @return
	 */
	private Query queryWithKey(String nq, String key, EntityManager em) {
		Query query = em.createNamedQuery( nq );
		query.setParameter( "param", key );
		
		return query;
	}

	public String getNamedQueryLoadByKey() {
		return NamedQueryLoadByKey;
	}

	public String getNamedQueryLoadAllKeys() {
		return NamedQueryLoadAllKeys;
	}
}
