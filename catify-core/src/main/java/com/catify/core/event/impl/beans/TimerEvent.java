package com.catify.core.event.impl.beans;

import java.io.Serializable;

public class TimerEvent implements Serializable {

	private static final long serialVersionUID = 4233809355282392353L;
	
	// the id is needed for persistence
	private String id;
	private long time;
	private String instanceId;
	private String taskId;
	private String account;
	private String process;
	private String version;
	private String nodename;
	
	public TimerEvent() {}
	
	public TimerEvent(long time, String instanceId, String taskId, String account, String process, String version, String nodename){
		this.time = time;
		this.instanceId = instanceId;
		this.taskId = taskId;
		this.account = account;
		this.process = process;
		this.version = version;
		this.nodename = nodename;
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

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getNodename() {
		return nodename;
	}

	public void setNodename(String nodename) {
		this.nodename = nodename;
	}
	
}
