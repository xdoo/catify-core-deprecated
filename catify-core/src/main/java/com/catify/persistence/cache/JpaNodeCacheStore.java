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

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;

import com.catify.core.event.impl.beans.StateEvent;
import com.catify.persistence.beans.NodeCache;

public class JpaNodeCacheStore extends BaseJpaCacheStore {

	public static final String LOAD_BY_KEY 		= "nodeCache_LoadByKey";
	public static final String LOAD_ALL_KEYS 	= "nodeCache_LoadAllKeys";

	@Produce(uri = "seda:foo")
	ProducerTemplate foo;
	
	public JpaNodeCacheStore() {
		super(LOAD_BY_KEY, LOAD_ALL_KEYS);
	}

	@Override public void store(String key, Object value) {
		
		if(value instanceof StateEvent) {
			EntityManager em = emf.createEntityManager();
			EntityTransaction tx = em.getTransaction();
			try {
				tx.begin();
				em.merge(new NodeCache(key, (StateEvent) value));
				tx.commit();
			} catch ( RuntimeException ex ) {
				if( tx != null && tx.isActive() ) tx.rollback();
		        throw ex;
			} finally{
				em.close();
			}
		} 
	}
	
	@Override public void storeAll(Map<String, Object> map) {
		
		Iterator<String> it = map.keySet().iterator();
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			
			while (it.hasNext()) {
				String key = (String) it.next();
				em.merge(new NodeCache(key, (StateEvent) map.get(key)));
			}
			
			tx.commit();
		} catch ( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
	        throw ex;
		} finally {
			em.close();
		}
	}
	
	@Override public StateEvent load(String key) {
		NodeCache result = (NodeCache) super.load(key);
		
		if (result != null) {
			return result.getBeanValue();
		} else {
			// TODO --> log
			return null;
		}
	}

	public void send(String value){
		foo.sendBody(value);
	}
	
	
}
