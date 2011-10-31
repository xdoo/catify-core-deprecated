package com.catify.core.routes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

import com.catify.core.constants.CacheConstants;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.testsupport.SpringTestBase;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestStartupRoutes extends SpringTestBase {

	@EndpointInject(uri = "mock:out")
	private MockEndpoint out;
	
	@Test
	public void testpipelineLoad() throws InterruptedException{
		
		ProcessDefinition d1 = super.getProcessDefinition(this.getProcess("process01"));
		ProcessDefinition d2 = super.getProcessDefinition(this.getProcess("process02"));
		ProcessDefinition d3 = super.getProcessDefinition(this.getProcess("process03"));
		
		IMap<String, ProcessDefinition> pc = Hazelcast.getMap(CacheConstants.PROCESS_CACHE);
		
		pc.put(d1.getProcessId(), d1);
		pc.put(d2.getProcessId(), d2);
		pc.put(d3.getProcessId(), d3);
		
		//create xslt's
		List<String> names = new ArrayList<String>();
		names.add("start");
		names.add("mock");
		
		out.setExpectedMessageCount(3);
		
		template.sendBody("seda://load_process_definitions", "");
		
		//wait until all routes are registered
		Thread.sleep(10000);
		
		template.sendBody("seda://init_process01", super.getXml());
		template.sendBody("seda://init_process02", super.getXml());
		template.sendBody("seda://init_process03", super.getXml());
		
		assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);
	}
	
	private String getProcess(String name){
		return " <process processVersion=\"1.0\" processName=\""+name+"\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda:init_"+name+"\"/>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"mock\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock:out\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
}
