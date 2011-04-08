package com.catify.core;

import java.net.InetAddress;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.Main;
import org.drools.KnowledgeBase;
import org.springframework.context.support.AbstractApplicationContext;

import com.catify.core.process.ProcessDeployer;
import com.catify.core.process.builders.CatifyProcessBuilder;
import com.catify.core.process.model.ProcessDefinition;

public class Catify {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		Catify catify = new Catify();
		catify.run();
	}
	
	private void run() throws Exception{
		
		//first create property file
		this.createPropertyFile();
		
		Thread.sleep(1000);
		
		//use the standard camel main class
		Main camel = new Main();
		camel.setApplicationContextUri("/META-INF/spring/camel-context.xml");
		camel.enableHangupSupport();
		camel.run();
		
		AbstractApplicationContext context = camel.getApplicationContext();
		
		//get camel context and knowledge base
		CamelContext camelContext = context.getBean("camelContext", CamelContext.class);
		KnowledgeBase kbase = context.getBean("kbase", KnowledgeBase.class);
		
		//deploy process
		ProcessDeployer deployer = new ProcessDeployer(camelContext, kbase);
		deployer.deployProcess(this.createProcess());

	}
	
	/**
	 * TODO --> delete this in final version
	 * 
	 * @return
	 */
	private ProcessDefinition createProcess(){
		//create the process
		CatifyProcessBuilder process = new CatifyProcessBuilder();
		
		process.start("sbk", "process-01", "1.0", "start")
		.request("rq-01", "1")
		.end("end");
		
		return process.getProcessDefinition();
	}
	
	/**
	 * creates a property file for the new node
	 */
	private void createPropertyFile(){
		
		System.out.println("creating property file...");
		CamelContext context = new DefaultCamelContext();
		
		try {
			context.start();
			
			//add config route
			context.addRoutes(this.createPropertyFileRoute());
			
			//send message to route
			ProducerTemplate template = context.createProducerTemplate();
			template.sendBody("direct:configuration", String.format("hostname=%s", InetAddress.getLocalHost().getHostName()));
			
			context.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * create a config file to identify this
	 * node from the network of brokers.
	 * 
	 * @return
	 */
	private RouteBuilder createPropertyFileRoute(){
		return new RouteBuilder(){

			@Override
			public void configure() throws Exception {
				
				from("direct:configuration")
				.routeId("configuration")
				.to("file:lib/?fileName=network.properties");
			}			
		};
	}

	// Git push-test
	
}
