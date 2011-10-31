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
package com.catify.core.process.processors;

import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;


public class TestXPathProcessor extends CamelTestSupport {

	@EndpointInject(uri = "mock://out")
	private MockEndpoint out;
	
	@Test public void testXmlResult() throws InterruptedException{
		this.out.expectedMessageCount(1);
		this.out.expectedBodiesReceived("hello02");
		
		template.sendBodyAndHeader("direct://xml", this.xml(), XPathProcessor.XPATH, "/foo/bar2/text()");
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
	@Override
	protected RouteBuilder createRouteBuilder(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:xml")
				.process(new XPathProcessor())
				.to("mock://out");
				
			}
		};
	}
	
	private String xml(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<foo>\n" +
				"  <bar1>hello01</bar1>\n" +
				"  <bar2>hello02</bar2>\n" +
				"  <bar3>hello03</bar3>\n" +
				"</foo>";
	}
	
}
