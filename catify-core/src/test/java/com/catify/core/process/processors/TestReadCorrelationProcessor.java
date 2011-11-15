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
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.exceptions.CorrelationException;
import com.hazelcast.core.Hazelcast;

public class TestReadCorrelationProcessor extends CamelTestSupport {

	@EndpointInject(uri = "mock://out")
	private MockEndpoint out;
	
	@EndpointInject(uri = "mock://error")
	private MockEndpoint error;
	
	private String key = "";
	private String msg = "mycorrelationmessage";
	private String iid = "4711";
	
	@Override
	public void setUp() throws Exception{
		super.setUp();
		
		out.reset();
		error.reset();
		Hazelcast.getMap(CacheConstants.CORRELATION_CACHE).clear();
		
		// get md5 hash
		key = DigestUtils.md5Hex(msg);
		
		// prepare cache
		Hazelcast.getMap(CacheConstants.CORRELATION_CACHE).put(key, iid);
	}
	
	@Override
	public void tearDown() throws Exception{
		super.tearDown();
		
		out.reset();
		error.reset();
		Hazelcast.getMap(CacheConstants.CORRELATION_CACHE).clear();
	}
	
	/**
	 * test normal behavior: correlation message as input, 
	 * the instance id as header variable as output.
	 * 
	 * @throws InterruptedException
	 */
	@Test public void testReadInstanceId() throws InterruptedException {
		out.setExpectedMessageCount(1);
		out.expectedHeaderReceived(MessageConstants.INSTANCE_ID, iid);
		
		// send message
//		template.sendBody("direct://in", "mycorrelationmessage");
		Object body = template.requestBody("direct://in", "mycorrelationmessage");
		
		System.out.println("--------------> " + body);
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
	/**
	 * test if an exception will be thrown if no
	 * suitable correlation key will be found.
	 * 
	 * @throws InterruptedException
	 */
	@Test public void testFailtoReadInstanceId() throws InterruptedException {
		error.setExpectedMessageCount(1);
		out.setExpectedMessageCount(0);
		
		// send message
		template.sendBody("direct://in", "notacorrelationmessage");
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct://in")
				.doTry()
					.process(new ReadCorrelationProcessor())
					.to("mock://out")
				.doCatch(CorrelationException.class)
					.to("mock://error");
				
			}
		};
	}
	
}
