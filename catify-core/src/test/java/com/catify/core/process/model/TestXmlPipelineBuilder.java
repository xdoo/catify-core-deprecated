package com.catify.core.process.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.core.constants.CacheConstants;
import com.catify.core.process.ProcessDeployer;
import com.catify.core.process.xml.XmlProcessBuilder;
import com.catify.core.process.xml.model.Process;
import com.hazelcast.core.Hazelcast;

public class TestXmlPipelineBuilder extends CamelSpringTestSupport {

	@Override
	protected AbstractXmlApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	@Test
	public void testBuildStartPipeline() throws Exception {
		Process process = template.requestBody("direct:xml", this.xmlStart(), com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(process);
		
//		System.out.println(definition.getStartPipeline());
		
//		System.out.println("-------------------------------------------------->");
		
		//insert xslt to caches
		this.insertXslts();
		
		InputStream is = new ByteArrayInputStream(definition.getStartPipeline().getBytes());
		RoutesDefinition routes = context.loadRoutesDefinition(is);
		context.addRouteDefinitions(routes.getRoutes());
		
//		Thread.sleep(2500);
		
		assertNotNull(context.getRoute("in-file-e8c2eb9abd37d710f4447af1f4da99ef"));
		assertNotNull(context.getRoute("in-rest-e8c2eb9abd37d710f4447af1f4da99ef"));
		assertNotNull(context.getRoute("save-payload-e8c2eb9abd37d710f4447af1f4da99ef"));
		assertNotNull(context.getRoute("send-to-queue-e8c2eb9abd37d710f4447af1f4da99ef"));
		assertNotNull(context.getRoute("create-correlation-e8c2eb9abd37d710f4447af1f4da99ef"));
		
		//send message
		System.out.println("--------------------------------------------------> sending message");
		template.sendBody(context.getRoute("in-rest-e8c2eb9abd37d710f4447af1f4da99ef").getEndpoint(), this.getXml1());
	}

	@Test
	public void testBuildInPipeline() throws Exception {
		Process process = template.requestBody("direct:xml", this.xmlReceive(), com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(process);
		
//		System.out.println(definition.getInPipelines().get(0));
		
//		System.out.println("-------------------------------------------------->");
		
		//insert xslt to caches
		this.insertXslts();
		
		InputStream is = new ByteArrayInputStream(definition.getInPipelines().get(0).getBytes());
		RoutesDefinition routes = context.loadRoutesDefinition(is);
		context.addRouteDefinitions(routes.getRoutes());
		
		assertNotNull(context.getRoute("in-file-3e68dd4a3a52369301021ceb61158950"));
		assertNotNull(context.getRoute("in-rest-3e68dd4a3a52369301021ceb61158950"));
		assertNotNull(context.getRoute("save-payload-3e68dd4a3a52369301021ceb61158950"));
		assertNotNull(context.getRoute("send-to-queue-3e68dd4a3a52369301021ceb61158950"));
		assertNotNull(context.getRoute("get-correlation-3e68dd4a3a52369301021ceb61158950"));
	}

	@Test
	public void testBuildOutPipeline() throws Exception {
		Process process = template.requestBody("direct:xml", this.xmlRequest(), com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(process);
		
//		System.out.println(definition.getOutPipelines().get(0));
		
//		System.out.println("-------------------------------------------------->");
		
		//insert xslt to caches
		this.insertXslts();
		
		InputStream is = new ByteArrayInputStream(definition.getOutPipelines().get(0).getBytes());
		RoutesDefinition routes = context.loadRoutesDefinition(is);
		context.addRouteDefinitions(routes.getRoutes());
		
		assertNotNull(context.getRoute("out-pipeline-a600aa727f9df80c45b0674eda578fea"));
		assertNotNull(context.getRoute("load-payload-a600aa727f9df80c45b0674eda578fea"));
	}
	
	@Test
	public void testDeployProcessWithPipelines() throws InterruptedException{
		Process process = template.requestBody("direct:xml", this.xmlProcess(), com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(process);
		
		this.insertXslts();
		
		ProcessDeployer deployer = new ProcessDeployer(context);
		
		deployer.deployProcess(definition);
		
		assertNotNull(context.getRoute("in-rest-e8c2eb9abd37d710f4447af1f4da99ef"));
		Endpoint rest1 = context.getRoute("in-rest-e8c2eb9abd37d710f4447af1f4da99ef").getEndpoint();
		Endpoint rest2 = context.getRoute("in-rest-3e68dd4a3a52369301021ceb61158950").getEndpoint();
		
		MockEndpoint end = getMockEndpoint("mock:end");
		end.expectedMessageCount(1);
		
		//send message to process
		System.out.println("--------------------------> sending message to init process");
		template.sendBody(rest1 , this.getXml1());
		
		//send message to receive node
		Thread.sleep(5000);
		System.out.println("--------------------------> sending message to 'wait_for_payload'");
		template.sendBody(rest2, this.getXml2());
		
		assertMockEndpointsSatisfied(10000, TimeUnit.MILLISECONDS);
	}
	
	@Test
	public void testCallProcessTwice() throws InterruptedException{
		Process process = template.requestBody("direct:xml", this.xmlProcess2(), com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(process);
		
		this.insertXslts();
		
		ProcessDeployer deployer = new ProcessDeployer(context);
		
		deployer.deployProcess(definition);
		
		assertNotNull(context.getRoute("in-rest-e8c2eb9abd37d710f4447af1f4da99ef"));
		Endpoint rest1 = context.getRoute("in-rest-e8c2eb9abd37d710f4447af1f4da99ef").getEndpoint();
		Endpoint rest2 = context.getRoute("in-rest-3e68dd4a3a52369301021ceb61158950").getEndpoint();
		
		MockEndpoint end = getMockEndpoint("mock:end");
		end.expectedMessageCount(2);
		
		//send message to process
		System.out.println("--------------------------> sending message to init process");
		template.sendBody(rest1 , this.getXml1());
		
		//send message to receive node
		Thread.sleep(5000);
		System.out.println("--------------------------> sending 1. message to 'wait_for_payload'");
		template.sendBody(rest2, this.getXml2());
		System.out.println("--------------------------> sending 2. message to 'wait_for_payload'");
		template.sendBody(rest2, this.getXml2());
		
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
				
				from("restlet:http://localhost:9080/answer?restletMethod=post")
				.log("${body}")
				.to("mock:end");
				
			}
		};
	}
	
	private String xmlStart(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\">\n" +
				"		<inPipeline>\n" +
				"			<fromEndpoint>\n" +
				"				<file ns:uri=\"file:../files?delete=false\"/>\n" +
//				"				<rest ns:uri=\"restlet:http://localhost:9080/myprocess?restletMethod=post\"/>\n" +
				"				<rest />\n" +
				"			</fromEndpoint>\n" +
				"			<marshaller ns:type=\"csvMarshallProcessor\"/>\n" +
				"			<split ns:xpath=\"/foo\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/x</xpath>\n" +
				"				<xpath>/foo/bar/z</xpath>\n" +
				"			</correlation>\n" +
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
				"			<fromEndpoint>\n" +
				"				<file ns:uri=\"file:../files?delete=false\"/>\n" +
				"				<rest/>\n" +
				"			</fromEndpoint>\n" +
				"			<marshaller ns:type=\"csvMarshallProcessor\"/>\n" +
				"			<split ns:xpath=\"/foo\"/>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/x</xpath>\n" +
				"				<xpath>/foo/bar/z</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</receive>" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
		
	}
	
	private String xmlRequest(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\"/>\n" +
				"	<request ns:name=\"send_to_hazelcast\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint>\n" +
				"				<hazelcast ns:uri=\"hazelcast:map:foo\" ns:operation=\"put\"/>\n" +
				"			</toEndpoint>\n" +
				"			<marshaller ns:type=\"csvMarshallProcessor\"/>\n" +
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
				"			<fromEndpoint>\n" +
				"				<rest />\n" +
				"			</fromEndpoint>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"				<xpath>/foo/c/y</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"send_to_hazelcast\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint>\n" +
				"				<hazelcast ns:uri=\"hazelcast:map:foo\" ns:operation=\"put\"/>\n" +
				"			</toEndpoint>\n" +
				"		</outPipeline>\n" +
				"	</request>" +
				"	<receive ns:name=\"wait_for_payload\">\n" +
				"		<timeEvent ns:time=\"60000\">\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"		<inPipeline>\n" +
				"			<fromEndpoint>\n" +
				"				<rest/>\n" +
				"			</fromEndpoint>\n" +
				"			<correlation>\n" +
				"				<xpath>/bar/a</xpath>\n" +
				"				<xpath>/bar/b</xpath>\n" +
				"				<xpath>/bar/c/y</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</receive>" +
				"	<request ns:name=\"send_to_rest\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint>\n" +
				"				<rest ns:uri=\"restlet:http://localhost:9080/answer?restletMethod=post\"/>\n" +
				"			</toEndpoint>\n" +
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
				"			<fromEndpoint>\n" +
				"				<rest />\n" +
				"			</fromEndpoint>\n" +
				"			<correlation>\n" +
				"				<xpath>/foo/a</xpath>\n" +
				"				<xpath>/foo/b</xpath>\n" +
				"				<xpath>/foo/c/y</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</start>\n" +
				"	<request ns:name=\"send_to_hazelcast\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint>\n" +
				"				<hazelcast ns:uri=\"hazelcast:map:foo\" ns:operation=\"put\"/>\n" +
				"			</toEndpoint>\n" +
				"		</outPipeline>\n" +
				"	</request>" +
				"	<receive ns:name=\"wait_for_payload\">\n" +
				"		<timeEvent ns:time=\"60000\">\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"		<inPipeline>\n" +
				"			<fromEndpoint>\n" +
				"				<rest/>\n" +
				"			</fromEndpoint>\n" +
				"			<correlation>\n" +
				"				<xpath>/bar/a</xpath>\n" +
				"				<xpath>/bar/b</xpath>\n" +
				"				<xpath>/bar/c/y</xpath>\n" +
				"			</correlation>\n" +
				"		</inPipeline>\n" +
				"	</receive>" +
				"	<request ns:name=\"send_to_rest\">\n" +
				"		<outPipeline>\n" +
				"			<toEndpoint>\n" +
				"				<rest ns:uri=\"restlet:http://localhost:9080/answer?restletMethod=post\"/>\n" +
				"			</toEndpoint>\n" +
				"		</outPipeline>\n" +
				"	</request>\n" +
				"	<sleep>\n" +
				"		<timeEvent time=\"20000\"/>\n" +
				"	</sleep>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private String getTransformation(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n" +
				"	<xsl:template match=\"*\">" +
				"		<xsl:copy>" +
				"			<xsl:apply-templates/>" +
				"		</xsl:copy>" +
				"	</xsl:template>"+
				"</xsl:stylesheet>";
	}
	
	private String getCorrelation(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" exclude-result-prefixes=\"xs\">" +
				"	<xsl:output method=\"xml\" encoding=\"UTF-8\" indent=\"yes\"/>" +
				"	<xsl:template match=\"/\">" +
				"		<xsl:variable name=\"var1_instance\" select=\".\"/>" +
				"		<correlation>" +
				"			<xsl:value-of select=\"concat(concat(string($var1_instance/foo/a), string($var1_instance/foo/b)), string($var1_instance/foo/c/y))\"/>" +
				"		</correlation>" +
				"	</xsl:template>" +
				"</xsl:stylesheet>";
		
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
	
	private void insertXslts(){
		//insert xslt to caches
		Hazelcast.getMap(CacheConstants.CORRELATION_RULE_CACHE).put("3e68dd4a3a52369301021ceb61158950", this.getCorrelation());
		Hazelcast.getMap(CacheConstants.TRANSFORMATION_CACHE).put("3e68dd4a3a52369301021ceb61158950", this.getTransformation());
		
		Hazelcast.getMap(CacheConstants.CORRELATION_RULE_CACHE).put("e8c2eb9abd37d710f4447af1f4da99ef", this.getCorrelation());
		Hazelcast.getMap(CacheConstants.TRANSFORMATION_CACHE).put("e8c2eb9abd37d710f4447af1f4da99ef", this.getTransformation());
		
		Hazelcast.getMap(CacheConstants.TRANSFORMATION_CACHE).put("a600aa727f9df80c45b0674eda578fea", this.getTransformation());
		
		Hazelcast.getMap(CacheConstants.TRANSFORMATION_CACHE).put("4a9cd68d09e14452f8e9d95b8b43aac8", this.getTransformation());
		
	}

}
