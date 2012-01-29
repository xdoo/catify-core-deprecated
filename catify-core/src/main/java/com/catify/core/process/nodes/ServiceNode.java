package com.catify.core.process.nodes;

import com.catify.core.process.nodes.Node;

public abstract class ServiceNode extends Node {
	
	public ServiceNode(String processId, String nodeName, String serviceId) {
		super(processId, nodeName, "service");
		this.serviceId = serviceId;
	}

	private static final long serialVersionUID = -8918225775051993302L;
	protected String serviceId;

	public abstract int getNodeType();

	public String getServiceId() {
		return serviceId;
	}

}
