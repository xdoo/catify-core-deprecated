package com.catify.core.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelSpringTestSupport;
import org.drools.KnowledgeBase;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.QueueConstants;
import com.catify.core.process.ProcessDeployer;
import com.catify.core.process.builders.CatifyProcessBuilder;
import com.catify.core.process.model.ProcessDefinition;

public class TestProcessDeployer extends CamelSpringTestSupport {

	@Override
	protected AbstractXmlApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	public void testSimpleProcessDeployment(){
		this.deployProcess("process");
	}
	
	public void testConcurrentProcessRun(){
		ProcessDefinition d1 = this.deployProcess("process01");
		ProcessDefinition d2 = this.deployProcess("process02");
		
		//start process
		template.sendBodyAndHeaders("activemq:queue:in_queue", d1.getProcessId(), this.setHeaders(d1.getProcessId(), "process01"));
		template.sendBodyAndHeaders("activemq:queue:in_queue", d2.getProcessId(), this.setHeaders(d2.getProcessId(), "process02"));
		
		//get message
		Exchange ex1 = consumer.receive("activemq:queue:"+QueueConstants.OUT_QUEUE+"?selector="+MessageConstants.PROCESS_ID+"%3D%27"+d1.getProcessId()+"%27", 5000);
		Exchange ex2 = consumer.receive("activemq:queue:"+QueueConstants.OUT_QUEUE+"?selector="+MessageConstants.PROCESS_ID+"%3D%27"+d2.getProcessId()+"%27", 5000);
		
		assertNotNull(ex1);
		assertEquals(d1.getProcessId(), ex1.getIn().getBody());
		
		assertNotNull(ex2);
		assertEquals(d2.getProcessId(), ex2.getIn().getBody());
		
	}
	
	public void testFork() throws Exception{
		context.addRoutes(this.createMockConsumer());
		MockEndpoint out = getMockEndpoint("mock:out");
		
		out.setExpectedMessageCount(4);
		
		ProcessDeployer deployer = new ProcessDeployer(context, super.applicationContext.getBean("kbase", KnowledgeBase.class));
		
		ProcessDefinition definition = this.getForkProcess("process01");
		
		deployer.deployProcess(definition);
		
		//check if routes are registered
		this.checkRoutes(definition);
		
		//send message 
		Map<String, Object> headers = this.setHeaders(definition.getProcessId(), definition.getProcessName());
		template.sendBodyAndHeaders("activemq:queue:in_queue", "foo", headers);
		
		Thread.sleep(150);
		
		out.assertIsSatisfied();
	}

	private ProcessDefinition getSimpleProcess(String name){
		//create the process
		CatifyProcessBuilder process = new CatifyProcessBuilder();
		
		process.start("tester", name, "1.0", "start")
		.request("rq-01", "1")
		.end("end");
		
		ProcessDefinition definition = process.getProcessDefinition();
		
		assertNotNull(definition);
		
		return definition;
	}
	
	private ProcessDefinition getForkProcess(String name){
		//create the process
		CatifyProcessBuilder process = new CatifyProcessBuilder();
		
		process.start("tester", name, "1.0", "start")
		.fork("f-01")
			.line("sl-01").request("rq-01", "1").endline()
			.line("sl-02").request("rq-02", "1").endline()
			.line("sl-03").request("rq-03", "1").endline()
		.merge("m-01")
		.request("rq-04", "1")
		.end("e-01");
		
		ProcessDefinition definition = process.getProcessDefinition();
		
		assertNotNull(definition);
		
		return definition;
	}
	

	
	private ProcessDefinition deployProcess(String name){
		ProcessDeployer deployer = new ProcessDeployer(context, super.applicationContext.getBean("kbase", KnowledgeBase.class));
		
		ProcessDefinition definition = this.getSimpleProcess(name);
		
		//deploy it
		deployer.deployProcess(definition);
		
		//check if routes are registered
		this.checkRoutes(definition);
		
		return definition;
	}
	
	private Map<String,Object> setHeaders(String processId, String processName){
		
		Map<String,Object> headers = new HashMap<String, Object>();
		headers.put(MessageConstants.ACCOUNT_NAME, "tester");
		headers.put(MessageConstants.PROCESS_NAME, processName);
		headers.put(MessageConstants.PROCESS_VERSION, "1.0");
		headers.put(MessageConstants.TASK_ID, "start");
		headers.put(MessageConstants.PROCESS_ID, processId);
		
		return headers;
	}
	
	private void checkRoutes(ProcessDefinition definition){
		
		//process route
		assertNotNull(context.getRoute(String.format("process-%s", definition.getProcessId())));
		
		Iterator<String> it = definition.getNodes().keySet().iterator();
		while (it.hasNext()) {
			assertNotNull(context.getRoute(String.format("node-%s", it.next())));
		}
	}
	
	private RouteBuilder createMockConsumer(){
		return new RouteBuilder(){

			@Override
			public void configure() throws Exception {
				
				from("seda:load")
				.multicast()
				.to("activemq:queue:in_queue", "activemq:queue:in_queue", "activemq:queue:in_queue", "activemq:queue:in_queue", "activemq:queue:in_queue");
				
				from("activemq:queue:"+QueueConstants.OUT_QUEUE)
				.log("${body}")
				.log(String.format("--------------------------> ${header.%s}", MessageConstants.TASK_ID))
				.to("mock:out");				
			}			
		};
	}

}
