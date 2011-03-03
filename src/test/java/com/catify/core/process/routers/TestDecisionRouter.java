package com.catify.core.process.routers;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelTestSupport;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;

import com.catify.core.constants.MessageConstants;
import com.catify.core.process.processors.TransformDecisionPayloadProcessor;
import com.catify.core.process.routers.DecisionRouter;

public class TestDecisionRouter extends CamelTestSupport {

	public void testRouting() throws InstantiationException, IllegalAccessException{		
		//get router
		DecisionRouter router = new DecisionRouter(this.createKnowledgeBase());
//		DecisionRouter router = new DecisionRouter();
		
		//send payload
		String result = router.route(this.getPayload(20));
		
		assertEquals("seda:node-c5f00e5f3462795fdc4ccc98b4bbb818", result);
	}
	
	public void testRoutingFromCamel() throws Exception{
		
		context.addRoutes(this.decisionRoute(this.createKnowledgeBase()));
		
		MockEndpoint out = super.getMockEndpoint("mock:out20");
		out.setMinimumExpectedMessageCount(1);
		
		template.sendBody("direct:start1", this.getPayload(20));
		
		out.assertIsSatisfied();
		
	}
	
	public void testConcurrentDecisionsWithSamePayload() throws Exception{
		
		context.addRoutes(this.decisionRoute(this.createKnowledgeBase()));
		
		int cnt = 30;
		
		MockEndpoint out = super.getMockEndpoint("mock:out20");
		out.setMinimumExpectedMessageCount(cnt);
		
		for (int i = 0; i < cnt; i++) {
			template.sendBody("direct:start1", this.getPayload(20));
		}
		
		out.assertIsSatisfied();
		
	}
	
	public void testSequentiellDecisionsWithHighLoadAndDifferentPayloads() throws Exception{
		context.addRoutes(this.decisionRoute(this.createKnowledgeBase()));
		
		int cnt = 20;
		
		MockEndpoint m10 = super.getMockEndpoint("mock:out10");
		MockEndpoint m20 = super.getMockEndpoint("mock:out20");
		MockEndpoint m30 = super.getMockEndpoint("mock:out30");
		
		m10.setExpectedMessageCount(cnt);
		m20.setExpectedMessageCount(cnt);
		m30.setExpectedMessageCount(cnt);
		
		for (int i = 0; i < cnt; i++) {
			template.sendBody("direct:start1", this.getPayload(10));
			template.sendBody("direct:start1", this.getPayload(20));
			template.sendBody("direct:start1", this.getPayload(30));
		}
		
		m10.assertIsSatisfied();
		m20.assertIsSatisfied();
		m30.assertIsSatisfied();
		
	}
	
	public void testConcurrentDecisionWithHighLoadAndDifferentPayloads() throws Exception{
		context.addRoutes(this.decisionRoute(this.createKnowledgeBase()));
		
		int cnt = 100;
		
		MockEndpoint m10 = super.getMockEndpoint("mock:out10");
		MockEndpoint m20 = super.getMockEndpoint("mock:out20");
		MockEndpoint m30 = super.getMockEndpoint("mock:out30");
		
		int x = cnt * 4;
		
		m10.setExpectedMessageCount(x);
		m20.setExpectedMessageCount(x);
		m30.setExpectedMessageCount(x);
		
		for (int i = 0; i < cnt; i++) {
			template.sendBody("direct:multi", null);
		}
		
		Thread.sleep(2500);
		
		System.out.println(String.format("10 --> %s / 20 --> %s / 30 --> %s", m10.getReceivedCounter(), m20.getReceivedCounter(), m30.getReceivedCounter()));
		
		m10.assertIsSatisfied();
		m20.assertIsSatisfied();
		m30.assertIsSatisfied();
		
	}
	
	public void testRoutingFromCamelWithPayloadCreator() throws Exception{
		context.addRoutes(this.decisionRoute(this.createKnowledgeBase()));
		
		MockEndpoint out = super.getMockEndpoint("mock:out20");
		out.setMinimumExpectedMessageCount(1);
		
		Map<String,Object> headers = new HashMap<String, Object>();
		headers.put(MessageConstants.ACCOUNT_NAME, "tester");
		headers.put(MessageConstants.TASK_ID, "cfd7332952eff5ecdbfba16124067d70");
		
		template.sendBodyAndHeaders("direct:start2", this.getSimplePayload(20), headers);
		
		out.assertIsSatisfied();
	}
	
	public void testDifferentResults() throws Exception{
		context.addRoutes(this.decisionRoute(this.createKnowledgeBase()));
		
		MockEndpoint out20 = super.getMockEndpoint("mock:out20");
		out20.setMinimumExpectedMessageCount(1);
		
		MockEndpoint out30 = super.getMockEndpoint("mock:out30");
		out30.setMinimumExpectedMessageCount(1);
		
		Map<String,Object> headers = new HashMap<String, Object>();
		headers.put(MessageConstants.ACCOUNT_NAME, "tester");
		headers.put(MessageConstants.TASK_ID, "cfd7332952eff5ecdbfba16124067d70");
		
		template.sendBodyAndHeaders("direct:start2", this.getSimplePayload(30), headers);
		template.sendBodyAndHeaders("direct:start2", this.getSimplePayload(20), headers);
		
		out20.assertIsSatisfied();
		out30.assertIsSatisfied();
	}
	
	private RouteBuilder decisionRoute(final KnowledgeBase kbase){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:multi")
				.multicast().to("seda:start10", "seda:start20", "seda:start30", "seda:start10", "seda:start20", "seda:start30"
						,"seda:start10", "seda:start20", "seda:start30", "seda:start10", "seda:start20", "seda:start30"
							);
				
				from("seda:start10?concurrentConsumers=1")
				.setBody(constant(getPayload(10)))
				.dynamicRouter(bean(new DecisionRouter(createKnowledgeBase()), "route"));
				
				from("seda:start20?concurrentConsumers=1")
				.setBody(constant(getPayload(20)))
				.dynamicRouter(bean(new DecisionRouter(createKnowledgeBase()), "route"));
				
				from("seda:start30?concurrentConsumers=1")
				.setBody(constant(getPayload(30)))
				.dynamicRouter(bean(new DecisionRouter(createKnowledgeBase()), "route"));
				
				from("direct:start1")
				.dynamicRouter(bean(new DecisionRouter(createKnowledgeBase()), "route"));
				
				from("direct:start2")
				.process(new TransformDecisionPayloadProcessor())
				.dynamicRouter(bean(new DecisionRouter(createKnowledgeBase()), "route"));
				
				from("seda:node-02f7c32af3d977dcddcbcd77cf7500ff")
				.to("mock:out10");
				
				from("seda:node-c5f00e5f3462795fdc4ccc98b4bbb818")
				.to("mock:out20");
				
				from("seda:node-98fc2186952c29fd4ee8749b036c5eee")
				.to("mock:out30");			
				
			}
		};
	}
	
	private KnowledgeBase createKnowledgeBase(){
		final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
		.newKnowledgeBuilder();
		
		// this will parse and compile in one step		
		kbuilder.add(ResourceFactory.newClassPathResource("META-INF/rules/types.drl"), ResourceType.DRL);
//		kbuilder.add(ResourceFactory.newClassPathResource("rules/DecisionTypes.drl"), ResourceType.DRL);
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
	
	
	private String getPayload(int value){
		String xml = String.format(
			"<com.catify.tester.class_cfd7332952eff5ecdbfba16124067d70>\n" +
			"<payload><value>%s</value></payload>" +
			"</com.catify.tester.class_cfd7332952eff5ecdbfba16124067d70>", value);
		
		return xml;
	}
	
	private String getSimplePayload(int value){
		return String.format("<payload><value>%s</value></payload>", value);
	}
	
}
