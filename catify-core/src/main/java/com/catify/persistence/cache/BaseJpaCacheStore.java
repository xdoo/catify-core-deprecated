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
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jpa.JpaTemplateTransactionStrategy;
import org.apache.camel.component.jpa.TransactionStrategy;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;

public abstract class BaseJpaCacheStore implements MapLoader<String, Object>, MapStore<String, Object> {
	
	// borrowed from camel-jpa
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BaseJpaCacheStore.class);
    private EntityManagerFactory entityManagerFactory;
    protected PlatformTransactionManager transactionManager;
    private JpaTemplate template;
    protected DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    
 // single TransactionTemplate shared amongst all methods in this instance
    private final TransactionTemplate transactionTemplate;
	
	protected EntityManager em;
	
	private String NamedQueryLoadByKey;
	private String NamedQueryLoadAllKeys;
	
	public BaseJpaCacheStore(CamelContext context, String loadByKey, String LoadAllKeys) {
		
		setupJpa(context, loadByKey, LoadAllKeys);
		this.createTransactionStrategy();
		
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		
		def.setName("BaseJPATransactionDefinition");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		
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
		
		Query query = em.createNamedQuery( this.NamedQueryLoadAllKeys );
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
	@SuppressWarnings("unchecked")
	@Override public void delete(String key) {	
		PersistenceException cause = null;
		TransactionStatus status = transactionManager.getTransaction(def);
		
		EntityManager emp = getEntityManagerFactory().createEntityManager();
		
		try {
			final Object values = queryWithKey(NamedQueryLoadByKey, key).getSingleResult();
			
//			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//                
//	            // the code in this method executes in a transactional context
//	            public void doInTransactionWithoutResult(TransactionStatus status) {
//	            	em.remove(values);
//	            }
//
//	        });
			
			this.template.execute(new JpaCallback<Object>() {
	            public Object doInJpa(EntityManager emp) throws PersistenceException {
	            	em.remove(values);
	            	return null;
	            }
			});
			
			
//			em.remove(this.queryWithKey(NamedQueryLoadByKey, key).getSingleResult());
		} catch (Exception e) {
            if (e instanceof PersistenceException) {
            	transactionManager.rollback(status);
                cause = (PersistenceException) e;
            } else {
                cause = new PersistenceException(e);
            }
        }
		
//		transactionManager.commit(status);

        if (cause != null) {
                throw cause;
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
		Query query = em.createNamedQuery( nq );
		query.setParameter( "param", key );
		
		return query;
	}
	
	private Query queryWithKey(String nq, String key, EntityManager emp) {
		Query query = emp.createNamedQuery( nq );
		query.setParameter( "param", key );
		
		return query;
	}
	

	public String getNamedQueryLoadByKey() {
		return NamedQueryLoadByKey;
	}

	public String getNamedQueryLoadAllKeys() {
		return NamedQueryLoadAllKeys;
	}
	
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.template = new JpaTemplate(entityManagerFactory);
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    public JpaTemplate getTemplate() {
        if (template == null) {
            template = createTemplate();
        }
        return template;
    }

    public void setTemplate(JpaTemplate template) {
        this.template = template;
    }
    
    protected JpaTemplate createTemplate() {
        return new JpaTemplate(getEntityManagerFactory());
    }
    
    protected TransactionStrategy createTransactionStrategy() {
        return JpaTemplateTransactionStrategy.newInstance(getTransactionManager(), getTemplate());
    }
    
    public void setupJpa(CamelContext context, String loadByKey, String LoadAllKeys){
    	 // lookup entity manager factory and use it if only one provided
        if (entityManagerFactory == null) {
            Map<String, EntityManagerFactory> map = context.getRegistry().lookupByType(EntityManagerFactory.class);
            if (map != null) {
                if (map.size() == 1) {
                    entityManagerFactory = map.values().iterator().next();
                    LOG.info("Using EntityManagerFactory found in registry with id ["
                            + map.keySet().iterator().next() + "] " + entityManagerFactory);
                } else {
                    LOG.debug("Could not find a single EntityManagerFactory in registry as there was " + map.size() + " instances.");
                }
            }
        } else {
            LOG.info("Using EntityManagerFactory configured: " + entityManagerFactory);
        }

        // lookup transaction manager and use it if only one provided
        if (transactionManager == null) {
            Map<String, PlatformTransactionManager> map = context.getRegistry().lookupByType(PlatformTransactionManager.class);
            if (map != null) {
                if (map.size() == 1) {
                    transactionManager = map.values().iterator().next();
                    LOG.info("Using TransactionManager found in registry with id ["
                            + map.keySet().iterator().next() + "] " + transactionManager);
                } else {
                    LOG.debug("Could not find a single TransactionManager in registry as there was " + map.size() + " instances.");
                }
            }
        } else {
            LOG.info("Using TransactionManager configured on this component: " + transactionManager);
        }

        // transaction manager could also be hidden in a template
        if (transactionManager == null) {
            Map<String, TransactionTemplate> map = context.getRegistry().lookupByType(TransactionTemplate.class);
            if (map != null) {
                if (map.size() == 1) {
                    transactionManager = map.values().iterator().next().getTransactionManager();
                    LOG.info("##### --> BASEJPACACHESTORE: Using TransactionManager found in registry with id ["
                            + map.keySet().iterator().next() + "] " + transactionManager);
                } else {
                    LOG.debug("##### --> BASEJPACACHESTORE: Could not find a single TransactionTemplate in registry as there was " + map.size() + " instances.");
                }
            }
        }

        // warn about missing configuration
        if (entityManagerFactory == null) {
            LOG.warn("No EntityManagerFactory has been configured on this JpaComponent. Each JpaEndpoint will auto create their own EntityManagerFactory.");
        }
        if (transactionManager == null) {
            LOG.warn("No TransactionManager has been configured on this JpaComponent. Each JpaEndpoint will auto create their own JpaTransactionManager.");
        }
		
		// store name queries
		this.NamedQueryLoadByKey 	= loadByKey;
		this.NamedQueryLoadAllKeys 	= LoadAllKeys;
				
		em = getEntityManagerFactory().createEntityManager();
    }
	
}
