package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;

public class RequestNode extends ServiceNode {

	public RequestNode(String processId, String taskName,  String serviceId) {
		super(processId, taskName, serviceId);
	}

	private static final long serialVersionUID = -2692746377410029266L;

	@Override
	public int getNodeType() {
		return ProcessConstants.REQUEST;
	}

}
