package com.catify.core.testsupport;

import java.util.ArrayList;
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
import com.catify.core.process.ProcessHelper;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.xml.XmlProcessBuilder;
import com.catify.core.process.xml.model.Process;
import com.hazelcast.core.Hazelcast;

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
	
	protected String getTransformation(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n" +
				"	<xsl:template match=\"*\">" +
				"		<xsl:copy>" +
				"			<xsl:apply-templates/>" +
				"		</xsl:copy>" +
				"	</xsl:template>"+
				"</xsl:stylesheet>";
	}
	
	protected String getXml(){
		return "<foo>" +
				"	<a>a</a>" +
				"	<b>b</b>" +
				"</foo>";
	}
	
	protected void insertXslts(List<String> ids){
		
		Iterator<String> it = ids.iterator();
		while (it.hasNext()) {
			Hazelcast.getMap(CacheConstants.TRANSFORMATION_CACHE).put(it.next(), this.getTransformation());
			
		}
		
	}
	
	protected void insertXslts(List<String> names, String process, String version, String account){
		
		List<String> ids = new ArrayList<String>();
		String pid = ProcessHelper.createProcessId(account, process, version);
		
		//always inster process xslt
		ids.add(pid);
		
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			ids.add(ProcessHelper.createTaskId(pid, name));
		}
		
		//put them into cache
		this.insertXslts(ids);
		
	}
	
	protected ProcessDefinition deployProcess(String process, List<String> ids){
		
		ProcessDefinition definition = this.getProcessDefinition(process);
		
		this.insertXslts(ids);
		
		ProcessDeployer deployer = new ProcessDeployer(context);
		deployer.deployProcess(definition);
		
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
				
			}
		};
	}

}
