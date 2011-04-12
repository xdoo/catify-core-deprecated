package com.catify.core.process.routers;

import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentFactory;
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

public class DecisionRouter {

	private StatefulKnowledgeSession ksession;
	private Pipeline pipeline;
	
	private boolean first = true;
	private com.catify.core.process.routers.DecisionRouter.ResultHandlerImpl handler;
	private FactType decisionType;
	private Transformer transformer;

	public DecisionRouter(KnowledgeBase kbase){	
		
//		//resource scanner
//		KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent( "MyAgent" );
//		kagent.applyChangeSet( ResourceFactory.newByteArrayResource(this.getChangeConfiguration()) );
//		KnowledgeBase kbase = kagent.getKnowledgeBase();
		
		this.ksession = kbase.newStatefulKnowledgeSession();
		
	    // Make the results, in this case the FactHandles, available to the user
	    Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

	    // Insert the transformed object into the session associated with the PipelineContext
	    KnowledgeRuntimeCommand insertStage = PipelineFactory.newStatefulKnowledgeSessionInsert();
	    insertStage.setReceiver( executeResultHandler );
	        
	    // Create the transformer instance and create the Transformer stage, where we are going from Xml to Pojo.
	    XStream xstream = new XStream();
	    this.transformer = PipelineFactory.newXStreamFromXmlTransformer( xstream );
	    transformer.setReceiver( insertStage );

	    // Create the start adapter Pipeline for StatefulKnowledgeSessions
	    this.pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
	    pipeline.setReceiver( transformer );
	    
	    //create resource handler
	    this.handler = new ResultHandlerImpl();
	    
	    //decision type
	    this.decisionType = kbase.getFactType( 	"com.catify.types",
    	"Decision" );
	}
	
	public String route(String payload) throws IllegalAccessException, InstantiationException{
		
		if(first){
			first = false;
			
			//insert result object
			Object decision = decisionType.newInstance();
		    ksession.insert(decision);
		    
			//insert payload
			pipeline.insert(ResourceFactory.newByteArrayResource(payload.getBytes()), this.handler);
			ksession.insert(this.handler.getResult());
			
			//fire!
			ksession.fireAllRules();
			
			String result = (String) decisionType.get(decision, "result");
			
			return String.format("seda:%s", result);
			
		} else {
			first = true;
			return null;
		}
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
