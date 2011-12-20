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

import javax.persistence.PersistenceException;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import com.catify.persistence.beans.PayloadCache;


public class JpaPayloadCacheStore extends BaseJpaCacheStore {

	public static final String LOAD_BY_KEY 		= "payloadCache_LoadByKey";
	public static final String LOAD_ALL_KEYS 	= "payloadCache_LoadAllKeys";
	
	@EndpointInject(uri = "seda:jpaPayloadCacheStore")
	ProducerTemplate producer;
	
	public JpaPayloadCacheStore(CamelContext context) {
		super(context, LOAD_BY_KEY, LOAD_ALL_KEYS);
	}

	@Override
	public void store(String key, Object value) {
		
		if(value instanceof String) {
			
			producer.sendBody(new PayloadCache(key, (String) value));
			
		} 

	}

	@Override
	public void storeAll(Map<String, Object> map) {
		Iterator<String> it = map.keySet().iterator();
		PersistenceException cause = null;
		TransactionStatus status = tm.getTransaction(def);
		try {
			while (it.hasNext()) {
				String key = (String) it.next();
				em.merge(new PayloadCache(key, (String) map.get(key)));
			}
			tm.commit(status);
		} catch (Exception e) {
            if (e instanceof PersistenceException) {
                cause = (PersistenceException) e;
                tm.rollback(status);
            } else {
                cause = new PersistenceException(e);
            }
        }

        if (cause != null) {
                throw cause;
        }

	}
	
	@Override public String load(String key) {
		PayloadCache result = (PayloadCache) super.load(key);
		
		if (result != null) {
			return result.getBeanValue();
		} else {
			// TODO --> log
			return null;
		}
	}




}
