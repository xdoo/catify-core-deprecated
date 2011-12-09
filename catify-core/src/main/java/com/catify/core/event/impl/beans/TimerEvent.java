package com.catify.core.event.impl.beans;

import java.io.Serializable;

public class TimerEvent implements Serializable {

	private static final long serialVersionUID = 4233809355282392353L;
	
	// the id is needed for persistence
	private String id;
	private long time;
	private String instanceId;
	private String taskId;
	
	public TimerEvent() {}
	
	public TimerEvent(long time, String instanceId, String taskId){
		this.time = time;
		this.instanceId = instanceId;
		this.taskId = taskId;
	}
		
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public String getInstanceId() {
		return instanceId;
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}


	public String getTaskId() {
		return taskId;
	}


	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	
}
