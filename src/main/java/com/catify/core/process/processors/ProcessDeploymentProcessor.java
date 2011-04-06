package com.catify.core.process.processors;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.drools.KnowledgeBaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.process.ProcessDeployer;
import com.catify.core.process.model.ProcessDefinition;

public class ProcessDeploymentProcessor extends BaseProcessor {

	static final Logger LOG = LoggerFactory
			.getLogger(ProcessDeploymentProcessor.class);
	
	public void process(Exchange ex) throws Exception {
		
		//get camel context
		CamelContext context = ex.getContext();
		
		//process deployer 
		//TODO --> do something with knowledge base
		ProcessDeployer deployer = new ProcessDeployer(context, KnowledgeBaseFactory.newKnowledgeBase());
		deployer.deployProcess(ex.getIn().getBody(ProcessDefinition.class));
		
//		LOG.info(String.format("process deployed --> name = %s | version = %s | account = %s", definition.getProcessName(), definition.getProcessVersion(), definition.getAccountName()));
		
		super.copyBodyAndHeaders(ex);
	}

}
