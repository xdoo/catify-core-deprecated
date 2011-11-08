package com.catify.core.process;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

import com.catify.core.event.impl.beans.PayloadEvent;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.testsupport.SpringTestBase;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

/**
 * Tests if the fork node works correct.
 * 
 * @author claus
 *
 */
public class TestTerminateNode extends SpringTestBase {
	
	@Test
	public void testSleepNode(){

		ProcessDefinition definition = this.deploy();
		
		//send message 
		Map<String, Object> headers = this.setHeaders(definition);
		template.sendBodyAndHeaders("seda:init_process", super.getXml(), headers);
		
		IMap<String, Object> map = Hazelcast.getMap("payload-cache");
		PayloadEvent pe = new PayloadEvent();
		pe.setInstanceId("1");
		pe.setPayload("TESTPAYLOAD");
		map.put("1", pe);
		
		//process sleeps 1 second...
		Exchange ex1 = consumer.receive("seda:out", 50000);

		
		

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
				"	<request ns:name=\"mock\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"seda:out\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<request ns:name=\"send_to_hazelcast\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"hazelcast:map:foo\"/>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" />\n" +
				"				<variable ns:name=\"bar\" />\n" +
				"			</variables>\n" +
				"		</outPipeline>\n" +
				"	</request>" +
				
				"	<terminate ns:name=\"terminate\"/>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
}
