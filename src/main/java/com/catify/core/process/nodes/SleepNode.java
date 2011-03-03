package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;

public class SleepNode extends Node {

	public SleepNode(String processId, String nodeName, long time) {
		super(processId, nodeName);
		
		this.time = time;
	}

	private static final long serialVersionUID = -2345592802766478253L;
	private long time;

	@Override
	public int getNodeType() {
		return ProcessConstants.SLEEP;
	}

	public long getTime() {
		return time;
	}

}
