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

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import com.catify.core.constants.MessageConstants;

public class TestInitProcessProcessor extends CamelTestSupport {

	@Test
	public void testWithGivenId() throws Exception{
		context.addRoutes(getRoutes());
		template.sendBody("direct:with", "foo");
		Exchange exchange = consumer.receive("seda:out", 5000);
		assertEquals("4711", exchange.getIn().getHeader(MessageConstants.INSTANCE_ID));
	}
	
	@Test
	public void testWithoutId() throws Exception{
		context.addRoutes(getRoutes());
		template.sendBody("direct:without", "foo");
		Exchange exchange = consumer.receive("seda:out", 5000);
		assertNotNull(exchange.getIn().getHeader(MessageConstants.INSTANCE_ID));
	}
	
	private RouteBuilder getRoutes(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:with")
				.setHeader(MessageConstants.INSTANCE_ID, constant("4711"))
				.process(new InitProcessProcessor())
				.to("seda:out");
				
				from("direct:without")
				.process(new InitProcessProcessor())
				.to("seda:out");
				
			}
		};
	}
	
}
