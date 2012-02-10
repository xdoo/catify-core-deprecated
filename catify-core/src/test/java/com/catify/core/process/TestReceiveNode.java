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
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.processors.ReadCorrelationProcessor;
import com.catify.core.testsupport.SpringTestBase;
import com.hazelcast.core.Hazelcast;

public class TestReceiveNode extends SpringTestBase {

	
	@EndpointInject(uri = "mock://out")
	private MockEndpoint out;
	
	@EndpointInject(uri = "mock://timeout")
	private MockEndpoint timeout;
	
	@EndpointInject(uri = "mock://timeout01")
	private MockEndpoint timeout01;
	
	@EndpointInject(uri = "mock://timeout02")
	private MockEndpoint timeout02;
	
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
	
	@Test public void testTimeoutInsideFork() throws InterruptedException{
		super.deployProcess(this.processReceiveWithTimeoutInFork());
		
		timeout02.setExpectedMessageCount(1);
		timeout02.expectedBodiesReceived(this.getXml());
		timeout01.setExpectedMessageCount(0);
		
		template.sendBody("seda://init", this.getXml());
		Thread.sleep(4000);
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
	@Test public void testBamProcess() throws Exception {
		super.deployProcess(this.processBam01());
		super.deployProcess(this.processBam00());
		this.createAdditionalRoutes(1000);
		
		String queue = "activemq:queue://data_tp1.out.process00_Bitmarck_Eingang_initialData";
		
		timeout02.setExpectedMessageCount(1);
//		timeout02.expectedBodiesReceived(this.getXml(), this.getNumberXml());
		timeout01.setExpectedMessageCount(1);
		
		template.sendBody(queue, this.getXml("x", "y"));
//		template.sendBody(queue, this.getXml("q", "n"));
//		template.sendBody(queue, this.getXml("v", "p"));
//		template.sendBody(queue, this.getXml("l", "k"));
//		template.sendBody(queue, this.getXml("r", "c"));
//		template.sendBody(queue, this.getXml("f", "w"));
//		template.sendBody(queue, this.getXml("g", "b"));
//		template.sendBody(queue, this.getXml("h", "m"));
//		template.sendBody(queue, this.getXml("i", "j"));
//		template.sendBody(queue, this.getXml("o", "e"));
//		template.sendBody(queue, this.getXml("p", "l"));
		Thread.sleep(15000);
		
		assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);
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
				
				from("activemq:queue://timeout01")
				.to("mock://timeout01");
				
				from("activemq:queue://timeout02")
				.to("mock://timeout02");
				
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
		"			<pipelineExceptionEvent ns:uri=\"activemq:queue:corr_ex\" ns:attachPayload=\"true\"/>\n" +
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
	
	private String processReceiveWithTimeoutInFork(){
		return 	" <process processVersion=\"1.0\" processName=\"process01\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
		"	<start ns:name=\"start\">\n" +
		"		<inPipeline>\n" +
		"			<endpoint ns:uri=\"seda:init\"/>\n" +
		"			<variables>\n" +
		"				<variable ns:name=\"foo\" ns:xpath=\"/\" />\n" +
		"			</variables>\n" +
		"			<correlation>\n" +
		"				<xpath>/foo/a</xpath>\n" +
		"				<xpath>/foo/b</xpath>\n" +
		"			</correlation>\n" +
		"		</inPipeline>\n" +
		"	</start>\n" +
		"	<fork ns:name=\"fork01\" ns:receivingLines=\"1\">\n" +
		"	<line ns:name=\"line01\">\n" +
		"	<receive ns:name=\"wait_for_answer01\">\n" +
		"		<timeEvent ns:time=\"4000\">\n" +
		"			<request ns:name=\"mock_timeout01\">\n" +
		"				<outPipeline>\n" +
		"					<endpoint ns:uri=\"mock:timeout01\"/>\n" +
		"					<variables>\n" +
		"						<variable ns:name=\"foo\" />\n" +
		"					</variables>\n" +
		"				</outPipeline>\n" +
		"			</request>\n" +
		"			<end ns:name=\"end_time_out01\"/>\n" +
		"		</timeEvent>\n" +
		"		<inPipeline>\n" +
		"			<endpoint ns:uri=\"seda:in01\"/>\n" +
		"			<correlation>\n" +
		"				<xpath>/foo/a</xpath>\n" +
		"				<xpath>/foo/b</xpath>\n" +
		"			</correlation>\n" +
		"		</inPipeline>\n" +
		"	</receive>\n" +
		"	</line>\n" +
		"	<line ns:name=\"line02\">\n" +
		"		<receive ns:name=\"wait_for_answer02\">\n" +
		"			<timeEvent ns:time=\"2000\">\n" +
		"				<request ns:name=\"mock_timeout02\">\n" +
		"					<outPipeline>\n" +
		"						<endpoint ns:uri=\"mock:timeout02\"/>\n" +
		"						<variables>\n" +
		"							<variable ns:name=\"foo\" />\n" +
		"						</variables>\n" +
		"					</outPipeline>\n" +
		"				</request>\n" +
		"				<end ns:name=\"end_time_out02\"/>\n" +
		"			</timeEvent>\n" +
		"			<inPipeline>\n" +
		"				<endpoint ns:uri=\"seda:in02\"/>\n" +
		"				<correlation>\n" +
		"					<xpath>/foo/a</xpath>\n" +
		"					<xpath>/foo/b</xpath>\n" +
		"				</correlation>\n" +
		"			</inPipeline>\n" +
		"		</receive>\n" +
		"	</line>\n" +
		"	</fork>\n" +
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
	
	private String processBam00() {
		return "<process processVersion=\"1.0\" processName=\"process00\" accountName=\"SBK\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
			    "<start ns:name=\"start_process00_bitmarck_initialLoad\">\n" +
			        "<inPipeline>\n" +
			            "<endpoint ns:uri=\"activemq:queue://data_tp1.out.process00_Bitmarck_Eingang_initialData\"/>\n" +
			            "<variables>\n" +
			                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
			            "</variables>\n" +
			            "<correlation>\n" +
			                "<xpath>/foo/a</xpath>\n" +
			            "</correlation>\n" +
			        "</inPipeline>\n" +
			    "</start>\n" +
			    "<request ns:name=\"nextProcess_out_to_process01\">\n" +
			        "<outPipeline>\n" +
			            "<endpoint ns:uri=\"activemq:queue://data_tp1.out.process01\" />\n" +
			            "<variables>\n" +
			                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
			            "</variables>\n" +
			        "</outPipeline>\n" +
			    "</request>\n" +
			    "<request ns:name=\"final_out_process00_Bitmarck_Eingang_initialData\">\n" +
			        "<outPipeline>\n" +
			            "<endpoint ns:uri=\"activemq:queue://bam\"/>\n" +
			            "<variables>\n" +
			                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
			            "</variables>\n" +
			        "</outPipeline>\n" +
			    "</request>\n" +
			    "<end ns:name=\"end\"/>\n" +
			"</process>" ;
	}
	
	private String processBam01() {
		return "<process processVersion=\"1.0\" processName=\"process01\" accountName=\"SBK\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
	    "<start ns:name=\"start_process01\">\n" +
        "<inPipeline>\n" +
            "<endpoint ns:uri=\"activemq:queue://data_tp1.out.process01\"/>\n" +
            "<variables>\n" +
                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
            "</variables>\n" +
            "<correlation>\n" +
                "<xpath>/foo/a</xpath>\n" +
            "</correlation>\n" +
        "</inPipeline>\n" +
	    "</start>\n" +
	    "<fork ns:name=\"fork01\" ns:receivingLines=\"1\">\n" +
	        "<line ns:name=\"line1_fork1_bamTerminate\">\n" +
	            "<receive ns:name=\"listenFor_terminate_process01\">\n" +
	                "<inPipeline>\n" +
	                    "<endpoint ns:uri=\"activemq:queue://terminate_process01\"/>\n" +
	                    "<variables>\n" +
	                        "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                    "</variables>\n" +
	                    "<correlation>\n" +
	                        "<xpath>/foo/a</xpath>\n" +
	                    "</correlation>\n" +
	                "</inPipeline>\n" +
	            "</receive>\n" +
	            "<request ns:name=\"execute_terminate_process01\">\n" +
	                "<terminate ns:name=\"terminate\"/>\n" +
	                    "<variables>\n" +
	                        "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                    "</variables>\n" +
	            "</request>\n" +
	        "</line>\n" +
	        "<line ns:name=\"line2_fork1\">\n" +
	            "<fork ns:name=\"fork02\" ns:receivingLines=\"1\">\n" +
	                "<line ns:name=\"line1_fork2_bamError\">\n" +
	                    "<receive ns:name=\"process01_Bitmarck_Fehler\">\n" +
	                        "<inPipeline>\n" +
	                            "<endpoint ns:uri=\"activemq:queue://data_tp1.out.process01_Bitmarck_Fehler\"/>\n" +
	                            "<variables>\n" +
	                                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                            "</variables>\n" +
	                            "<correlation>\n" +
	                                "<xpath>/foo/a</xpath>\n" +
	                            "</correlation>\n" +
	                        "</inPipeline>\n" +
	                    "</receive>\n" +
	                    "<request ns:name=\"persistence_out_process01_Bitmarck_Fehler\">\n" +
	                        "<outPipeline>\n" +
	                            "<endpoint ns:uri=\"activemq:queue://bam\"/>\n" +
	                            "<variables>\n" +
	                                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                            "</variables>\n" +
	                        "</outPipeline>\n" +
	                    "</request>\n" +
	                "</line>\n" +
	                "<line ns:name=\"line2_fork2_bamreinitialization\">\n" +
	                    "<receive ns:name=\"process01_Bitmarck_Eingang_correctedData\">\n" +
	                        "<inPipeline>\n" +
	                            "<pipelineExceptionEvent ns:pipelineExceptionType=\"CorrelationException\" ns:uri=\"activemq:queue://data_tp1.out.process01_Bitmarck_Eingang_correctedData_reinitialization\" ns:attachPayload=\"true\" />\n" +
	                            "<endpoint ns:uri=\"activemq:queue://data_tp1.out.process01_Bitmarck_Eingang_correctedData\"/>\n" +
	                            "<variables>\n" +
	                                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                            "</variables>\n" +
	                            "<correlation>\n" +
	                                "<xpath>/foo/a</xpath>\n" +
	                            "</correlation>\n" +
	                        "</inPipeline>\n" +
	                    "</receive>\n" +
	                    "<request ns:name=\"persistence_out_process01_Bitmarck_Eingang_correctedData\">\n" +
	                        "<outPipeline>\n" +
	                            "<endpoint ns:uri=\"activemq:queue://bam\"/>\n" +
	                            "<variables>\n" +
	                                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                            "</variables>\n" +
	                        "</outPipeline>\n" +
	                    "</request>\n" +
	                "</line>\n" +
	                "<line ns:name=\"line3_fork2_bamReturn\">\n" +
	                    "<receive ns:name=\"process01_Bitmarck_Rueck\">\n" +
	                        "<timeEvent ns:time=\"3000\">\n" +
	                            "<request ns:name=\"timeout_process01_Bitmarck_Rueck\">\n" +
	                                "<outPipeline>\n" +
	                                    "<endpoint ns:uri=\"activemq:queue://timeout01\"/>\n" +
	                                    "<variables>\n" +
	                                        "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                                    "</variables>\n" +
	                                "</outPipeline>\n" +
	                            "</request>\n" +
	                            "<end ns:name=\"end_time_out01\"/>\n" +
	                        "</timeEvent>\n" +
	                        "<inPipeline>\n" +
	                            "<endpoint ns:uri=\"activemq:queue://data_tp1.out.process01_Bitmarck_Rueck\"/>\n" +
	                            "<variables>\n" +
	                                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                            "</variables>\n" +
	                            "<correlation>\n" +
	                                "<xpath>/foo/a</xpath>\n" +
	                            "</correlation>\n" +
	                        "</inPipeline>\n" +
	                    "</receive>\n" +
	                    "<request ns:name=\"persistence_out_process01_Bitmarck_Rueck\">\n" +
	                        "<outPipeline>\n" +
	                            "<endpoint ns:uri=\"activemq:queue://bam\"/>\n" +
	                            "<variables>\n" +
	                                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                            "</variables>\n" +
	                        "</outPipeline>\n" +
	                    "</request>\n" +
	                "</line>\n" +
	                "<line ns:name=\"line4_fork2_bamExit\">\n" +
	                    "<receive ns:name=\"process01_Bitmarck_Ausgang\">\n" +
	                        "<timeEvent ns:time=\"3000\">\n" +
	                            "<request ns:name=\"timeout_process01_Bitmarck_Ausgang\">\n" +
	                                "<outPipeline>\n" +
	                                    "<endpoint ns:uri=\"activemq:queue://timeout02\"/>\n" +
	                                    "<variables>\n" +
	                                        "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                                    "</variables>\n" +
	                                "</outPipeline>\n" +
	                            "</request>\n" +
	                            "<end ns:name=\"end_time_out02\"/>\n" +
	                        "</timeEvent>\n" +
	                        "<inPipeline>\n" +
	                            "<pipelineExceptionEvent ns:pipelineExceptionType=\"CorrelationException\" ns:uri=\"activemq:queue://data_tp1.out.process01_bitmarck_sendsData_reinitialization\" ns:attachPayload=\"true\" />\n" +
	                            "<endpoint ns:uri=\"activemq:queue://data_tp1.out.process01_Bitmarck_Ausgang\"/>\n" +
	                            "<variables>\n" +
	                                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                            "</variables>\n" +
	                            "<correlation>\n" +
	                                "<xpath>/foo/a</xpath>\n" +
	                            "</correlation>\n" +
	                        "</inPipeline>\n" +
	                    "</receive>\n" +
	                    "<request ns:name=\"persistence_out_process01_Bitmarck_Ausgang\">\n" +
	                        "<outPipeline>\n" +
	                            "<endpoint ns:uri=\"activemq:queue://bam\"/>\n" +
	                            "<variables>\n" +
	                                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                            "</variables>\n" +
	                        "</outPipeline>\n" +
	                    "</request>\n" +
	                    "<request ns:name=\"nextProcess_out_to_process02\">\n" +
	                        "<outPipeline>\n" +
	                            "<endpoint ns:uri=\"activemq:queue://data_tp1.out.process02\"/>\n" +
	                            "<variables>\n" +
	                                "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                            "</variables>\n" +
	                        "</outPipeline>\n" +
	                    "</request>\n" +
	                "</line>\n" +
	            "</fork>\n" +
	            "<request ns:name=\"reinitialize_process01\">\n" +
	            "<outPipeline>\n" +
	                "<endpoint ns:uri=\"activemq:queue://data_tp1.out.process01\"/>\n" +
	                "<variables>\n" +
	                    "<variable ns:name=\"payload\" ns:xpath=\"/\"/>\n" +
	                "</variables>\n" +
	            "</outPipeline>\n" +
	            "</request>\n" +
	        "</line>\n" +
	    "</fork>\n" +
	    "<end ns:name=\"end\"/>\n" +
	"</process>";
	}

}
