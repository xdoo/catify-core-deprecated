package com.catify.core.process;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;

import com.catify.core.constants.MessageConstants;
import com.catify.core.testsupport.SpringTestBase;

public class TestSubprocessCall extends SpringTestBase {

	@EndpointInject(uri = "mock://p1")
	private MockEndpoint p1;
	
	@EndpointInject(uri = "mock://p2")
	private MockEndpoint p2;
	
	
	public void testCallSubProcess() throws InterruptedException{
		this.deployProcess();
		
		p1.setExpectedMessageCount(1);
		p2.setExpectedMessageCount(1);
		
		template.sendBody("seda:init_process01", this.getXml());
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
		
		String id1 = (String) p1.getReceivedExchanges().get(0).getIn().getHeader(MessageConstants.INSTANCE_ID);
		String id2 = (String) p2.getReceivedExchanges().get(0).getIn().getHeader(MessageConstants.INSTANCE_ID);
		
		assertEquals(id1, id2);
	}
	
	private String getProcess01(){
		return " <process processVersion=\"1.0\" processName=\"process01\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<fromEndpoint><generic ns:uri=\"seda:init_process01\"/></fromEndpoint>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"call_process02\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint><generic ns:uri=\"seda:init_process02\"/></toEndpoint>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<receive ns:name=\"wait_for_answer_of_process02\">\n" +
				"		<timeEvent ns:time=\"30000\">\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"		<inPipeline>\n" +
				"			<fromEndpoint><generic ns:uri=\"seda:call_process01\"/></fromEndpoint>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</receive>\n" +
				"	<request ns:name=\"mock\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint><generic ns:uri=\"mock:p1\"/></toEndpoint>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private String getProcess02(){
		return 	" <process processVersion=\"1.0\" processName=\"process02\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<fromEndpoint><generic ns:uri=\"seda:init_process02\"/></fromEndpoint>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"call_process01\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint><generic ns:uri=\"seda:call_process01\"/></toEndpoint>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<request ns:name=\"mock\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint><generic ns:uri=\"mock:p2\"/></toEndpoint>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private void deployProcess(){
		
		String pid1 = ProcessHelper.createProcessId("CATIFY", "process01", "1.0");
		String pid2 = ProcessHelper.createProcessId("CATIFY", "process02", "1.0");
		
		List<String> ids = new ArrayList<String>();
		ids.add(pid1);
		ids.add(ProcessHelper.createTaskId(pid1, "start"));
		ids.add(ProcessHelper.createTaskId(pid1, "call_process02"));
		ids.add(ProcessHelper.createTaskId(pid1, "wait_for_answer_of_process02"));
		ids.add(ProcessHelper.createTaskId(pid1, "mock"));
		
		super.deployProcess(getProcess01(), ids);
		
		ids.clear();
		ids.add(pid2);
		ids.add(ProcessHelper.createTaskId(pid2, "start"));
		ids.add(ProcessHelper.createTaskId(pid2, "call_process01"));
		ids.add(ProcessHelper.createTaskId(pid2, "mock"));
		
		super.deployProcess(getProcess02(), ids);
	}

}
