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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.component.jpa.JpaEndpoint;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.jpa.JpaTemplate;

import com.catify.persistence.beans.NodeCache;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;

public class JpaPersistenceTestHelper extends CamelSpringTestSupport {

	protected java.sql.Connection cn;
	private List<String> insertStatements = new ArrayList<String>();
	private List<String> countStatements = new ArrayList<String>();
	
	public JpaPersistenceTestHelper() throws ClassNotFoundException, SQLException {
		Class.forName( "com.mysql.jdbc.Driver" );
		cn = DriverManager.getConnection( "jdbc:mysql://172.17.16.65/Hazelcast_Persistenz", "test", "test" );
	}
	
	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.cleanTables();
	}

	@After
	public void tearDown() throws Exception {
		this.cleanTables();
	}
	
	protected void checkTable(String name) throws SQLException {		
		// check if the db has been initialized
		Statement statement = cn.createStatement();
		ResultSet rs = statement.executeQuery(String.format("SELECT count(*) FROM information_schema.tables WHERE table_schema = 'Hazelcast_Persistenz' AND table_name = '%s'", name));
		assertTrue(rs.first());
		assertTrue(rs.getInt(1) == 1);
	}
	
	protected void checkStore(MapStore<String, Object> store, String table, Object value) throws SQLException {
		store.store("4711", value);
		
		// wait for DB-update
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// check result with SQL-statement
		this.countRow(String.format("SELECT count(*) FROM %s WHERE BEANKEY = '4711'", table), 1);
		
		// check result with JPQL-statement
//		JpaEndpoint endpoint =
//				(JpaEndpoint) context.getEndpoint("jpa:com.catify.persistence.beans.NodeCache");
//				JpaTemplate jpaTemplate = endpoint.getTemplate();
//				
//				List list =
//						jpaTemplate.find(String.format("SELECT x FROM %s x", table));
//						
//				assertEquals(1, list.size());
//				assertIsInstanceOf(NodeCache.class, list.get(0));
		
	}

	protected Object checkLoad(MapLoader<String, Object> loader) throws SQLException {
		// fill table
		this.insertRows(this.insertStatements, 5);
		
		Object object = loader.load("key_2");
		
		assertNotNull(object);
		return object;
	}
	
	protected void checkLoadWithNoResult(MapLoader<String, Object> loader) throws SQLException {
		// fill table
		this.insertRows(this.insertStatements, 5);
		
		// there's no key 6 inside the db
		Object object = loader.load("key_6");
		
		assertNull(object);
	}

	protected void checkLoadAll(MapLoader<String, Object> loader) throws SQLException {
		// fill table
		this.insertRows(this.insertStatements, 5);
		
		List<String> keys = this.getKeyList();
		
		Map<String, Object> values = loader.loadAll(keys);
		
		assertNotNull(values);
		assertEquals(values.size(), 3);
		
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			Object o =  values.get((String) iterator.next());
			assertNotNull(o);
		}
	}

	protected void checkLoadAllKeys(MapLoader<String, Object> loader) throws SQLException {
		// fill table
		this.insertRows(this.insertStatements, 5);
		
		Set<String> keys = loader.loadAllKeys();
		
		assertNotNull(keys);
		assertEquals(5, keys.size());
		assertTrue(keys.contains("key_0"));
		assertTrue(keys.contains("key_1"));
		assertTrue(keys.contains("key_2"));
		assertTrue(keys.contains("key_3"));
		assertTrue(keys.contains("key_4"));
	}

	protected void checkStoreAll(MapStore<String, Object> store, Object value) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		Map<String, Object> input = new HashMap<String, Object>();
		
		input.put("0", BeanUtils.cloneBean(value));
		input.put("1", BeanUtils.cloneBean(value));
		input.put("2", BeanUtils.cloneBean(value));
		input.put("3", BeanUtils.cloneBean(value));
		input.put("4", BeanUtils.cloneBean(value));
		input.put("5", BeanUtils.cloneBean(value));
		input.put("6", BeanUtils.cloneBean(value));
		input.put("7", BeanUtils.cloneBean(value));
		input.put("8", BeanUtils.cloneBean(value));
		input.put("9", BeanUtils.cloneBean(value));
		
		store.storeAll(input);
		
		// check result
		this.countRows(10);
	}

	protected void checkDelete(MapStore<String, Object> store, String table) throws SQLException {
		// fill table
		this.insertRows(this.insertStatements, 5);
		
		this.countRows(5);
		this.countRow(String.format("SELECT count(*) FROM %s WHERE BEANKEY = 'key_3'", table), 1);
		
		store.delete("key_3");
		
		this.countRows(4);
		this.countRow(String.format("SELECT count(*) FROM %s WHERE BEANKEY = 'key_3'", table), 0);
	}

	protected void checkDeleteAll(MapStore<String, Object> store) throws SQLException {
		// fill table
		this.insertRows(this.insertStatements, 5);
		
		this.countRows(5);
		
		store.deleteAll(this.getKeyList());
		
		this.countRows(2);
	}
	
	protected void cleanTables() throws SQLException {
		Statement st = cn.createStatement();
		
		// clean tables
		st.executeUpdate("DELETE FROM CORRELATIONRULECACHE");
		st.executeUpdate("DELETE FROM CORRELATIONCACHE");
		st.executeUpdate("DELETE FROM PAYLOADCACHE");
		st.executeUpdate("DELETE FROM STATEEVENT");
		st.executeUpdate("DELETE FROM NODECACHE");
		st.executeUpdate("DELETE FROM TIMEREVENT");
		st.executeUpdate("DELETE FROM TIMERCACHE");
	}
	
	protected void countRows(int number) throws SQLException {
		
		
		Iterator<String> it = this.countStatements.iterator();
		
		while (it.hasNext()) {
			String sql= it.next();
			this.countRow(sql, number);	
		}
	}
	
	protected void countRow(String sql, int number) throws SQLException {
		Statement statement = cn.createStatement();
		ResultSet rs = statement.executeQuery(sql);
		assertTrue(rs.first());
		assertTrue(String.format("rowcount does not fit for statement '%s'", sql), rs.getInt(1) == number);
	}
	
	protected void insertRows(List<String> statements, int number) throws SQLException {
		Statement st = cn.createStatement();
		
		Iterator<String> it = statements.iterator();
		
		while (it.hasNext()) {
			String sql = it.next();
			
			for (int i = 0; i < number; i++) {
				st.executeUpdate(String.format(sql, i, i, i));
			}
			
		}
		

	}
	
	protected List<String> getKeyList(){
		List<String> keys = new ArrayList<String>();
		
		keys.add("key_1");
		keys.add("key_3");
		keys.add("key_4");
		
		return keys;
	}
	
	protected void addInsertStatement(String sql) {
		this.insertStatements.add(sql);
	}
	
	protected void addCountSteatement(String sql) {
		this.countStatements.add(sql);
	}
	
}
