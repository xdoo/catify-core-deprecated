package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;

public class TimerEventNode extends Node {

	private static final long serialVersionUID = -6563527383313830658L;

	public TimerEventNode(String processId, String nodeName) {
		super(processId, nodeName, "timer.event");
	}

	@Override
	public int getNodeType() {
		return ProcessConstants.TIMEREVENT;
	}

}
