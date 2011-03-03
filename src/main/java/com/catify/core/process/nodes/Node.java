package com.catify.core.process.nodes;

import java.io.Serializable;

import org.apache.commons.codec.digest.DigestUtils;

public abstract class Node implements Serializable {

	private static final long serialVersionUID = -8180371187354900197L;
	protected String nodeId;
	protected String nodeName;

	public Node(String processId, String nodeName){
		
		//generate a unique identifier for the task
		this.nodeId = DigestUtils.md5Hex(String.format("%s%s", processId, nodeName));
		
		this.nodeName = nodeName;
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getNodeName() {
		return nodeName;
	}
	
	
	
	public abstract int getNodeType();
	
}
