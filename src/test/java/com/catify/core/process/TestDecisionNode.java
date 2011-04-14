package com.catify.core.process;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.QueueConstants;
import com.catify.core.process.builders.CatifyProcessBuilder;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.testsupport.ProcessBase;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestDecisionNode extends ProcessBase {

	@Override
	protected AbstractXmlApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	public void testDecisionNode(){
		
		//create process deployer
		ProcessDeployer deployer = new ProcessDeployer(context, this.createKnowledgeBase());
		assertNotNull(deployer);
		
		//deploy process
		ProcessDefinition definition = this.getDecisionProcess("process01");
		deployer.deployProcess(definition);
		
		//save payload
		IMap<String, String> payloadCache = Hazelcast.getMap(CacheConstants.PAYLOAD_CACHE);
		payloadCache.put("12345", this.getPayload(20));
		
		//set message headers for process start
		Map<String, Object> headers = this.getHeaders(definition, "12345");
		
		//set instance id
		headers.put(MessageConstants.INSTANCE_ID, "12345");
		
		//send message
		template.sendBodyAndHeaders("activemq:queue:in_e8c2eb9abd37d710f4447af1f4da99ef", "", headers);
		
		//get result
		Exchange ex = consumer.receive("activemq:queue:out_fb4b2d94895dc05e79d383c80b6af23a", 5000);
		
		assertNotNull(ex);
		assertEquals("fb4b2d94895dc05e79d383c80b6af23a", ex.getIn().getHeader(MessageConstants.TASK_ID));
	}
	
	public void testConcurrentModus() throws Exception{
		
		context.addRoutes(this.getMocks());
		
		MockEndpoint out10 = super.getMockEndpoint("mock:out10");
		MockEndpoint out20 = super.getMockEndpoint("mock:out20");
		MockEndpoint out30 = super.getMockEndpoint("mock:out30");
		
		//create process deployer
		ProcessDeployer deployer = new ProcessDeployer(context, this.createKnowledgeBase());
		assertNotNull(deployer);
		
		//deploy process
		ProcessDefinition definition = this.getDecisionProcess("process01");
		deployer.deployProcess(definition);
		
		
		//save payload
		IMap<String, String> payloadCache = Hazelcast.getMap(CacheConstants.PAYLOAD_CACHE);
		
		int[] values = {  10, 20, 30, 30, 20, 20, 30, 10, 30, 10, 10, 20, 20, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 10, 10, 20, 10, 10, 10, 20, 20, 10
						, 10, 20, 30, 30, 20, 30, 30, 10, 30, 10, 10, 20, 20, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 10, 20, 20, 20, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 10, 10, 20, 10, 10, 10, 20, 20, 10
						, 10, 20, 30, 30, 20, 10, 30, 10, 30, 10, 10, 20, 20, 10, 30, 20, 20, 10
						, 10, 30, 30, 30, 20, 20, 20, 10, 30, 10, 20, 20, 20, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 10, 10, 20, 10, 30, 10, 20, 20, 10
						, 10, 10, 30, 30, 20, 30, 10, 10, 30, 30, 10, 20, 20, 10, 30, 20, 20, 10
						, 10, 10, 10, 30, 20, 20, 30, 10, 30, 10, 10, 20, 10, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 10, 20, 20, 10, 30, 10, 30, 20, 10, 10, 10, 20, 20, 10
						, 10, 20, 30, 20, 20, 30, 30, 10, 30, 30, 10, 20, 30, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 10, 20, 20, 20, 20, 20, 20, 10, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 10, 10, 20, 10, 10, 10, 20, 20, 10
						, 10, 20, 30, 30, 20, 30, 30, 10, 30, 10, 10, 20, 20, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 10, 20, 20, 20, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 30, 10, 20, 10, 10, 10, 20, 20, 10
						, 10, 20, 30, 30, 20, 10, 30, 10, 30, 10, 10, 20, 20, 30, 30, 20, 20, 10
						, 10, 30, 30, 30, 20, 20, 20, 10, 30, 10, 20, 20, 20, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 10, 10, 20, 10, 30, 10, 20, 20, 10
						, 10, 10, 30, 30, 20, 30, 10, 10, 30, 30, 10, 20, 20, 10, 30, 20, 20, 20
						, 10, 10, 20, 30, 20, 20, 30, 10, 30, 10, 10, 20, 10, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 10, 20, 30, 10, 30, 20, 30, 20, 10, 10, 10, 20, 20, 10
						, 10, 20, 30, 20, 20, 30, 30, 10, 30, 30, 10, 20, 30, 10, 30, 20, 20, 10
						, 10, 20, 20, 30, 20, 20, 30, 10, 30, 10, 20, 20, 20, 20, 20, 20, 10, 10
						, 10, 20, 30, 30, 10, 20, 20, 10, 30, 10, 30, 20, 10, 10, 10, 20, 20, 10
						, 10, 10, 10, 20, 20, 30, 30, 10, 30, 30, 10, 20, 30, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 20, 20, 20, 20, 20, 20, 20, 10, 10
						, 10, 20, 10, 30, 20, 20, 10, 10, 30, 10, 10, 20, 10, 10, 10, 20, 20, 10
						, 10, 20, 30, 30, 10, 30, 30, 10, 30, 10, 10, 20, 20, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 10, 10, 30, 20, 20, 20, 20, 20, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 30, 10, 20, 10, 10, 10, 20, 20, 10
						, 10, 20, 30, 30, 10, 10, 30, 10, 30, 10, 10, 20, 20, 30, 30, 20, 20, 10
						, 10, 30, 30, 30, 20, 20, 20, 10, 30, 10, 20, 20, 20, 10, 30, 20, 20, 10
						, 10, 20, 30, 30, 20, 20, 30, 10, 30, 10, 20, 20, 10, 30, 10, 20, 20, 10
						};
		
		int x10 = 0;
		int x20 = 0;
		int x30 = 0;
		
		for (int i = 0; i < values.length; i++) {
			if(values[i] == 10)
				x10++;
			
			if(values[i] == 20)
				x20++;
			
			if(values[i] == 30)
				x30++;
		}
		
		out10.expectedMessageCount(x10);
		out20.expectedMessageCount(x20);
		out30.expectedMessageCount(x30);
		
		
		for (int i = 0; i < values.length; i++) {
			payloadCache.put(Integer.toString(i), this.getPayload(values[i]));
			
//			System.out.println(String.format("%s --> %s ", i, values[i]));
			
			//send message
			template.sendBodyAndHeaders("activemq:queue:in_e8c2eb9abd37d710f4447af1f4da99ef", "", this.getHeaders(definition, String.valueOf(i)));
			
		}
		
		
		super.assertMockEndpointsSatisfied(5000, TimeUnit.MILLISECONDS);
		
	}
	
	private RouteBuilder getMocks(){
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("activemq:queue:out_d1abcf163ee7f78e91f851e7f48e984d")
				.to("mock:out10");
				
				from("activemq:queue:out_fb4b2d94895dc05e79d383c80b6af23a")
				.to("mock:out20");
				
				from("activemq:queue:out_48c8b90e7a89129195c0492f269fa1dc")
				.to("mock:out30");
				
			}
		};
	}
	
	private ProcessDefinition getDecisionProcess(String name){
		//create the process
		CatifyProcessBuilder process = new CatifyProcessBuilder();
		
		process.start("tester", name, "1.0", "start")
		.decision("d-01")
			.line("sl-01").request("rq-01", "1").endline() //10
			.line("sl-02").request("rq-02", "1").endline() //20
			.line("sl-03").request("rq-03", "1").endline() //30
		.merge("m-01")
		.end("e-01");
		
		ProcessDefinition definition = process.getProcessDefinition();
		
		assertNotNull(definition);
		
		return definition;
	}
	
	private Map<String,Object> getHeaders(ProcessDefinition definition, String x){
		
		Map<String,Object> headers = new HashMap<String, Object>();
		headers.put(MessageConstants.ACCOUNT_NAME, "tester");
		headers.put(MessageConstants.PROCESS_NAME, definition.getProcessName());
		headers.put(MessageConstants.PROCESS_VERSION, "1.0");
		headers.put(MessageConstants.TASK_ID, "start");
		headers.put(MessageConstants.PROCESS_ID, definition.getProcessId());
		headers.put(MessageConstants.INSTANCE_ID, x);
		
		return headers;
	}
	
	
	private String getPayload(int value){
		return String.format("<payload><value>%s</value></payload>", value);
	}

}
