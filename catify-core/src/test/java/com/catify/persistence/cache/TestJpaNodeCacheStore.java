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

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import com.catify.core.event.impl.beans.StateEvent;

public class TestJpaNodeCacheStore extends JpaPersistenceTestHelper {	

	private final static String TABLE_NODECACHE = "NODECACHE";
	private final static String TABLE_STATEEVENT = "STATEEVENT";
	private JpaNodeCacheStore cs;
	
	public TestJpaNodeCacheStore() throws ClassNotFoundException, SQLException {
		
		super();
		
		
		super.addInsertStatement("INSERT INTO STATEEVENT VALUES ( 'id_%s', 2, 'instance_%s' )");
		super.addInsertStatement("INSERT INTO NODECACHE VALUES ( 'key_%s', 'id_%s' )");
		
		super.addCountSteatement("SELECT count(*) FROM NODECACHE");
		super.addCountSteatement("SELECT count(*) FROM STATEEVENT");

	}
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		cs = new JpaNodeCacheStore();
	}
	

	@Test public void testNodeJpaCacheStore() throws SQLException {
		assertNotNull(cs);
		assertTrue(cs.getNamedQueryLoadAllKeys().equals(cs.LOAD_ALL_KEYS));
		assertTrue(cs.getNamedQueryLoadByKey().equals(cs.LOAD_BY_KEY));
		
		// check if the db has been initialized
		super.checkTable(TABLE_NODECACHE);
		super.checkTable(TABLE_STATEEVENT);
	}
	
	@Test public void testStore() throws SQLException {
		// test with SQL-statement
		super.checkStore(cs, TABLE_NODECACHE, new StateEvent("1", 2));
		
		// test with JPQL-statement
//		super.checkStore(cs, "NodeCache", new StateEvent("1", 2));
	}

	@Test public void testLoad() throws SQLException {
		// fill table
		StateEvent event = (StateEvent) super.checkLoad(cs);
		assertEquals(event.getInstanceId(), "instance_2");
		assertEquals(event.getState(), 2);
	}
	
	@Test public void loadWithNoResult() throws SQLException {
		super.checkLoadWithNoResult(cs);
	}

	@Test public void testLoadAll() throws SQLException {
		super.checkLoadAll(cs);
	}

	@Test public void testLoadAllKeys() throws SQLException {
		super.checkLoadAllKeys(cs);
	}

	@Test public void testStoreAll() throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		super.checkStoreAll(cs, new StateEvent("2", 3));
	}

	@Test public void testDelete() throws SQLException {
		super.checkDelete(cs, TABLE_NODECACHE);
	}

	@Test public void testDeleteAll() throws SQLException {
		super.checkDeleteAll(cs);
	}
}
