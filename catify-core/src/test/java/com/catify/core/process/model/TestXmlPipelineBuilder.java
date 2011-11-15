package com.catify.core.process.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.core.process.ProcessDeployer;
import com.catify.core.process.xml.XmlProcessBuilder;
import com.catify.core.process.xml.model.Process;

public class TestXmlPipelineBuilder extends CamelSpringTestSupport {

	@EndpointInject(uri = "mock:out1")
	private MockEndpoint out1;
	
	@EndpointInject(uri = "mock:out2")
	private MockEndpoint out2;
	
	@EndpointInject(uri = "mock:error")
	private MockEndpoint error;	
	
	private static final int START = 0;
	private static final int IN = 1;
	private static final int OUT = 2;

	@Override
	protected AbstractXmlApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	/**
	 * tests if the start pipeline will be build and deployed properly 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBuildStartPipeline() throws Exception {
		
		this.deployRoutes(this.xmlStart(), TestXmlPipelineBuilder.START);
		
		assertNotNull(context.getRoute("start-e8c2eb9abd37d710f4447af1f4da99ef"));
		assertNotNull(context.getRoute("save-payload-e8c2eb9abd37d710f4447af1f4da99ef-foo"));
		assertNotNull(context.getRoute("send-to-queue-e8c2eb9abd37d710f4447af1f4da99ef"));
		assertNotNull(context.getRoute("create-correlation-e8c2eb9abd37d710f4447af1f4da99ef"));

	}
	
	/**
	 * tests if the in pipeline will be build and deployed properly 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBuildInPipeline() throws Exception {
		
		this.deployRoutes( this.xmlReceive(), TestXmlPipelineBuilder.IN);
		
		assertNotNull(context.getRoute("in-3e68dd4a3a52369301021ceb61158950"));
		assertNotNull(context.getRoute("save-payload-3e68dd4a3a52369301021ceb61158950-foo"));
		assertNotNull(context.getRoute("send-to-queue-3e68dd4a3a52369301021ceb61158950"));
		assertNotNull(context.getRoute("get-correlation-3e68dd4a3a52369301021ceb61158950"));

	}
	
	/**
	 * 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBuildInPipelineWithCorrelationException() throws Exception{
		
		this.deployRoutes(this.xmlReceiveCorrelationException(), TestXmlPipelineBuilder.IN);
		
		assertNotNull(context.getRoute("in-3e68dd4a3a52369301021ceb61158950"));
		assertNotNull(context.getRoute("save-payload-3e68dd4a3a52369301021ceb61158950-foo"));
		assertNotNull(context.getRoute("send-to-queue-3e68dd4a3a52369301021ceb61158950"));
		assertNotNull(context.getRoute("get-correlation-3e68dd4a3a52369301021ceb61158950"));
		
	}

	/**
	 * tests if the out pipeline will be build and deployed properly 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBuildOutPipeline() throws Exception {
		
		this.deployRoutes(this.xmlRequest(), TestXmlPipelineBuilder.OUT);
		
		assertNotNull(context.getRoute("out-pipeline-a600aa727f9df80c45b0674eda578fea"));
		assertNotNull(context.getRoute("load-payload-a600aa727f9df80c45b0674eda578fea-foo"));
		assertNotNull(context.getRoute("load-payload-a600aa727f9df80c45b0674eda578fea-bar"));

	}
	
	/**
	 * test if we can build an out pipeline without using variables (loading payload)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBuildOutPipelineWithoutVariables() throws Exception{
		
		this.deployRoutes(this.xmlRequest2(), TestXmlPipelineBuilder.OUT);
		
		assertNotNull(context.getRoute("out-pipeline-a600aa727f9df80c45b0674eda578fea"));
	}
	
	/**
	 * test if we can deploy a whole process with pipelines
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testDeployProcessWithPipelines() throws InterruptedException{
		Process process = template.requestBody("direct:xml", this.xmlProcess(), com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(process);
		
		ProcessDeployer deployer = new ProcessDeployer(context);
		
		deployer.deployProcess(definition);
		
		out1.expectedMessageCount(1);
		out2.expectedMessageCount(1);
		
		//send message to process
//		System.out.println("--------------------------> sending message to init process");
		template.sendBody("seda://start" , this.getXml1());
		
		//send message to receive node
		Thread.sleep(3000);
//		System.out.println("--------------------------> sending message to 'wait_for_payload'");
		template.sendBody("seda://receive", this.getXml2());
		
		assertMockEndpointsSatisfied(10000, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * test if the correlation exception part works.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testCorrelationException() throws InterruptedException{
		Process process = template.requestBody("direct:xml", this.xmlProcess(), com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(process);
		
		ProcessDeployer deployer = new ProcessDeployer(context);
		
		deployer.deployProcess(definition);
		
		out1.expectedMessageCount(1);
		error.expectedMessageCount(1);
		
		//send message to process
//		System.out.println("--------------------------> sending message to init process");
		template.sendBody("seda://start" , this.getXml1());
		
		//send message to receive node
		Thread.sleep(3000);
//		System.out.println("--------------------------> sending message to 'wait_for_payload'");
		template.sendBody("seda://receive", this.getXml3());
		
		assertMockEndpointsSatisfied(10000, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * test how an active instance acts, if it will be called twice.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testCallProcessTwice() throws InterruptedException{
		Process process = template.requestBody("direct:xml", this.xmlProcess2(), com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(process);
		
		ProcessDeployer deployer = new ProcessDeployer(context);
		
		deployer.deployProcess(definition);
		
		out1.expectedMessageCount(1);
		out2.expectedMessageCount(2);
		
		//send message to process
//		System.out.println("--------------------------> sending message to init process");
		template.sendBody("seda://start" , this.getXml1());
		
		//send message to receive node
		Thread.sleep(3000);
//		System.out.println("--------------------------> sending 1. message to 'wait_for_payload'");
		template.sendBody("seda://receive", this.getXml2());
//		System.out.println("--------------------------> sending 2. message to 'wait_for_payload'");
		template.sendBody("seda://receive", this.getXml2());
		
		assertMockEndpointsSatisfied(10000, TimeUnit.MILLISECONDS);
	}
	
	@Override
	protected RouteBuilder createRouteBuilder(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				DataFormat jaxb = new JaxbDataFormat("com.catify.core.process.xml.model");
				
				errorHandler(loggingErrorHandler());
				
				from("direct:xml")
				.routeId("marshal")
				.unmarshal(jaxb)
				.log("${body}");
				
			}
		};
	}
	
	/**
	 * helper to deploy xml routes from a string input.
	 * 
	 * @param processXml
	 * @param direction
	 * @throws Exception
	 */
	private void deployRoutes(String processXml, int direction) throws Exception{
		Process process = template.requestBody("direct:xml", processXml, com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(process);
		
		//deploy correlation rule
		ProcessDeployer.deployCorrelationRules(definition.getAllCorrelationRules());
		
		InputStream is = null;
		switch (direction) {
		case 0:
			//start
			is = new ByteArrayInputStream(definition.getStartPipeline().getBytes());
			break;

		case 1:
			//in
			is = new ByteArrayInputStream(definition.getInPipelines().get(0).getBytes());
			break;
			
		case 2:
			//out
			is = new ByteArrayInputStream(definition.getOutPipelines().get(0).getBytes());
			break;
		}
		
		RoutesDefinition routes = context.loadRoutesDefinition(is);
		context.addRouteDefinitions(routes.getRoutes());
	}
	
	private String xmlStart(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"direct://foo\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/x</xpath>\n" +
				"				<xpath>/foo/bar/z</xpath>\n" +
				"			</correlation>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" ns:xpath=\"/\"/>\n" +
				"			</variables>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private String xmlReceive(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\"/>\n" +
				"	<receive ns:name=\"wait_for_payload\">\n" +
				"		<timeEvent ns:time=\"60000\">\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"direct://foo\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/x</xpath>\n" +
				"				<xpath>/foo/bar/z</xpath>\n" +
				"			</correlation>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" ns:xpath=\"/\"/>\n" +
				"			</variables>\n" +
				"		</inPipeline>\n" +
				"	</receive>" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
		
	}
	
	private String xmlReceiveCorrelationException(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\"/>\n" +
				"	<receive ns:name=\"wait_for_payload\">\n" +
				"		<inPipeline>\n" +
				"			<pipelineExceptionEvent ns:pipelineExceptionType=\"CorrelationException\" ns:uri=\"mock://error\" ns:attachPayload=\"true\"/>\n" +
				"			<endpoint ns:uri=\"direct://bar\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/x</xpath>\n" +
				"				<xpath>/foo/bar/z</xpath>\n" +
				"			</correlation>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" ns:xpath=\"/\"/>\n" +
				"			</variables>\n" +
				"		</inPipeline>\n" +
				"	</receive>\n" +
				"</process>";
		
	}
	
	private String xmlRequest(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\"/>\n" +
				"	<request ns:name=\"send_to_hazelcast\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"hazelcast:map:foo\"/>\n" +
				"			<variables>\n" +
				"				<variable ns:name=\"foo\" />\n" +
				"				<variable ns:name=\"bar\" />\n" +
				"			</variables>\n" +
				"		</outPipeline>\n" +
				"	</request>" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
		
	}
	
	private String xmlRequest2(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\"/>\n" +
				"	<request ns:name=\"send_to_hazelcast\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"hazelcast:map:foo\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
		
	}
	
	private String xmlProcess(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda:start\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"				<xpath>/foo/c/y</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"send_to_out1\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock:out1\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>" +
				"	<receive ns:name=\"wait_for_payload\">\n" +
				"		<timeEvent ns:time=\"60000\">\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"		<inPipeline>\n" +
				"			<pipelineExceptionEvent ns:pipelineExceptionType=\"CorrelationException\" ns:uri=\"mock://error\" ns:attachPayload=\"true\"/>\n" +
				" 			<endpoint ns:uri=\"seda:receive\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/bar/a</xpath>\n" +
				"				<xpath>/bar/b</xpath>\n" +
				"				<xpath>/bar/c/y</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</receive>" +
				"	<request ns:name=\"send_to_out2\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock:out2\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private String xmlProcess2(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<endpoint ns:uri=\"seda:start\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"				<xpath>/foo/c/y</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"send_to_out1\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock:out1\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>" +
				"	<receive ns:name=\"wait_for_payload\">\n" +
				"		<timeEvent ns:time=\"60000\">\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"		<inPipeline>\n" +
				" 			<endpoint ns:uri=\"seda:receive\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/bar/a</xpath>\n" +
				"				<xpath>/bar/b</xpath>\n" +
				"				<xpath>/bar/c/y</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</receive>" +
				"	<request ns:name=\"send_to_out2\">\n" +
				"		<outPipeline>\n" +
				"			<endpoint ns:uri=\"mock:out2\"/>\n" +
				"		</outPipeline>\n" +
				"	</request>" +
				"	<sleep>\n" +
				"		<timeEvent time=\"20000\"/>\n" +
				"	</sleep>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private String getXml1(){
		return "<foo>" +
				"	<a>a</a>" +
				"	<b>b</b>" +
				"	<c>" +
				"		<x>x</x>" +
				"		<y>y</y>" +
				"	</c>" +
				"</foo>";
	}
	
	private String getXml2(){
		return "<bar>" +
				"	<a>a</a>" +
				"	<b>b</b>" +
				"	<c>" +
				"		<x>x</x>" +
				"		<y>y</y>" +
				"	</c>" +
				"</bar>";
	}
	
	private String getXml3(){
		return "<bar>" +
				"	<a>1</a>" +
				"	<b>2</b>" +
				"	<c>" +
				"		<x>3</x>" +
				"		<y>4</y>" +
				"	</c>" +
				"</bar>";
	}

}
