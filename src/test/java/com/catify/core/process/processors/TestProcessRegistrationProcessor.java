package com.catify.core.process.processors;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.CamelSpringTestSupport;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.core.constants.CacheConstants;
import com.catify.core.process.model.ProcessDefinition;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class TestProcessRegistrationProcessor extends CamelSpringTestSupport {

	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	public void testProcessRegistration() throws InterruptedException{
		template.sendBody("direct:xml", this.xml());
		
		Thread.sleep(500);
		
		IMap<String, Object> map = Hazelcast.getMap(CacheConstants.PROCESS_CACHE);
		assertTrue(map.containsKey("e8c2eb9abd37d710f4447af1f4da99ef"));
		assertNotNull(map.get("e8c2eb9abd37d710f4447af1f4da99ef"));
		map.clear();
	}
	
	public void testProcessRegistrationRoute() throws InterruptedException{
		String body = template.requestBody("restlet:http://localhost:9080/catify/deploy_process?restletMethod=post", this.xml(), String.class);
		
		assertNotNull(body);
		
		IMap<String, Object> map = Hazelcast.getMap(CacheConstants.PROCESS_CACHE);
		assertTrue(map.containsKey("e8c2eb9abd37d710f4447af1f4da99ef"));
		assertNotNull(map.get("e8c2eb9abd37d710f4447af1f4da99ef"));
		map.clear();
	}
	
	@Override
	protected RouteBuilder createRouteBuilder(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				DataFormat jaxb = new JaxbDataFormat("com.catify.core.process.xml.model");
				
				from("direct:xml")
				.unmarshal(jaxb)
				.processRef("processRegistrationProcessor");
				
				from("direct:xml2")
				.unmarshal(jaxb)
				.processRef("processRegistrationProcessor")
				.marshal(jaxb);
				
				
			}
		};
	}
	
	private String xml(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process01\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
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

}
