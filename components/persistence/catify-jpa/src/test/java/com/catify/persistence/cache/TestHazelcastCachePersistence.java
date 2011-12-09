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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;

import org.junit.Test;

import com.catify.core.constants.CacheConstants;
import com.catify.core.event.impl.beans.StateEvent;
import com.catify.core.event.impl.beans.TimerEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestHazelcastCachePersistence extends JpaPersistenceTestHelper {

	public TestHazelcastCachePersistence() throws ClassNotFoundException,
			SQLException {
		super();
	}
	
	/**
	 * test node cache
	 * 
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	@Test public void testNodeCache() throws InterruptedException, SQLException {
		IMap<String, StateEvent> nc = Hazelcast.getMap(CacheConstants.NODE_CACHE);
		
		// store
		nc.put("4711", new StateEvent("1a", 3));
		nc.put("4712", new StateEvent("1b", 3));
		nc.put("4713", new StateEvent("1c", 3));
		// --- sleep ---
		Thread.sleep(250);
		super.countRow("SELECT count(*) FROM NODECACHE", 3);
		super.countRow("SELECT count(*) FROM STATEEVENT", 3);
		
		// --- sleep --
		Thread.sleep(1000);
		
		// read
		StateEvent stateEvent = nc.get("4711");
		assertNotNull(stateEvent);
		assertEquals("1a", stateEvent.getInstanceId());
		assertEquals(3, stateEvent.getState());
		super.countRow("SELECT count(*) FROM NODECACHE", 3);
		super.countRow("SELECT count(*) FROM STATEEVENT", 3);
		
		// delete
		nc.remove("4712");
		super.countRow("SELECT count(*) FROM NODECACHE", 2);
		super.countRow("SELECT count(*) FROM STATEEVENT", 2);
		
	}
	
	/**
	 * test if we can insert and read very fast messages.
	 * 
	 * @throws SQLException
	 */
	@Test public void testMassLoadRead() throws SQLException{
		IMap<String, StateEvent> nc = Hazelcast.getMap(CacheConstants.NODE_CACHE);
		nc.put("4710", new StateEvent("0a", 3));
		int x = 500;
		for (int i = 1; i < x; i++) {
			nc.put("471"+i, new StateEvent(i+"a", 3));
			int y = i-1;
			StateEvent event = nc.get("471"+y);
			assertNotNull(event);
		}
		super.countRow("SELECT count(*) FROM NODECACHE", x);
	}
	
	/**
	 * test payload cache
	 * 
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	@Test public void testPayloadCache() throws InterruptedException, SQLException {
		IMap<String, String> pc = Hazelcast.getMap(CacheConstants.PAYLOAD_CACHE);
		
		// store
		pc.put("4711", TestJpaPayloadCacheStore.getPayload(1));
		pc.put("4712", TestJpaPayloadCacheStore.getPayload(2));
		pc.put("4713", TestJpaPayloadCacheStore.getPayload(3));
		// --- sleep ---
		Thread.sleep(250);
		super.countRow("SELECT count(*) FROM PAYLOADCACHE", 3);
		
		// --- sleep --
		Thread.sleep(1000);
		
		String xml = pc.get("4712");
		assertNotNull(xml);
		assertEquals(TestJpaPayloadCacheStore.getPayload(2), xml);
		
		// delete
		pc.remove("4712");
		super.countRow("SELECT count(*) FROM PAYLOADCACHE", 2);
		
	}
	
	/**
	 * test correlation store
	 * 
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	@Test public void testCorrelationCache() throws InterruptedException, SQLException {
		IMap<String, String> cc = Hazelcast.getMap(CacheConstants.CORRELATION_CACHE);
		
		// store
		cc.put("71cc46fd4210e8aa3d9e0ab1761a2791", "599dd0d74d6e1c140daf587e591b7841");
		cc.put("71cc46fd4210e8aa3d9e0ab1761a2792", "599dd0d74d6e1c140daf587e591b7842");
		cc.put("71cc46fd4210e8aa3d9e0ab1761a2793", "599dd0d74d6e1c140daf587e591b7843");
		// --- sleep ---
		Thread.sleep(250);
		super.countRow("SELECT count(*) FROM CORRELATIONCACHE", 3);
		
		// --- sleep --
		Thread.sleep(1000);
		
		String id = cc.get("71cc46fd4210e8aa3d9e0ab1761a2792");
		assertNotNull(id);
		assertEquals("599dd0d74d6e1c140daf587e591b7842", id);
		
		// delete
		cc.remove("71cc46fd4210e8aa3d9e0ab1761a2792");
		super.countRow("SELECT count(*) FROM CORRELATIONCACHE", 2);
		
	}
	
	/**
	 * test correlation rule cache
	 * 
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	@Test public void testCorrelationRuleCache() throws InterruptedException, SQLException {
		IMap<String, String> crc = Hazelcast.getMap(CacheConstants.CORRELATION_RULE_CACHE);
		
		// store
		crc.put("71cc46fd4210e8aa3d9e0ab1761a2791", TestJpaCorrelationRuleCacheStore.getCorrelationRule());
		crc.put("71cc46fd4210e8aa3d9e0ab1761a2792", TestJpaCorrelationRuleCacheStore.getCorrelationRule());
		crc.put("71cc46fd4210e8aa3d9e0ab1761a2793", TestJpaCorrelationRuleCacheStore.getCorrelationRule());
		// --- sleep ---
		Thread.sleep(250);
		super.countRow("SELECT count(*) FROM CORRELATIONRULECACHE", 3);
		
		// --- sleep --
		Thread.sleep(1000);
		
		String id = crc.get("71cc46fd4210e8aa3d9e0ab1761a2792");
		assertNotNull(id);
		assertEquals(TestJpaCorrelationRuleCacheStore.getCorrelationRule(), id);
		
		// delete
		crc.remove("71cc46fd4210e8aa3d9e0ab1761a2792");
		super.countRow("SELECT count(*) FROM CORRELATIONRULECACHE", 2);
		
	}
	
	@Test public void testTimerCache() throws InterruptedException, SQLException {
		IMap<String, TimerEvent> tc = Hazelcast.getMap(CacheConstants.TIMER_CACHE);
		
		// store
		tc.put("4711", new TimerEvent(12345678, "i1", "t1"));
		tc.put("4712", new TimerEvent(12345678, "i2", "t2"));
		tc.put("4713", new TimerEvent(12345678, "i3", "t3"));
		// --- sleep ---
		Thread.sleep(250);
		super.countRow("SELECT count(*) FROM TIMERCACHE", 3);
		super.countRow("SELECT count(*) FROM TIMEREVENT", 3);
		
		// --- sleep --
		Thread.sleep(1000);
		
		// read
		TimerEvent event = tc.get("4711");
		assertNotNull(event);
		assertEquals("i1", event.getInstanceId());
		assertEquals(12345678, event.getTime());
		assertEquals("t1", event.getTaskId());
		super.countRow("SELECT count(*) FROM TIMERCACHE", 3);
		super.countRow("SELECT count(*) FROM TIMEREVENT", 3);
		
		// delete
		tc.remove("4712");
		super.countRow("SELECT count(*) FROM TIMERCACHE", 2);
		super.countRow("SELECT count(*) FROM TIMEREVENT", 2);
		
	}

}
