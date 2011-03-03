package com.catify.core.model.pipeline;

import java.io.Serializable;

public class PipelineDefinition implements Serializable {

	private static final long serialVersionUID = -7679479288676609567L;
	
	private String processId;
	private String taskId;
	
	private boolean validate 				= false;
	private boolean transform 				= false;
	private boolean addCorrelationContext	= false;
	private boolean loadCorrelationContext	= false;
	private boolean addPayload				= false;
	private boolean loadPayload				= false;
	
	public PipelineDefinition () {}
	
	public PipelineDefinition (String processId, String taskId, boolean validate, boolean transform, boolean addCorrelationContext, boolean loadCorrelationContext){
		
		this.processId 				= processId;
		this.taskId 				= taskId;
		
		this.validate 				= validate;
		this.transform 				= transform;
		this.addCorrelationContext 	= addCorrelationContext;
		this.loadCorrelationContext = loadCorrelationContext;
	}
	
	public boolean isValidate() {
		return validate;
	}
	
	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	
	public boolean isTransform() {
		return transform;
	}
	
	public void setTransform(boolean transform) {
		this.transform = transform;
	}
	
	public boolean isAddCorrelationContext() {
		return addCorrelationContext;
	}

	public void setAddCorrelationContext(boolean addCorrelationContext) {
		this.addCorrelationContext = addCorrelationContext;
	}

	public boolean isLoadCorrelationContext() {
		return loadCorrelationContext;
	}

	public void setLoadCorrelationContext(boolean loadCorrelationContext) {
		this.loadCorrelationContext = loadCorrelationContext;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	

}
