package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.nodes.Node;

public class LineEndNode extends Node {

	private static final long serialVersionUID = 948508103975995109L;

	public LineEndNode(String processId, String nodeName) {
		super(processId, nodeName, "lineend");
	}

	@Override
	public int getNodeType() {
		return ProcessConstants.LINEEND;
	}

}
