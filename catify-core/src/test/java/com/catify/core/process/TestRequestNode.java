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
package com.catify.core.process;

import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

import com.catify.core.constants.CacheConstants;
import com.catify.core.testsupport.SpringTestBase;
import com.hazelcast.core.Hazelcast;

public class TestRequestNode extends SpringTestBase{

	@EndpointInject(uri = "mock://out1")
	private MockEndpoint out1;
	
	@EndpointInject(uri = "mock://out2")
	private MockEndpoint out2;
	
	@EndpointInject(uri = "mock://out3")
	private MockEndpoint out3;
	
	@Override
	public void setUp() throws Exception{
		super.setUp();
		
		out1.reset();
		out2.reset();
		out3.reset();
		Hazelcast.getMap(CacheConstants.TIMER_CACHE).clear();
	}
	
	@Override
	public void tearDown() throws Exception{
		super.tearDown();
		
		out1.reset();
		out2.reset();
		out3.reset();
		Hazelcast.getMap(CacheConstants.TIMER_CACHE).clear();
	}
	
	/**
	 * test sending of message with payload from a variable
	 * 
	 * @throws InterruptedException
	 */
	@Test public void testSendMessage() throws InterruptedException{
		super.deployProcess(this.getProcess01());
		
		out1.expectedMessageCount(1);
		out1.expectedBodiesReceived(super.getXml());
		
		template.sendBody("seda:init_process01", super.getXml());
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
	@Test public void testForkMode() throws InterruptedException {
		super.deployProcess(this.getProcess02());
		
		out1.expectedMessageCount(0);
		out2.expectedMessageCount(1);
		out3.expectedMessageCount(1);
		
		out2.expectedBodiesReceived(super.getXml());
		out3.expectedBodiesReceived(super.getXml());
		
		template.sendBody("seda:init_process02", super.getXml());
		template.sendBody("seda:in_line2", super.getXml());
		
		Thread.sleep(500);
		
		template.sendBody("seda:in_line2", super.getXml());
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
	private String getProcess01(){
		return 	" <process processVersion=\"1.0\" processName=\"process01\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda://init_process01\"/>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" ns:xpath=\"/\" />\n" +
				"			</variables>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"mock_p2\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock://out1\"/>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" />\n" +
				"			</variables>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private String getProcess02(){
		return 	" <process processVersion=\"1.0\" processName=\"process02\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda://init_process02\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"			</correlation>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" ns:xpath=\"/\" />\n" +
				"			</variables>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"   <fork ns:name=\"f1\" ns:receivingLines=\"1\">\n" +
				"	  <line ns:name=\"1\">\n" +
				"		<receive ns:name=\"wait_line1\">\n" +
				"			<inPipeline>\n" +
				"				<endpoint ns:uri=\"seda:in_line1\"/>\n" +
				"				<correlation>\n" +
				"					<xpath>/foo/a</xpath>\n" +
				"					<xpath>/foo/b</xpath>\n" +
				"				</correlation>\n" +
				"			</inPipeline>\n" +
				"		</receive>\n" +
				"		<request ns:name=\"write_line1\">\n" +
				"			<outPipeline>\n" +
				"				<endpoint ns:uri=\"mock:out1\"/>\n" +
				"				<variables>\n" +
				"					<variable ns:name=\"foo\" />\n" +
				"				</variables>\n" +
				"			</outPipeline>\n" +
				"		</request>\n" +
				"	  </line>\n" +
				"	  <line ns:name=\"2\">\n" +
				"		<receive ns:name=\"wait_line2\">\n" +
				"			<inPipeline>\n" +
				"				<endpoint ns:uri=\"seda:in_line2\"/>\n" +
				"				<correlation>\n" +
				"					<xpath>/foo/a</xpath>\n" +
				"					<xpath>/foo/b</xpath>\n" +
				"				</correlation>\n" +
				"			</inPipeline>\n" +
				"		</receive>\n" +
				"		<request ns:name=\"write_line2\">\n" +
				"			<outPipeline>\n" +
				"				<endpoint ns:uri=\"mock:out2\"/>\n" +
				"				<variables>\n" +
				"					<variable ns:name=\"foo\" />\n" +
				"				</variables>\n" +
				"			</outPipeline>\n" +
				"		</request>\n" +
				"	  </line>\n" +
				"	</fork>\n" +
				"	<request ns:name=\"mock_p2\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock://out3\"/>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" />\n" +
				"			</variables>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
}
