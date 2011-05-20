package com.catify.core.camel;

import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelTestSupport;

import com.catify.core.testsupport.beans.Foo;
import com.hazelcast.core.Hazelcast;

public class CamelHazelcastSedaTest extends CamelTestSupport {

	@EndpointInject(uri = "mock://out")
	private MockEndpoint out;
	
	public void setUp() throws Exception {
		super.setUp();
		out.reset();
		Hazelcast.getQueue("foo").clear();
	}
	
	public void tearDown() throws Exception {
		super.tearDown();
		out.reset();
		Hazelcast.getQueue("foo").clear();
	}
	
	public void testComplexMessageWithHeaders() throws InterruptedException{
		
		Foo foo = new Foo("foo", 100);
		
		out.expectedMessageCount(1);
		out.expectedHeaderReceived("my-foo", "foo_");
		
		template.sendBodyAndHeader("direct://foo", foo, "my-foo", "foo_");
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
		
		Foo result = out.getExchanges().get(0).getIn().getBody(Foo.class);
		
		assertEquals("foo", result.getFoo());
		assertEquals(100, result.getBar());
	}
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {

		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				from("direct://foo")
				.toF("hazelcast:%sfoo", HazelcastConstants.SEDA_PREFIX);
				
				fromF("hazelcast:%sfoo", HazelcastConstants.SEDA_PREFIX)
				.to("mock://out");

			}
		};
	}

}
