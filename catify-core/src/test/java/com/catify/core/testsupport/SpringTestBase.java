package com.catify.core.testsupport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.process.ProcessDeployer;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.xml.XmlProcessBuilder;
import com.catify.core.process.xml.model.Process;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class SpringTestBase extends CamelSpringTestSupport {
	
	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return  new ClassPathXmlApplicationContext("/META-INF/spring/camel-context.xml");
	}
	
	protected Map<String,Object> setHeaders(ProcessDefinition def){
		
		Map<String,Object> headers = new HashMap<String, Object>();
		headers.put(MessageConstants.ACCOUNT_NAME, "tester");
		headers.put(MessageConstants.PROCESS_NAME, def.getProcessName());
		headers.put(MessageConstants.PROCESS_VERSION, "1.0");
		headers.put(MessageConstants.TASK_ID, "start");
		headers.put(MessageConstants.PROCESS_ID, def.getProcessId());
		headers.put(MessageConstants.TASK_NAME, "start");
		
		return headers;
	}
	
	protected void checkRoutes(ProcessDefinition definition){
		
		//process route
		assertNotNull(context.getRoute(String.format("process-%s", definition.getProcessId())));
		
		Iterator<String> it = definition.getNodes().keySet().iterator();
		while (it.hasNext()) {
			assertNotNull(context.getRoute(String.format("node-%s", it.next())));
		}
	}
	
	protected String getXml(){
		return this.getXml("a", "b");
	}
	
	protected String getXml(String a, String b){
		return "<foo>" +
				"	<a>"+a+"</a>" +
				"	<b>"+b+"</b>" +
				"</foo>";
	}
	
	protected String getNumberXml(){
		return "<foo>" +
				"	<a>1</a>" +
				"	<b>2</b>" +
				"</foo>";
	}
	
	protected ProcessDefinition deployProcess(String process){
		
		// marschal xml
		Process p = template.requestBody("direct:xml2process", process, com.catify.core.process.xml.model.Process.class);
		
		// register process
		template.sendBody("direct://deploy.process", p);
		
//		ProcessDefinition definition = this.getProcessDefinition(process);
//		
//		ProcessDeployer deployer = new ProcessDeployer(context);
//		deployer.deployProcess(definition);
		
		// get definition
		IMap<String, ProcessDefinition> map = Hazelcast.getMap(CacheConstants.PROCESS_CACHE);
		ProcessDefinition definition = map.get(p.getProcessId());
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return definition;
	}
	
	protected ProcessDefinition getProcessDefinition(String process){
		Process p = template.requestBody("direct:xml2process", process, com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(p);
		
		return definition;
	}
	
	@Override
	protected RouteBuilder createRouteBuilder(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				DataFormat jaxb = new JaxbDataFormat("com.catify.core.process.xml.model");
				
				errorHandler(loggingErrorHandler());
				
				from("direct:xml2process")
				.routeId("marshal")
				.unmarshal(jaxb)
				.log("${body}");
				
				from("direct://deploy.process")
				.processRef("processRegistrationProcessor");
				
			}
		};
	}

}
