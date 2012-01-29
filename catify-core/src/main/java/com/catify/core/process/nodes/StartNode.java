package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.nodes.Node;

public class StartNode extends Node {


	public StartNode(String processId, String nodeName) {
		super(processId, nodeName, "start");
	}

	private static final long serialVersionUID = -1231792093855525644L;

	@Override
	public int getNodeType() {
		return ProcessConstants.START;
	}

}
