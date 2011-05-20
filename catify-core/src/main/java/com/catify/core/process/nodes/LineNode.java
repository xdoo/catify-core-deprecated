package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.nodes.Node;

public class LineNode extends Node {

	private static final long serialVersionUID = -95469388426047426L;

	public LineNode(String processId, String nodeName) {
		super(processId, nodeName);
	}

	@Override
	public int getNodeType() {
		return ProcessConstants.LINE;
	}

}
