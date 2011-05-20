package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.nodes.Node;

public class ForkNode extends Node {

	private static final long serialVersionUID = 2771099004884397971L;

	public ForkNode(String processId, String nodeName) {
		super(processId, nodeName);
	}

	@Override
	public int getNodeType() {
		return ProcessConstants.FORK;
	}

}
