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
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.catify.core.event.impl.beans.StateEvent;
import com.catify.core.event.impl.beans.TimerEvent;

public class TestJpaTimerCacheStore extends JpaPersistenceTestHelper {

	private final static String TABLE_TIMERCACHE = "TIMERCACHE";
	private final static String TABLE_TIMEREVENT = "TIMEREVENT";
	private JpaTimerCacheStore cs;
	
	public TestJpaTimerCacheStore() throws ClassNotFoundException, SQLException {
		super();
		
		super.addInsertStatement("INSERT INTO TIMEREVENT VALUES ( 'id_%s', 'taskid_%s', 1234567890, 'instance_%s' )");
		super.addInsertStatement("INSERT INTO TIMERCACHE VALUES ( '%s', 'key_%s', 'id_%s' )");
		
		super.addCountSteatement(String.format("SELECT count(*) FROM %s", TABLE_TIMERCACHE));
		super.addCountSteatement(String.format("SELECT count(*) FROM %s", TABLE_TIMEREVENT));
		
	}
	
	@Before
	public void setUp() throws Exception {
		cs = new JpaTimerCacheStore();
		super.setUp();
	}

	@Test public void testJpaTimerCacheStore() throws SQLException {
		assertNotNull(cs);
		assertTrue(cs.getNamedQueryLoadAllKeys().equals(cs.LOAD_ALL_KEYS));
		assertTrue(cs.getNamedQueryLoadByKey().equals(cs.LOAD_BY_KEY));
		
		// check if the db has been initialized
		super.checkTable(TABLE_TIMERCACHE);
		super.checkTable(TABLE_TIMEREVENT);
	}
	
	@Test public void testStore() throws SQLException {
		super.checkStore(cs, TABLE_TIMERCACHE, new TimerEvent(1234567, "i1", "t1"));
	}

	@Test public void testLoad() throws SQLException {
		// fill table
		TimerEvent event = (TimerEvent) super.checkLoad(cs);
		assertEquals(event.getInstanceId(), "instance_2");
		assertEquals(event.getTaskId(), "taskid_2");
		assertEquals(event.getTime(), 1234567890);
	}

	@Test public void testLoadAll() throws SQLException {
		super.checkLoadAll(cs);
	}

	@Test public void testLoadAllKeys() throws SQLException {
		super.checkLoadAllKeys(cs);
	}

	@Test public void testStoreAll() throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		super.checkStoreAll(cs, new TimerEvent(1234567, "i1", "t1"));
	}

	@Test public void testDelete() throws SQLException {
		super.checkDelete(cs, TABLE_TIMERCACHE);
	}

	@Test public void testDeleteAll() throws SQLException {
		super.checkDeleteAll(cs);
	}

}
