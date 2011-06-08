package com.catify.core.process;

import java.util.ArrayList;
import java.util.List;
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

	
	@EndpointInject(uri = "mock:out")
	private MockEndpoint out;
	
	@EndpointInject(uri = "mock:timeout")
	private MockEndpoint timeout;
	
	@Override
	public void setUp() throws Exception{
		super.setUp();
		
		Hazelcast.getMap(CacheConstants.TIMER_CACHE).clear();
	}
	
	@Override
	public void tearDown() throws Exception{
		super.tearDown();
		
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
		this.createMessageCopyRoute(1500);
		
		out.setExpectedMessageCount(1);
		timeout.setExpectedMessageCount(0);
		
		//send message 
		template.sendBody("seda:init_process", super.getXml());
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
	@Test
	public void testReceiveWithoutWait() throws Exception{		
		this.deploy();
		this.createMessageCopyRoute(500);
		
		out.setExpectedMessageCount(1);
		timeout.setExpectedMessageCount(0);
		
		
		//send message 
		template.sendBody("seda:init_process", super.getXml());
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}
	
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
		
		List<String> ids = new ArrayList<String>();
		
		String pid = ProcessHelper.createProcessId("CATIFY", "process01", "1.0");
		
		ids.add(pid);
		ids.add(ProcessHelper.createTaskId(pid, "start"));
		ids.add(ProcessHelper.createTaskId(pid, "send_message"));
		ids.add(ProcessHelper.createTaskId(pid, "wait_for_answer"));
		ids.add(ProcessHelper.createTaskId(pid, "mock"));
		ids.add(ProcessHelper.createTaskId(pid, "mock_timeout"));
		
		return super.deployProcess(this.getProcess(), ids);
	}
	
	private String getProcess(){
		return " <process processVersion=\"1.0\" processName=\"process01\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<fromEndpoint><generic ns:uri=\"seda:init_process\"/></fromEndpoint>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"send_message\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint><generic ns:uri=\"seda:out\"/></toEndpoint>\n" +
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
				"					<toEndpoint><generic ns:uri=\"mock:timeout\"/></toEndpoint>\n" +
				"				</outPipeline>\n" +
				"			</request>\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"		<inPipeline>\n" +
				"			<fromEndpoint><generic ns:uri=\"seda:in\"/></fromEndpoint>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</receive>\n" +
				"	<request ns:name=\"mock\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint><generic ns:uri=\"mock:out\"/></toEndpoint>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}

}
