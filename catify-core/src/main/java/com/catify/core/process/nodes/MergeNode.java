package com.catify.core.process.nodes;

import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.nodes.Node;

public class MergeNode extends Node {
	
	private static final long serialVersionUID = -7210011525686028810L;
	
	private int awaitedHits = -1;

	public MergeNode(String processId, String nodeName) {
		super(processId, nodeName, "merge");
	}
	
	public MergeNode(String processId, String nodeName, int awaitedHits) {
		super(processId, nodeName, "merge");
		this.awaitedHits = awaitedHits;
	}

	@Override
	public int getNodeType() {
		return ProcessConstants.MERGE;
	}

	public int getAwaitedHits() {
		return awaitedHits;
	}

	public void setAwaitedHits(int awaitedHits) {
		this.awaitedHits = awaitedHits;
	}
	
	

}
