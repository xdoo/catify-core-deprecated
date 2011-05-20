package com.catify.rules.drools;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.type.FactType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Transformer;

import com.thoughtworks.xstream.XStream;

public class DroolsTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testSimpleRule() throws InstantiationException, IllegalAccessException{
		final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
		.newKnowledgeBuilder();
		
		// this will parse and compile in one step
		kbuilder.add(ResourceFactory.newByteArrayResource(this.getWorldType()), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newByteArrayResource(this.getHelloWorldRule()), ResourceType.DRL);
		
		//check for errors
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		} else {
			KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		    kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
		    StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		    
		    // get the declared FactType
		    FactType worldType = kbase.getFactType( "com.catify.test.types",
		                                            "World" );
		    
		    //set values
		    Object world = worldType.newInstance();
		    
		    worldType.set(world, "greeting", "hello world");
		    worldType.set(world, "state", 1);
		    
		    ksession.insert(world);
		    ksession.fireAllRules();
		    
		    String greeting = (String) worldType.get(world, "greeting");
		    System.out.println("---> " + greeting);
		}
	}
	
	public void testSimpleRuleWithXml() throws InstantiationException, IllegalAccessException{
		final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
		.newKnowledgeBuilder();
		
		// this will parse and compile in one step
		kbuilder.add(ResourceFactory.newByteArrayResource(this.getWorldType()), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newByteArrayResource(this.getHelloWorldRule()), ResourceType.DRL);
		
		//check for errors
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		} else {
			KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		    kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
		    StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		    
		    // Make the results, in this case the FactHandles, available to the user
		    Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

		    // Insert the transformed object into the session associated with the PipelineContext
		    KnowledgeRuntimeCommand insertStage = PipelineFactory.newStatefulKnowledgeSessionInsert();
		    insertStage.setReceiver( executeResultHandler );
		        
		    // Create the transformer instance and create the Transformer stage, where we are going from Xml to Pojo.
		    XStream xstream = new XStream();
		    Transformer transformer = PipelineFactory.newXStreamFromXmlTransformer( xstream );
		    transformer.setReceiver( insertStage );

		    // Create the start adapter Pipeline for StatefulKnowledgeSessions
		    Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
		    pipeline.setReceiver( transformer );
		    
		    //create decision object
		    FactType decisionType = kbase.getFactType( 	"com.catify.test.types",
		                                            	"Decision" );
		    
		    //get instance
		    Object decision = decisionType.newInstance();
		    ksession.insert(decision);
		    
		    ResultHandlerImpl handler = new ResultHandlerImpl();
		    
		    pipeline.insert(ResourceFactory.newByteArrayResource(this.getSimpleXml()), handler);
		    
		    System.out.println("---> " + handler.getResult());
		    
		    ksession.insert(handler.getResult());
		    ksession.fireAllRules();
		    
		    //complex xml
		    pipeline.insert(ResourceFactory.newByteArrayResource(this.getComplexXml()), handler);
		    
		    System.out.println("---> " + handler.getResult());
		    
		    ksession.insert(handler.getResult());
		    ksession.fireAllRules();
		    
		    String result = (String) decisionType.get(decision, "result");
		    System.out.println("---> " + result);
		}
	}
	
	public void testDiffrentSessions(){
		final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
		.newKnowledgeBuilder();
		
		// this will parse and compile in one step
		kbuilder.add(ResourceFactory.newByteArrayResource(this.getWorldType()), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newByteArrayResource(this.getHelloWorldRule()), ResourceType.DRL);
		
		//check for errors
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		} else {
			KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		    kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
		    StatefulKnowledgeSession ksession1 = kbase.newStatefulKnowledgeSession();
		    StatefulKnowledgeSession ksession2 = kbase.newStatefulKnowledgeSession();
		    
		    
		}
	}
	
	public void testRealPayload() throws InstantiationException, IllegalAccessException{
		
		final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
		.newKnowledgeBuilder();
		
		// this will parse and compile in one step
		kbuilder.add(ResourceFactory.newByteArrayResource(this.getDecisionType()), ResourceType.DRL);
		
		//check for errors
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		} else {
			KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		    kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
		    StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		    
			kbuilder.add(ResourceFactory.newByteArrayResource(this.getPayloadTypes()), ResourceType.DRL);
			kbuilder.add(ResourceFactory.newByteArrayResource(this.getPayloadRules()), ResourceType.DRL);
			kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
		    
		    // Make the results, in this case the FactHandles, available to the user
		    Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

		    // Insert the transformed object into the session associated with the PipelineContext
		    KnowledgeRuntimeCommand insertStage = PipelineFactory.newStatefulKnowledgeSessionInsert();
		    insertStage.setReceiver( executeResultHandler );
		        
		    // Create the transformer instance and create the Transformer stage, where we are going from Xml to Pojo.
		    XStream xstream = new XStream();
		    Transformer transformer = PipelineFactory.newXStreamFromXmlTransformer( xstream );
		    transformer.setReceiver( insertStage );

		    // Create the start adapter Pipeline for StatefulKnowledgeSessions
		    Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
		    pipeline.setReceiver( transformer );
		    
		    //create decision object
		    FactType decisionType = kbase.getFactType( 	"com.catify.types",
		                                            	"Decision" );
		    
		    //get instance
		    Object decision = decisionType.newInstance();
		    ksession.insert(decision);
		    
		    ResultHandlerImpl handler = new ResultHandlerImpl();
		    
		    pipeline.insert(ResourceFactory.newByteArrayResource(this.getPayload()), handler);
		    
		    System.out.println("---> " + handler.getResult());
		    
		    ksession.insert(handler.getResult());
		    ksession.fireAllRules();
		    
		    String result = (String) decisionType.get(decision, "result");
		    System.out.println("---> " + result);
		}
	}
	
	
	private byte[] getHelloWorldRule(){

		
		String rule = 
			" 	package com.catify.test.rules\n" +
			"	\n" +
			"	import com.catify.test.types.*" +
//			"	import com.catify.test.types.World\n" +
//			"	import com.catify.test.types.Bar\n" +
//			"	import com.catify.test.types.Foo\n" +
//			"	import com.catify.test.types.Decision\n" +
//			"	import com.catify.test.types.Payload\n" +
//			"	import com.catify.test.types.cfd7332952eff5ecdbfba16124067d70" +
			"	\n" +
			"	rule \"hello_world\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		//lhs\n"+
			"		$w: World( state == 1, $greeting : greeting )\n"+
			"	then\n"+
			"		//rhs\n" +
			"		modify( $w ) { greeting = \"by world\", state = 2 }\n" +
			"	end\n"+
			"	\n"+
			"	rule \"by_world\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		//lhs\n"+
			"		$w: World( state == 2, $greeting : greeting )\n"+
			"	then\n"+
			"		//rhs\n"+
			"		System.out.println( \"---> \" + $greeting )\n"+
			"	end\n" +
			"	\n" +
			"	rule \"complex xml\"\n" +
			"	dialect \"mvel\"\n" +
			"	when\n" +
			"		//lhs\n" +
			"		$bar: Bar( state == 1 )\n" +
			"		$dec: Decision( )\n" +
			"	then\n" +
			"		System.out.println( \"---> \" + $bar.foo.a)\n" +
			"		modify( $dec ) { result = \"foo\" }\n" +
			"		modify( $bar ) { state = 2 }\n" +
			"	end\n" +
			"	\n" +
			"	rule \"to-02f7c32af3d977dcddcbcd77cf7500ff\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		//value equals 10\n"+
			"		$p: cfd7332952eff5ecdbfba16124067d70( payload.value == 10 )\n" +
			"		$d: Decision( )\n"+
			"	then\n"+
			"		//goto node 02f7c32af3d977dcddcbcd77cf7500ff\n" +
			"		modify( $d ) { result = \"node-02f7c32af3d977dcddcbcd77cf7500ff\" }\n" +
			"		retract( $p )\n" +
			"		System.out.println( \"----> 10\" )\n" +
			"	end\n"+
			"	\n" +
			"	rule \"to-c5f00e5f3462795fdc4ccc98b4bbb818\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		//value equals 10\n"+
			"		$p: cfd7332952eff5ecdbfba16124067d70( payload.value == 20 )\n" +
			"		$d: Decision( )\n"+
			"	then\n"+
			"		//goto node c5f00e5f3462795fdc4ccc98b4bbb818\n" +
			"		modify( $d ) { result = \"node-c5f00e5f3462795fdc4ccc98b4bbb818\" }\n" +
			"		System.out.println( \"---> 20\" )\n" +
			"		retract( $p )" +
			"	end\n"+
			"	\n" +
			"	rule \"to-98fc2186952c29fd4ee8749b036c5eee\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		//value equals 10\n"+
			"		$p: cfd7332952eff5ecdbfba16124067d70( payload.value == 30 )\n" +
			"		$d: Decision( )\n"+
			"	then\n"+
			"		//goto node 98fc2186952c29fd4ee8749b036c5eee\n" +
			"		modify( $d ) { result = \"node-98fc2186952c29fd4ee8749b036c5eee\" }\n" +
			"		retract( $p )\n" +
			"		System.out.println( \"----> 30\" )\n" +
			"	end\n" +
			"	\n"+
			"	rule \"y\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		$p: cfd7332952eff5ecdbfba16124067d70( )\n" +
			"	then\n"+
			"		System.out.println( \"objekt da....\" )\n" +
			"	end\n";
		
		
		System.out.println(rule);
		
		return rule.getBytes();
	}
	
	private byte[] getWorldType(){
		
		String declare = 
			" 	package com.catify.test.types\n"+
			"	\n"+
			"	declare World\n"+
			"		greeting : String\n"+
			"		state : int\n"+
			"	end" +
			"	\n" +
			"	declare Foo\n" +
			"		a : String\n" +
			"		b : String\n" +
			"	end\n" +
			"	\n" +
			"	declare Bar\n" +
			"		state : int\n" +
			"		foo : Foo\n" +
			"	end\n" +
			"	\n" +
			"	declare Decision\n" +
			"		result : String\n" +
			"	end" +
			"	\n" +
			"	declare Payload\n" +
			"		value : int\n" +
			"	end\n" +
			"	\n"+
			"	declare cfd7332952eff5ecdbfba16124067d70\n"+
			"		payload : Payload\n"+
			"	end";
		
		
		return declare.getBytes();
	}
	

	
	private byte[] getDecisionType(){
		
		String types = 
			"package com.catify.types" +
			"\n" +
			"declare Decision\n" +
			"	result : String\n" +
			"end";
		
		return types.getBytes();
	}
	
	private byte[] getPayloadTypes(){
		
		String types = 
			" 	package com.catify.tester\n"+
			"	\n" +
			"	declare Payload\n" +
			"		value : int\n" +
			"	end\n" +
			"	\n"+
			"	declare cfd7332952eff5ecdbfba16124067d70\n"+
			"		payload : Payload\n"+
			"	end";
		
		return types.getBytes();
	}
	
	private byte[] getPayloadRules(){
		
		String rules = 
			" 	package com.catify.rules\n" +
			"	\n" +
			"	import com.catify.types.Decision\n" +
			"	import com.catify.tester.*\n" +
			"	\n" +
			"	\n" +
			"	rule \"to-02f7c32af3d977dcddcbcd77cf7500ff\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		//value equals 10\n"+
			"		$p: cfd7332952eff5ecdbfba16124067d70( payload.value == 10 )\n" +
			"		$d: Decision( )\n"+
			"	then\n"+
			"		//goto node 02f7c32af3d977dcddcbcd77cf7500ff\n" +
			"		modify( $d ) { result = \"node-02f7c32af3d977dcddcbcd77cf7500ff\" }\n" +
			"		retract( $p )\n" +
			"		System.out.println( \"----> 10\" )\n" +
			"	end\n"+
			"	\n" +
			"	rule \"to-c5f00e5f3462795fdc4ccc98b4bbb818\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		//value equals 20\n"+
			"		$p: cfd7332952eff5ecdbfba16124067d70( payload.value == 20 )\n" +
			"		$d: Decision( )\n"+
			"	then\n"+
			"		//goto node c5f00e5f3462795fdc4ccc98b4bbb818\n" +
			"		modify( $d ) { result = \"node-c5f00e5f3462795fdc4ccc98b4bbb818\" }\n" +
			"		System.out.println( \"---> 20\" )\n" +
			"		retract( $p )" +
			"	end\n"+
			"	\n" +
			"	rule \"to-98fc2186952c29fd4ee8749b036c5eee\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		//value equals 30\n"+
			"		$p: cfd7332952eff5ecdbfba16124067d70( payload.value == 30 )\n" +
			"		$d: Decision( )\n"+
			"	then\n"+
			"		//goto node 98fc2186952c29fd4ee8749b036c5eee\n" +
			"		modify( $d ) { result = \"node-98fc2186952c29fd4ee8749b036c5eee\" }\n" +
			"		retract( $p )\n" +
			"		System.out.println( \"----> 30\" )\n" +
			"	end\n" +
			"	\n" +
			"	rule \"x\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		$d: Decision( )\n"+
			"	then\n"+
			"		System.out.println( \"decision....\" )\n" +
			"	end\n"+
			"	\n" +
			"	rule \"y\"\n" +
			"	dialect \"mvel\"\n"+
			"	when\n"+
			"		$p: cfd7332952eff5ecdbfba16124067d70( )\n" +
			"	then\n"+
			"		System.out.println( \"objekt da....\" )\n" +
			"	end\n";
		
		return rules.getBytes();
	}
	
	private byte[] getSimpleXml(){
		String xml =
			"<com.catify.test.types.World>\n" +
			"	<greeting>hello world</greeting>\n" +
			"	<state>1</state>\n" +
			"</com.catify.test.types.World>";
		
		return xml.getBytes();
	}
	
	private byte[] getComplexXml(){
		String xml = 
			"<com.catify.test.types.World>\n" +
			"	<greeting>hello world</greeting>\n" +
			"	<state>1</state>\n" +
			"</com.catify.test.types.World>";
		
		return xml.getBytes();
	}
	
	private byte[] getPayload(){
		String xml =
			"<com.catify.tester.cfd7332952eff5ecdbfba16124067d70>\n" +
			"<payload><value>20</value></payload>" +
			"</com.catify.tester.cfd7332952eff5ecdbfba16124067d70>";
		
		return xml.getBytes();
	}

	public class ResultHandlerImpl implements ResultHandler{
		
		private Object result;
		
		public void handleResult(Object result) {
			this.result = result;
		}

		public Object getResult() {
			return result;
		}
		
	}
}
