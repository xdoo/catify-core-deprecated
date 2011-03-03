package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.CamelSpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.core.constants.MessageConstants;

public class TestProcessDeploymentProcessor extends CamelSpringTestSupport {
	
	@Override
	protected AbstractXmlApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testSimpleMarshalling() throws Exception{		
		String body = template.requestBody("restlet:http://localhost:9080/catify/deploy_process?restletMethod=post", this.xml2(), String.class);
		
		assertNotNull(body);
		assertTrue(body.contains("<ns1:process xmlns:ns1=\"http://www.catify.com/api/1.0\" accountName=\"tester\" processName=\"process01\" processVersion=\"1.0\" processId=\"e8c2eb9abd37d710f4447af1f4da99ef\">"));
		assertTrue(body.contains("<ns1:start ns1:name=\"start\" ns1:id=\"05cc5b277e32985b01d5a44b20a491d7\"/>"));
		assertTrue(body.contains("<ns1:end ns1:name=\"end\" ns1:id=\"59ab3cb6320ef9940aa3cb5ea64b9600\"/>"));
		
		Thread.sleep(1500);
		
		assertNotNull(context.getRoute("process-e8c2eb9abd37d710f4447af1f4da99ef"));
		assertNotNull(context.getRoute("node-59ab3cb6320ef9940aa3cb5ea64b9600"));
		assertNotNull(context.getRoute("node-05cc5b277e32985b01d5a44b20a491d7"));
	}
	
	public void testComplexMarshalling() throws Exception{		
		String body = template.requestBody("restlet:http://localhost:9080/catify/deploy_process?restletMethod=post", this.xml1(), String.class);
		
		assertNotNull(body);
		
		Thread.sleep(3000);
		
		assertNotNull(context.getRoute("process-aa5c541a8c54340b4ba7ba7559d88390"));
		assertNotNull(context.getRoute("node-afaaf803b55a3aa3db5e2b14ce2d1c96"));
		assertNotNull(context.getRoute("node-939d5924eede2bdd984775dd04b7048f"));
		assertNotNull(context.getRoute("node-0f80e1909847377cdb7ab221badf8334"));
		assertNotNull(context.getRoute("aqnode-0f80e1909847377cdb7ab221badf8334"));
		assertNotNull(context.getRoute("go-0f80e1909847377cdb7ab221badf8334"));
		assertNotNull(context.getRoute("wait-0f80e1909847377cdb7ab221badf8334"));
		assertNotNull(context.getRoute("node-0a703f6f7442e1156c6eb7a81de3148b"));
		assertNotNull(context.getRoute("node-bb95ca986f5fde0e8f4f5b9392a5e786"));
		assertNotNull(context.getRoute("node-7c2a61ecf060974bddfa7326f5e8ed13"));
		assertNotNull(context.getRoute("node-480466aeb13c65ebd1ab1eae90e80ad5"));
		
		
	}
	
	public void testTimeOut() throws InterruptedException{
		
		//deploy process
		String body = template.requestBody("restlet:http://localhost:9080/catify/deploy_process?restletMethod=post", this.xml3(), String.class);
		assertNotNull(body);
		Thread.sleep(3000);
		
		//send message
		template.sendBody("direct:send", "foo");
		
		Exchange ex = consumer.receive("activemq:queue:out_316e7035b7b2a5e5a3d0095cd4136902", 10000);
		
		assertNotNull(ex);
		
	}
	
	
	@Override
	protected RouteBuilder createRouteBuilder(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				DataFormat jaxb = new JaxbDataFormat("com.catify.core.process.xml.model");
				
				from("direct:xml")
				.unmarshal(jaxb)
				.process(new ProcessDeploymentProcessor())
				.marshal(jaxb);
				
				from("direct:send")
				.setHeader(MessageConstants.ACCOUNT_NAME, constant("tester"))
				.setHeader(MessageConstants.PROCESS_NAME, constant("process03"))
				.setHeader(MessageConstants.PROCESS_VERSION, constant("1.0"))
				.process(new ProcessIdProcessor())
				.log(LoggingLevel.INFO, "sending message to 'activemq:queue:in_260f210446b2d99f24c2e748ebbead61'")
				.to("activemq:queue:in_260f210446b2d99f24c2e748ebbead61");
				
				
			}
		};
	}
	
	private String xml1(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process02\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\"/>\n" +
				"	<request ns:name=\"bam_step_01\"/>\n" +
				"	<receive ns:name=\"wait_for_payload\">\n" +
				"		<timeEvent ns:time=\"60000\">\n" +
				"			<request ns:name=\"throw_time_out_exception\"/>\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"	</receive>\n" +
				"	<request ns:name=\"bam_step_02\"/>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private String xml2(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\"/>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}
	
	private String xml3(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process03\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\"/>\n" +
				"	<request ns:name=\"bam_step_01\"/>\n" +
				"	<receive ns:name=\"wait_for_payload\">\n" +
				"		<timeEvent ns:time=\"1000\">\n" +
				"			<request ns:name=\"throw_time_out_exception\"/>\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"	</receive>\n" +
				"	<request ns:name=\"bam_step_02\"/>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}

}
