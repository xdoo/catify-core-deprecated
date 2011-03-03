package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.nodes.Node;

public class DecisionNode extends Node {

	private static final long serialVersionUID = -8844918646553140215L;

	public DecisionNode(String processId, String nodeName) {
		super(processId, nodeName);
	}

	@Override
	public int getNodeType() {
		return ProcessConstants.DECISION;
	}

}
