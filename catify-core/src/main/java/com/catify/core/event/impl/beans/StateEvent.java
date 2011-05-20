package com.catify.core.event.impl.beans;

import java.io.Serializable;

import org.apache.camel.Handler;
import org.apache.camel.Header;

import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;

public class StateEvent implements Serializable {

	private static final long serialVersionUID = -5918833981928700394L;
	
	private String instanceId;
	private int state;

	public StateEvent(){}
	
	public StateEvent(String instanceId, int state){
		this.instanceId = instanceId;
		this.state = state;
	}
	
	@Handler
	public StateEvent proxy(	@Header(MessageConstants.INSTANCE_ID) String instanceId,
								@Header(ProcessConstants.STATE) int state){
		
		this.instanceId = instanceId;
		this.state 		= state;
		
		return this;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
}
