package com.catify.core.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.Test;

import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.testsupport.SpringTestBase;

public class TestWaitNode extends SpringTestBase {

	@Test
	public void testSleepNode(){

		ProcessDefinition definition = this.deploy();
		
		//send message 
		Map<String, Object> headers = this.setHeaders(definition);
		template.sendBodyAndHeaders("seda:init_process", super.getXml(), headers);
		
		//process sleeps 1 second...
		Exchange ex1 = consumer.receive("seda:out", 500);
		assertNull(ex1);
		
		Exchange ex2 = consumer.receive("seda:out", 5000);
		assertNotNull(ex2);
	}
	
	private ProcessDefinition deploy(){
		return super.deployProcess(this.getProcess());
	}
	
	private String getProcess(){
		return " <process processVersion=\"1.0\" processName=\"process01\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda:init_process\"/>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<sleep>\n" +
				"		<timeEvent ns:time=\"1000\">\n" +
				"			<end/>\n" +
				"		</timeEvent>\n" +
				"	</sleep>\n" +
				"	<request ns:name=\"mock\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"seda:out\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
}
