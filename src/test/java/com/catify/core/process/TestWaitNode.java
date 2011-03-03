package com.catify.core.process;

import java.util.Map;

import org.apache.camel.Exchange;

import com.catify.core.process.builders.CatifyProcessBuilder;
import com.catify.core.process.model.ProcessDefinition;

public class TestWaitNode extends BaseNode {

	public void testSleepNode(){
		//create process deployer
		ProcessDeployer deployer = new ProcessDeployer(context, this.createKnowledgeBase());
		assertNotNull(deployer);
		
		//deploy process
		ProcessDefinition definition = this.getDecisionProcess("process01");
		deployer.deployProcess(definition);
		this.checkRoutes(definition);
		
		//send message 
		Map<String, Object> headers = this.setHeaders(definition);
		template.sendBodyAndHeaders("activemq:queue:in_e8c2eb9abd37d710f4447af1f4da99ef", "foo", headers);
		
		//process sleeps 1 second...
		Exchange ex1 = consumer.receive("activemq:queue:out_fb4b2d94895dc05e79d383c80b6af23a", 900);
		assertNull(ex1);
		
		Exchange ex2 = consumer.receive("activemq:queue:out_fb4b2d94895dc05e79d383c80b6af23a", 5000);
		assertNotNull(ex2);
	}
	
	private ProcessDefinition getDecisionProcess(String name){
		//create the process
		CatifyProcessBuilder process = new CatifyProcessBuilder();
		
		process.start("tester", name, "1.0", "start")
		.sleep("sp-01", 1000)
		.request("rq-01", "1")
		.end("en-01");
		
		ProcessDefinition definition = process.getProcessDefinition();
		
		assertNotNull(definition);
		
		return definition;
	}
	
}
