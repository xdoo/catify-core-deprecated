package com.catify.core.process.nodes;

import java.io.Serializable;
import java.util.UUID;

import com.catify.core.process.ProcessHelper;

public abstract class Node implements Serializable {

	private static final long serialVersionUID = -8180371187354900197L;
	protected String nodeId;
	protected String nodeName;

	public Node(String processId, String nodeName){
		
		//generate a unique identifier for the task
		this.nodeId = ProcessHelper.createTaskId(processId, nodeName);
		
		//convenient method for setting a node name 
		if(nodeName == null) {
			
			//if no node name has been set, simply generate one...
			this.nodeName = UUID.randomUUID().toString();
		} else {
			
			//...otherwise use the one that has been set.
			this.nodeName = nodeName;
		}
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getNodeName() {
		return nodeName;
	}
	
	
	
	public abstract int getNodeType();
	
}
