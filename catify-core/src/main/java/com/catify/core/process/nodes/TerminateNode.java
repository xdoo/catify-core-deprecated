package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;

public class TerminateNode extends Node {

	private static final long serialVersionUID = -6563527383313830658L;

	public TerminateNode(String processId, String nodeName) {
		super(processId, nodeName);
	}

	@Override
	public int getNodeType() {
		return ProcessConstants.TERMINATE;
	}

}
