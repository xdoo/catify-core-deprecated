package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;

public class ReplyNode extends ServiceNode {

	public ReplyNode(String processId, String nodeName,  String serviceId) {
		super(processId, nodeName, serviceId);
	}

	private static final long serialVersionUID = 6541640657080845295L;

	@Override
	public int getNodeType() {
		return ProcessConstants.REPLY;
	}

}
