package com.catify.core.camel;

import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.CamelTestSupport;

public class CamelTestDynamicRouter extends CamelTestSupport {
	
	public void testRouter() throws Exception {
		context.addRoutes(getRoutes());
		template.sendBody("direct:start", "foo");
	}

	private RouteBuilder getRoutes() {

		return new RouteBuilder() {
			
			MyRouter router = new MyRouter();

			@Override
			public void configure() throws Exception {

				from("direct:start")
				.dynamicRouter(bean(router, "route"))
				.to("log:A");

				from("direct:foo")
				.setBody(constant("bar"))
				.to("log:B")
				.to("seda:out");

			}
		};

	}

	public class MyRouter {

		public String route(Message message) {
			
			if (message.getBody(String.class).equals("foo")) {
				return "direct:foo";
			} else {
				return null;
			}
		}

	}
}
