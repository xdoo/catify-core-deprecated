package com.catify.core.rest;

import org.apache.camel.test.CamelSpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestSbK extends CamelSpringTestSupport {

	@Override
	protected AbstractXmlApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	public void testRestApi() throws InterruptedException{
		Thread.sleep(2*60*1000);
	}

}
