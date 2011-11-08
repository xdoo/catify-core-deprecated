package com.catify.core.process;

import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

import com.catify.core.constants.CacheConstants;
import com.catify.core.process.model.ProcessDefinition;
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
	
//	/**
//	 * the process waits for an incoming message
//	 * 
//	 * @throws Exception
//	 */
//	@Test
//	public void testReceiveWithWait() throws Exception{		
//		this.deploy();
//		this.createMessageCopyRoute(1500);
//		
//		out.setExpectedMessageCount(1);
//		timeout.setExpectedMessageCount(0);
//		
//		//send 1. message 
//		template.sendBody("seda:init_process", super.getXml());
//		
//		Thread.sleep(1500);
//		
//		//send 2. message
//		template.sendBody("seda:in", super.getXml());
//		
//		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
//	}
//	
//	
//	
//	@Test
//	public void testReceiveWithoutWait() throws Exception{		
//		this.deploy();
//		this.createMessageCopyRoute(500);
//		
//		out.setExpectedMessageCount(1);
//		timeout.setExpectedMessageCount(0);		
//		
//		//send 1. message 
//		template.sendBody("seda:init_process", super.getXml());
//		
//		//send 2. message
//		template.sendBody("seda:in", super.getXml());
//		
//		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
//	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReceiveTimeOut() throws Exception{		
		this.deploy();
		this.createMessageCopyRoute(6000);
		
		out.setExpectedMessageCount(0);
		timeout.setExpectedMessageCount(1);
		
		//send message 
		template.sendBody("seda:init_process", super.getXml());
		
		assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);
		
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
	
	private void createMessageCopyRoute(final int delay) throws Exception{

		RouteBuilder builder = new RouteBuilder(){

			@Override
			public void configure() throws Exception {
				from("seda:out")
				.delay(delay)
				.log("-------------------------------------------> returned message")
				.to("seda:in");	
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

}
