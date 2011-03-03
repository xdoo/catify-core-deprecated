package com.catify.core.camel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.test.CamelTestSupport;

public class CamelRouteLoaderTest extends CamelTestSupport {

	public void testLoadXmlRoute() throws Exception{
		
		InputStream is = new ByteArrayInputStream(this.getSimpleTestRoute().getBytes());
		RoutesDefinition routes = context.loadRoutesDefinition(is);
		context.addRouteDefinitions(routes.getRoutes());
		
		assertNotNull(context.getRoute("testRoute-01"));
		
		MockEndpoint out = super.getMockEndpoint("mock:out");
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
