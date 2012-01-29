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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.processors.ReadCorrelationProcessor;
import com.catify.core.testsupport.SpringTestBase;
import com.hazelcast.core.Hazelcast;

public class TestReceiveNode extends SpringTestBase {

	
	@EndpointInject(uri = "mock://out")
	private MockEndpoint out;
	
	@EndpointInject(uri = "mock://timeout")
	private MockEndpoint timeout;
	
	@Override
	public void setUp() throws Exception{
		super.setUp();
		
		out.reset();
		Hazelcast.getMap(CacheConstants.TIMER_CACHE).clear();
	}
	
	@Override
	public void tearDown() throws Exception{
		super.tearDown();
		
		out.reset();
		Hazelcast.getMap(CacheConstants.TIMER_CACHE).clear();
	}
	
	/**
	 * the process waits for an incoming message
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveWithWait() throws Exception{		
		this.deploy();
		this.createAdditionalRoutes(1500);
		
		out.setExpectedMessageCount(1);
		timeout.setExpectedMessageCount(0);
		
		//send 1. message 
		template.sendBody("seda://init_process", super.getXml());
		
		Thread.sleep(1500);
		
		//send 2. message
		template.sendBody("seda://in", super.getXml());
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
	@Test
	public void testReceiveWithoutWait() throws Exception{		
		this.deploy();
		this.createAdditionalRoutes(500);
		
		out.setExpectedMessageCount(1);
		timeout.setExpectedMessageCount(0);		
		
		//send 1. message 
		template.sendBody("seda://init_process", super.getXml());
		
		//send 2. message
		template.sendBody("seda://in", super.getXml());
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test public void testReceiveTimeOut() throws Exception{		
		this.deploy();
		this.createAdditionalRoutes(6000);
		
		out.setExpectedMessageCount(0);
		timeout.setExpectedMessageCount(1);
		
		//send message 
		template.sendBody("seda:init_process", super.getXml());
		
		assertMockEndpointsSatisfied(15, TimeUnit.SECONDS);
		
	}
	
	@Test public void testPayloadHandling() throws InterruptedException{
		super.deployProcess(this.getProcess02());
		
		out.setExpectedMessageCount(1);
		out.expectedBodiesReceived(this.getXml());
		
		//init process
		System.out.println("Message 1 ---------------------");
		template.sendBody("seda:init_process02", this.getXml());
		
		Thread.sleep(500);
		
		//send payload
		System.out.println("Message 2 ---------------------");
		template.sendBody("seda:in", this.getXml());
		
		assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);
	}
	
	@Test public void testCorrelationException() throws InterruptedException {
		super.deployProcess(this.processCorrelationException());
		
		out.setExpectedMessageCount(1);
		out.expectedBodiesReceived(this.getNumberXml());
		
		template.sendBody("seda://init", this.getXml());		
		Thread.sleep(500);
		template.sendBody("seda://in", this.getNumberXml());
		
		assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);
	}
	
	/**
	 * 
	 * 
	 * @throws Exception
	 */
	@Test public void testCorrelationExceptionWithReCall() throws Exception {
		super.deployProcess(this.processReCallProcessAfterCorrelationException());
		this.createAdditionalRoutes(1000);
		
		out.setExpectedMessageCount(1);
		out.expectedBodiesReceived(this.getNumberXml());
		
		template.sendBody("seda://init", this.getXml());
		Thread.sleep(1000);
		template.sendBody("seda:in", this.getNumberXml());
		
		// give the process time to throw an exception again (it should not ;)
		Thread.sleep(10000);
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
		
	}
	
	private void createAdditionalRoutes(final int delay) throws Exception{

		RouteBuilder builder = new RouteBuilder(){

			@Override
			public void configure() throws Exception {
				from("seda:out")
				.delay(delay)
				.log("-------------------------------------------> returned message")
				.to("seda:in");	
				
				from("activemq:queue:corr_ex")
				.to("log:CORR_EX?showAll=true")
				.setHeader(ReadCorrelationProcessor.CORRELATION_EXCEPTION_HEADER, constant(""))
				.to("seda:init")
				.to("log:RECALL?showAll=true")
				.delay(delay)
				.to("seda:in")
				.to("log:SEND_MESSAGE_AGAIN?showAll=true");
				
			}			
		};
		
		context.addRoutes(builder);
	}
	
	private ProcessDefinition deploy(){	
		return super.deployProcess(this.getProcess01());
	}
	
	private String getProcess01(){
		return " <process processVersion=\"1.0\" processName=\"process01\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda:init_process\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"send_message\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"seda:out\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<sleep>\n" +
				"		<timeEvent ns:time=\"1000\">\n" +
				"			<end/>\n" +
				"		</timeEvent>\n" +
				"	</sleep>\n" +
				"	<receive ns:name=\"wait_for_answer\">\n" +
				"		<timeEvent ns:time=\"3000\">\n" +
				"			<request ns:name=\"mock_timeout\">\n" +
				"				<outPipeline>\n" +
				"					<endpoint ns:uri=\"mock:timeout\"/>\n" +
				"				</outPipeline>\n" +
				"			</request>\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda:in\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</receive>\n" +
				"	<request ns:name=\"mock\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock://out\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private String getProcess02(){
		return 	" <process processVersion=\"1.0\" processName=\"process02\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda:init_process02\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<receive ns:name=\"wait_for_payload\">\n" +
				"		<timeEvent ns:time=\"3000\">\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda:in\"/>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" ns:xpath=\"/\" />\n" +
				"			</variables>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</receive>\n" +
				"	<request ns:name=\"mock_p2\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock://out\"/>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" />\n" +
				"			</variables>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private String processCorrelationException(){
		return 	" <process processVersion=\"1.0\" processName=\"process02\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
		"	<start ns:name=\"start\">\n" +
		"		<inPipeline>\n" +
		"			<endpoint ns:uri=\"seda:init\"/>\n" +
		"			<correlation>\n" +
		"				<xpath>/foo/a</xpath>\n" +
		"				<xpath>/foo/b</xpath>\n" +
		"			</correlation>\n" +
		"		</inPipeline>\n" +
		"	</start>\n" +
		"	<receive ns:name=\"wait_for_payload\">\n" +
		"		<inPipeline>\n" +
		"			<pipelineExceptionEvent ns:uri=\"mock:out\" ns:attachPayload=\"true\"/>\n" +
		"			<endpoint ns:uri=\"seda:in\"/>\n" +
		"			<variables>\n" +
		"				<variable ns:name=\"foo\" ns:xpath=\"/\" />\n" +
		"			</variables>\n" +
		"			<correlation>\n" +
		"				<xpath>/foo/a</xpath>\n" +
		"				<xpath>/foo/b</xpath>\n" +
		"			</correlation>\n" +
		"		</inPipeline>\n" +
		"	</receive>\n" +
		"	<end ns:name=\"end\"/>\n" +
		"</process>";
		
	}
	
	private String processReCallProcessAfterCorrelationException(){
		return 	" <process processVersion=\"1.0\" processName=\"process01\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
		"	<start ns:name=\"start\">\n" +
		"		<inPipeline>\n" +
		"			<endpoint ns:uri=\"seda:init\"/>\n" +
		"			<correlation>\n" +
		"				<xpath>/foo/a</xpath>\n" +
		"				<xpath>/foo/b</xpath>\n" +
		"			</correlation>\n" +
		"		</inPipeline>\n" +
		"	</start>\n" +
		"	<receive ns:name=\"wait_for_payload\">\n" +
		"		<inPipeline>\n" +
		"			<pipelineExceptionEvent ns:uri=\"activemq:queue:corr_ex?transferExchange=true\" ns:attachPayload=\"true\"/>\n" +
		"			<endpoint ns:uri=\"seda:in\"/>\n" +
		"			<variables>\n" +
		"				<variable ns:name=\"foo\" ns:xpath=\"/\" />\n" +
		"			</variables>\n" +
		"			<correlation>\n" +
		"				<xpath>/foo/a</xpath>\n" +
		"				<xpath>/foo/b</xpath>\n" +
		"			</correlation>\n" +
		"		</inPipeline>\n" +
		"	</receive>\n" +
		"	<request ns:name=\"mock_p2\">\n" +
		"		<outPipeline>\n" +
		"			<endpoint ns:uri=\"mock://out\"/>\n" +
		"			<variables>\n" +
		"				<variable ns:name=\"foo\" />\n" +
		"			</variables>\n" +
		"		</outPipeline>\n" +
		"	</request>\n" +
		"	<end ns:name=\"end\"/>\n" +
		"</process>";
	}

}
