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

public class TestJpaCorrelationRuleCacheStore extends JpaPersistenceTestHelper {

	private final static String TABLE_CORRELATIONRULECACHE = "CORRELATIONRULECACHE";
	private JpaCorrelationRuleCacheStore cs;
	
	public TestJpaCorrelationRuleCacheStore() throws ClassNotFoundException, SQLException {
		super();
		
		super.addInsertStatement("INSERT INTO CORRELATIONRULECACHE VALUES ( '%s', '"+getCorrelationRule()+"', 'key_%s' )");
		super.addCountSteatement(String.format("SELECT count(*) FROM %s", TABLE_CORRELATIONRULECACHE));
	}
	
	@Before
	public void setUp() throws Exception {
		cs = new JpaCorrelationRuleCacheStore();
		super.setUp();
	}
	
	@Test public void testPayloadJpaCacheStore() throws SQLException {
		assertNotNull(cs);
		assertTrue(cs.getNamedQueryLoadAllKeys().equals(cs.LOAD_ALL_KEYS));
		assertTrue(cs.getNamedQueryLoadByKey().equals(cs.LOAD_BY_KEY));
		
		// check if the db has been initialized
		super.checkTable(TABLE_CORRELATIONRULECACHE);
	}
	
	@Test public void testStore() throws SQLException {
		super.checkStore(cs, TABLE_CORRELATIONRULECACHE, getCorrelationRule());
	}

	@Test public void testLoad() throws SQLException {
		// fill table
		String payload = (String) super.checkLoad(cs);
		assertEquals(getCorrelationRule(), payload);
	}

	@Test public void testLoadAll() throws SQLException {
		super.checkLoadAll(cs);
	}

	@Test public void testLoadAllKeys() throws SQLException {
		super.checkLoadAllKeys(cs);
	}

	@Test public void testStoreAll() throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		super.checkStoreAll(cs, getCorrelationRule());
	}

	@Test public void testDelete() throws SQLException {
		super.checkDelete(cs, TABLE_CORRELATIONRULECACHE);
	}

	@Test public void testDeleteAll() throws SQLException {
		super.checkDeleteAll(cs);
	}
	
	// ---
	
	public static String getCorrelationRule(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			   "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" exclude-result-prefixes=\"xs\">\n" +
			   " <xsl:output method=\"xml\" encoding=\"UTF-8\" indent=\"yes\"/>\n" +
			   " <xsl:template match=\"/\">\n" +
			   "     <xsl:variable name=\"var1_instance\" select=\".\"/>\n" +
			   "     <correlation>\n" +
			   "         <xsl:value-of select=\"concat(string($var1_instance/correlation/key))\"/>\n" +
			   "     </correlation>\n" +
			   " </xsl:template>\n" +
			   "</xsl:stylesheet> \n";
	}

}
