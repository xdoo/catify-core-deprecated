package com.catify.core.process.processors;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestPipelineDeploymentProcessor extends CamelSpringTestSupport {

	@Override
	protected AbstractXmlApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	@Test
	public void testDeployRoute() throws InterruptedException{
		template.sendBody("restlet:http://localhost:9080/catify/deploy_pipeline/4711?restletMethod=post", this.getSimpleTestRoute());
		
		Thread.sleep(2000);
		assertNotNull(context.getRoute("testRoute-01"));
		
		MockEndpoint out = getMockEndpoint("mock:out");
		out.setExpectedMessageCount(1);
		
		template.sendBody("direct:in", "foo");
		
		out.assertIsSatisfied();
	}
	
	private String getSimpleTestRoute(){
		String route = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<routes xmlns=\"http://camel.apache.org/schema/spring\">" +
			" <route id=\"testRoute-01\">" +
			"	<from uri=\"direct:in\"/>" +
			"	<to   uri=\"mock:out\"/>" +
			" </route>" +
			"</routes>";
		
		return route;
	}
}
