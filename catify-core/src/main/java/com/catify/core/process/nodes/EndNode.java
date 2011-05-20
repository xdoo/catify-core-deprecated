package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.nodes.Node;

public class EndNode extends Node {

	public EndNode(String processId, String taskName) {
		super(processId, taskName);
	}
	
	private static final long serialVersionUID = 5692221297300800629L;


	@Override
	public int getNodeType() {
		return ProcessConstants.END;
	}

}
