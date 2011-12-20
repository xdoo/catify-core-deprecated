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

public class TestJpaPayloadCacheStore extends JpaPersistenceTestHelper {

	private final static String TABLE_PAYLOADCACHE = "PAYLOADCACHE";
	private JpaPayloadCacheStore cs;
	
	public TestJpaPayloadCacheStore() throws ClassNotFoundException, SQLException {
		
		super();
		
		super.addInsertStatement("INSERT INTO PAYLOADCACHE VALUES ( 'key_%s', '"+getPayload(1)+"' )");
		super.addCountSteatement(String.format("SELECT count(*) FROM %s", TABLE_PAYLOADCACHE));
		
	}
	
	@Before
	public void setUp() throws Exception {
		cs = new JpaPayloadCacheStore();
		super.setUp();
	}
	
	@Test public void testPayloadJpaCacheStore() throws SQLException {
		assertNotNull(cs);
		assertTrue(cs.getNamedQueryLoadAllKeys().equals(cs.LOAD_ALL_KEYS));
		assertTrue(cs.getNamedQueryLoadByKey().equals(cs.LOAD_BY_KEY));
		
		// check if the db has been initialized
		super.checkTable(TABLE_PAYLOADCACHE);
	}
	
	@Test public void testStore() throws SQLException {
		super.checkStore(cs, TABLE_PAYLOADCACHE, getPayload(1));
	}

	@Test public void testLoad() throws SQLException {
		// fill table
		String payload = (String) super.checkLoad(cs);
		assertEquals(getPayload(1), payload);
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
		super.checkStoreAll(cs, getPayload(1));
	}

	@Test public void testDelete() throws SQLException {
		super.checkDelete(cs, TABLE_PAYLOADCACHE);
	}

	@Test public void testDeleteAll() throws SQLException {
		super.checkDeleteAll(cs);
	}
	
	// ---
	
	public static String getPayload(int number) {
		return "<document number=\""+ number +"\">\n" +
				"  <address type=\"home\">\n" +
				"    <streetname>mainstreet</streetname>\n" +
				"    <streetnumber>34</streetnumber>\n" +
				"    <city>main-city</city>" +
				"    <postalcode>12345</postalcode>\n" +
				"  </address>\n" +
				"  <items>\n" +
				"    <item id=\"4711\">\n" +
				"	    <price>45.23</price>\n" +
				"		<currency>euro</currency>" +
				"		<quantity>300</quantity>\n" +
				"    </item>\n" +
				"    <item id=\"4712\">\n" +
				"	    <price>45.23</price>\n" +
				"		<currency>euro</currency>" +
				"		<quantity>300</quantity>\n" +
				"    </item>\n" +
				"    <item id=\"4713\">\n" +
				"	    <price>45.23</price>\n" +
				"		<currency>euro</currency>" +
				"		<quantity>300</quantity>\n" +
				"    </item>\n" +
				"    <item id=\"4714\">\n" +
				"	    <price>45.23</price>\n" +
				"		<currency>euro</currency>" +
				"		<quantity>300</quantity>\n" +
				"    </item>\n" +
				"    <item id=\"4715\">\n" +
				"	    <price>45.23</price>\n" +
				"		<currency>euro</currency>" +
				"		<quantity>300</quantity>\n" +
				"    </item>\n" +
				"    <item id=\"4716\">\n" +
				"	    <price>45.23</price>\n" +
				"		<currency>euro</currency>" +
				"		<quantity>300</quantity>\n" +
				"    </item>\n" +
				"    <item id=\"4717\">\n" +
				"	    <price>45.23</price>\n" +
				"		<currency>euro</currency>" +
				"		<quantity>300</quantity>\n" +
				"    </item>\n" +
				"  </items>\n" +
				"</document>";
	}

}
