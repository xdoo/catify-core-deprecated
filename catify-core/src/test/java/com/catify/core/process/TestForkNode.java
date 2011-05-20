package com.catify.core.process;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;

import com.catify.core.testsupport.SpringTestBase;

public class TestForkNode extends SpringTestBase {
	
	@EndpointInject(uri = "mock:out")
	private MockEndpoint out;
	
	@EndpointInject(uri = "mock:out_line1")
	private MockEndpoint outLine1;
	
	@EndpointInject(uri = "mock:out_line2")
	private MockEndpoint outLine2;
	
	@EndpointInject(uri = "mock:out_line3")
	private MockEndpoint outLine3;
	
	public void testReceiveAllLines() throws InterruptedException{

		this.deployProcess(-1);
		
		Thread.sleep(3000);
		
		outLine1.setExpectedMessageCount(1);
		outLine2.setExpectedMessageCount(1);
		outLine3.setExpectedMessageCount(1);
		out.setExpectedMessageCount(1);
		
//		init process
		template.sendBody("seda:start", this.getXml());
		Thread.sleep(1000);
		
//		send to lines
		template.sendBody("seda:in_line1", this.getXml());
		template.sendBody("seda:in_line2", this.getXml());
		template.sendBody("seda:in_line3", this.getXml());
		
		assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);
		
	}
	
	public void testReceiveTwoLines() throws InterruptedException{
		
		this.deployProcess(2);
		
		Thread.sleep(3000);
		
		outLine1.setExpectedMessageCount(1);
		outLine2.setExpectedMessageCount(0);
		outLine3.setExpectedMessageCount(1);
		out.setExpectedMessageCount(1);
		
//		init process
		template.sendBody("seda:start", this.getXml());
		Thread.sleep(1000);
		
//		send to lines (1 and 3)
		template.sendBody("seda:in_line1", this.getXml());
		template.sendBody("seda:in_line3", this.getXml());
		
//		check, what happens if we hit the 2. one after finishing the others
		Thread.sleep(2000);
		template.sendBody("seda:in_line2", this.getXml());
		
		Thread.sleep(2000);
		
		assertMockEndpointsSatisfied(15, TimeUnit.SECONDS);
		
	}
	
	private String getProcess(int awaitedHits){
		return " <process processVersion=\"1.0\" processName=\"process01\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >" +
				"<start ns:name=\"start\">\n" +
				"	<inPipeline>\n" +
				"		<fromEndpoint><generic ns:uri=\"seda:start\"/></fromEndpoint>\n" +
				"		<correlation>\n" +
				"			<xpath>/foo/a</xpath>\n" +
				"			<xpath>/foo/b</xpath>\n" +
				"		</correlation>\n" +
				"	</inPipeline>\n" +
				"</start>\n" +
				"<fork ns:name=\"fork01\" ns:receivingLines=\""+awaitedHits+"\">\n" +
				"	<line ns:name=\"1\">\n" +
				"		<receive ns:name=\"wait_line1\">\n" +
				"			<timeEvent ns:time=\"30000\">\n" +
				"				<end ns:name=\"end_time_out1\"/>\n" +
				"			</timeEvent>\n" +
				"			<inPipeline>\n" +
				"				<fromEndpoint><generic ns:uri=\"seda:in_line1\"/></fromEndpoint>\n" +
				"				<correlation>\n" +
				"					<xpath>/foo/a</xpath>\n" +
				"					<xpath>/foo/b</xpath>\n" +
				"				</correlation>\n" +
				"			</inPipeline>\n" +
				"		</receive>\n" +
				"		<request ns:name=\"write_line1\">\n" +
				"			<outPipeline>\n" +
				"				<toEndpoint><generic ns:uri=\"mock:out_line1\"/></toEndpoint>\n" +
				"			</outPipeline>\n" +
				"		</request>\n" +
				"	</line>\n" +
				"	<line ns:name=\"2\">\n" +
				"		<receive ns:name=\"wait_line2\">\n" +
				"			<timeEvent ns:time=\"30000\">\n" +
				"				<end ns:name=\"end_time_out2\"/>\n" +
				"			</timeEvent>\n" +
				"			<inPipeline>\n" +
				"				<fromEndpoint><generic ns:uri=\"seda:in_line2\"/></fromEndpoint>\n" +
				"				<correlation>\n" +
				"					<xpath>/foo/a</xpath>\n" +
				"					<xpath>/foo/b</xpath>\n" +
				"				</correlation>\n" +
				"			</inPipeline>\n" +
				"		</receive>\n" +
				"		<request ns:name=\"write_line2\">\n" +
				"			<outPipeline>\n" +
				"				<toEndpoint><generic ns:uri=\"mock:out_line2\"/></toEndpoint>\n" +
				"			</outPipeline>\n" +
				"		</request>\n" +
				"	</line>\n" +
				"	<line ns:name=\"3\">\n" +
				"		<receive ns:name=\"wait_line3\">\n" +
				"			<timeEvent ns:time=\"30000\">\n" +
				"				<end ns:name=\"end_time_out2\"/>\n" +
				"			</timeEvent>\n" +
				"			<inPipeline>\n" +
				"				<fromEndpoint><generic ns:uri=\"seda:in_line3\"/></fromEndpoint>\n" +
				"				<correlation>\n" +
				"					<xpath>/foo/a</xpath>\n" +
				"					<xpath>/foo/b</xpath>\n" +
				"				</correlation>\n" +
				"			</inPipeline>\n" +
				"		</receive>\n" +
				"		<request ns:name=\"write_line3\">\n" +
				"			<outPipeline>\n" +
				"				<toEndpoint><generic ns:uri=\"mock:out_line3\"/></toEndpoint>\n" +
				"			</outPipeline>\n" +
				"		</request>\n" +
				"	</line>\n" +
				"	</fork>\n" +
				"	<request ns:name=\"final_out\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint><generic ns:uri=\"mock:out\"/></toEndpoint>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>" +
				"</process>";
	}
	
	private void deployProcess(int awaitedHits){
		
		List<String> ids = new ArrayList<String>();
		ids.add("52c6c5cdc2e49838b6cb237e30f20fd7");
		ids.add("0767356e5a708d86d1f3900c40b108d9");
		ids.add("fcc962dd4b10713274b2c043a817d56c");
		ids.add("997a4c4ae03d2ad037c5bc96f7c8a320");
		ids.add("e01eeac36de03f7c1661b3c414b3b86e");
		ids.add("d1e92acffdd2339777444ea827abcf1a");
		ids.add("770758a8763397da3d6c6435c603d2fa");
		ids.add("d43eb28f540d9783c931242b0d7058ab");
		
		super.deployProcess(this.getProcess(awaitedHits), ids);
	}
	
}
