package com.catify.core.event.impl.beans;

import java.io.Serializable;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.Header;

import com.catify.core.constants.MessageConstants;

public class PayloadEvent implements Serializable {

	private static final long serialVersionUID = -5918833981928700394L;
	
	private String instanceId;
	private String payload;

	public PayloadEvent(){}
	
	public PayloadEvent(String instanceId, String payload){
		this.instanceId = instanceId;
		this.payload = payload;
	}

	//MessageConstants.INSTANCE_ID, variable.getName()
	
	@Handler
	public PayloadEvent proxy(	@Header(MessageConstants.INSTANCE_ID) String instanceId,
								@Body String body){
		
		this.instanceId = instanceId;
		this.payload = body;
		
		return this;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
	
}
