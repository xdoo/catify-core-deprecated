package com.catify.core.process;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.commons.codec.digest.DigestUtils;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;
import com.catify.core.constants.QueueConstants;
import com.catify.core.process.builders.CatifyProcessBuilder;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.nodes.ReceiveNode;
import com.catify.core.process.processors.TaskInstanceIdProcessor;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestReceiveNode extends BaseNode {
	
	/**
	 * the process waits for an incoming message
	 * 
	 * @throws Exception
	 */
	public void testReceiveWithWait() throws Exception{		
		ProcessDeployer deployer = new ProcessDeployer(context, this.createKnowledgeBase());
		ProcessDefinition definition = this.getReceiveProcess("process01");		
		
		context.addRoutes(this.createMessageCopyRoute());
		context.addRoutes(this.createMessageSelector());
		MockEndpoint out = getMockEndpoint("mock:out");
		
		out.setExpectedMessageCount(1);
		
		//check if routes are registered
		deployer.deployProcess(definition);
		this.checkRoutes(definition);
		
		//send message 
		Map<String, Object> headers = this.setHeaders(definition);
		template.sendBodyAndHeaders("activemq:queue:in_e8c2eb9abd37d710f4447af1f4da99ef", "foo", headers);
		
		Thread.sleep(1 * 1000);
		
		out.assertIsSatisfied();
	}
	
	public void testReceiveWithoutWait() throws Exception{		
		ProcessDeployer deployer = new ProcessDeployer(context, this.createKnowledgeBase());
		ProcessDefinition definition = this.getReceiveProcess("process01");
		
		context.addRoutes(this.createMessageSelector());
		MockEndpoint out = getMockEndpoint("mock:out");
		
		out.setExpectedMessageCount(1);
		deployer.deployProcess(definition);
		
		String nodeId = new ReceiveNode(definition.getProcessId(), "rc-01", 10000).getNodeId();
		
		//check if routes are registered
		this.checkRoutes(definition);
		
		//put message into cache (which simulates an early answer)
		IMap<String, Integer> map = Hazelcast.getMap(CacheConstants.NODE_CACHE);
		map.put(DigestUtils.md5Hex(String.format("%s%s", "123", nodeId)), ProcessConstants.STATE_WAITING);
		
		//send message to start process
		Map<String, Object> headers = this.setHeaders(definition);
		headers.put(MessageConstants.INSTANCE_ID, "123");
		template.sendBodyAndHeaders("activemq:queue:in_e8c2eb9abd37d710f4447af1f4da99ef", "foo", headers);
		
		Thread.sleep(1 * 1000);
		
		out.assertIsSatisfied();
		
	}
	
	public void testReceiveTimeOut() throws Exception{		
		ProcessDeployer deployer = new ProcessDeployer(context, this.createKnowledgeBase());
		ProcessDefinition definition = this.getReceiveProcess("process01");
		deployer.deployProcess(definition);
		
		//check if routes are registered
		this.checkRoutes(definition);
		
		//send message to start process
		Map<String, Object> headers = this.setHeaders(definition);
		headers.put(MessageConstants.INSTANCE_ID, "123");
		template.sendBodyAndHeaders("activemq:queue:in_e8c2eb9abd37d710f4447af1f4da99ef", "foo", headers);
		
		String nodeId = new ReceiveNode(definition.getProcessId(), "rc-01", 10000).getNodeId();
		Exchange exchange = consumer.receive("activemq:queue:event_"+nodeId, 5000);
		
		assertNotNull(exchange);
		
	}
	
	private RouteBuilder createMessageCopyRoute(){
		
		
		
		return new RouteBuilder(){

			@Override
			public void configure() throws Exception {
				// task id --> fb4b2d94895dc05e79d383c80b6af23a
				from("activemq:queue:out_fb4b2d94895dc05e79d383c80b6af23a")
//				.log("-----------------------------------> 1: ${body}")
				.setBody(simple("received-${body} --> ${header.catify_taskInstanceId}"))
				.setHeader(MessageConstants.TASK_ID, constant("fef8abc28980ebf608cbe4519bdca20d"))
				.process(new TaskInstanceIdProcessor())
				.delay(10)
				.to("activemq:queue:in_fef8abc28980ebf608cbe4519bdca20d");	
				

			}			
		};
	}
	
	private RouteBuilder createMessageSelector(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				// task id --> d1abcf163ee7f78e91f851e7f48e984d
//				from("activemq:queue:"+QueueConstants.OUT_QUEUE+"?selector="+MessageConstants.TASK_ID+"%3D%27d1abcf163ee7f78e91f851e7f48e984d%27")
				from("activemq:queue:out_d1abcf163ee7f78e91f851e7f48e984d")
				.log("-----------------------------------> 2: ${body}")
				.to("mock:out");
				
			}
		};
	}
	
	private ProcessDefinition getReceiveProcess(String name){
		//create the process
		CatifyProcessBuilder process = new CatifyProcessBuilder();
		
		process.start("tester", name, "1.0", "start")
		.request("rq-01", "1")
		.receive("rc-01", 150)
		.request("rq-02", "1")
		.end("e-01");
		
		ProcessDefinition definition = process.getProcessDefinition();
		
		assertNotNull(definition);
		
		return definition;
	}

}
