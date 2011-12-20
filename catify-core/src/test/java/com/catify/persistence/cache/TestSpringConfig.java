package com.catify.persistence.cache;

import java.sql.SQLException;

import org.junit.Test;

public class TestSpringConfig extends JpaPersistenceTestHelper {

	public TestSpringConfig() throws ClassNotFoundException, SQLException{
		super();
		System.out.println("construtor ----------------> ");
	}
	
	@Test public void testSpring(){
		assertNotNull(context);
		assertNotNull(applicationContext.getBean("jpaNodeCacheStore"));
	}

}
