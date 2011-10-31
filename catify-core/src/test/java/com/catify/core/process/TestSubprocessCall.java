package com.catify.core.process;

import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.event.impl.beans.StateEvent;
import com.catify.core.testsupport.SpringTestBase;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestSubprocessCall extends SpringTestBase {

	@EndpointInject(uri = "mock://p1")
	private MockEndpoint p1;
	
	@EndpointInject(uri = "mock://p2")
	private MockEndpoint p2;
	
	private IMap<String, StateEvent> cache = Hazelcast.getMap(CacheConstants.NODE_CACHE);
	
	@Override
	public void setUp() throws Exception{
		super.setUp();
		cache.clear();
	}
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		cache.clear();
	}
	
	@Test
	public void testCallSubProcess() throws InterruptedException{
		this.deployProcess();
		
		p1.setExpectedMessageCount(1);
		p2.setExpectedMessageCount(1);
		
		System.out.println("sending message -------------------------------------------->");
		template.sendBody("seda:init_process01", this.getXml());
		
		assertMockEndpointsSatisfied(50, TimeUnit.SECONDS);
		
		String id1 = (String) p1.getReceivedExchanges().get(0).getIn().getHeader(MessageConstants.INSTANCE_ID);
		String id2 = (String) p2.getReceivedExchanges().get(0).getIn().getHeader(MessageConstants.INSTANCE_ID);
		
		assertEquals(id1, id2);
	}
	
	private String getProcess01(){
		return " <process processVersion=\"1.0\" processName=\"process01\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda:init_process01\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"call_process02\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"seda:init_process02\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<request ns:name=\"mock_p1\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock:p1\"/>\n" +
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
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"call_process01\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"seda:call_process01\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<request ns:name=\"mock_p2\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock:p2\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private void deployProcess(){
		super.deployProcess(getProcess01());
		super.deployProcess(getProcess02());
	}

}
