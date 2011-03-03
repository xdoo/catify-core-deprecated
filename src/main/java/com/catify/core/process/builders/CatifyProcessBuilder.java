package com.catify.core.process.builders;

import com.catify.core.process.nodes.Node;
import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.nodes.StartNode;

public class CatifyProcessBuilder extends MainProcessBuilder {
	
	public MainProcessBuilder start(String accountName, String processName, String processVersion, String name) {		
		processDefinition = new ProcessDefinition(accountName, processName, processVersion);
		processId = processDefinition.getProcessId();
		
		Node node = new StartNode(processId, this.addToNodeNames(name));
		currentNode = processDefinition.addNode(node);
		
		//set start node id
		processDefinition.setStartNodeId(node.getNodeId());
		
		return this;
	}
	
	public MainProcessBuilder start(String accountName, String processName, String processVersion){
		return this.start(accountName, processName, processVersion, this.createNodeName());
	}
	
}
