package com.catify.core.testsupport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.CamelSpringTestSupport;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.DataBaseConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.process.ProcessDeployer;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.xml.XmlProcessBuilder;
import com.catify.core.process.xml.model.Process;
import com.hazelcast.core.Hazelcast;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class ProcessBase extends CamelSpringTestSupport {

	protected DBCollection timerCollection;
	private DB db;

	protected void setUp() throws Exception {
		
		super.setUp();
		
		Mongo m = new Mongo( "localhost" , 27017 );
		this.db = m.getDB( DataBaseConstants.MONGO_DB );
		this.timerCollection = this.db.getCollection( "timer" );
		
	}

	protected void tearDown() throws Exception {
		this.db.dropDatabase();
		
		super.tearDown();
	}
	
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
	
	protected KnowledgeBase createKnowledgeBase(){
		final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
		.newKnowledgeBuilder();
		
		// this will parse and compile in one step
		kbuilder.add(ResourceFactory.newClassPathResource("META-INF/rules/types.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/DecisionTypes.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/DecisionRules.drl"), ResourceType.DRL);
		
		//check for errors
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
	    kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
			    
	    return kbase;
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
	
	protected ProcessDefinition deployProcess(String process, List<String> ids){
		Process p = template.requestBody("direct:xml2process", process, com.catify.core.process.xml.model.Process.class);
		XmlProcessBuilder processBuilder = (XmlProcessBuilder) applicationContext.getBean("xmlProcessBuilder");
		
		ProcessDefinition definition = processBuilder.build(p);
		
		this.insertXslts(ids);
		
		ProcessDeployer deployer = new ProcessDeployer(context, this.createKnowledgeBase());
		deployer.deployProcess(definition);
		
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
