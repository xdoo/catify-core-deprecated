package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;

public class ExceptionEventNode extends Node {

	private static final long serialVersionUID = -3008084420638861682L;

	public ExceptionEventNode(String processId, String nodeName) {
		super(processId, nodeName);
	}

	@Override
	public int getNodeType() {
		return ProcessConstants.EXCEPTIONEVENT;
	}

}
