package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;

public class ReceiveNode extends Node{
	
	public ReceiveNode(String processId, String nodeName, long timeout) {
		super(processId, nodeName);
		
		this.timeout = timeout;
	}

	private static final long serialVersionUID = 3446867841175541457L;
	private long timeout;

	@Override
	public int getNodeType() {
		return ProcessConstants.RECEIVE;
	}

	public long getTimeout() {
		return timeout;
	}

}
