package com.catify.core.process;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.model.ProcessDefinition;

public class ProcessHelper {

	public static String getNormalNode(ProcessDefinition definition, String nodeId){
		
		Iterator<String> it = definition.getTransitionsFromNode(nodeId).iterator();
		
		while (it.hasNext()) {
			String id = (String) it.next();
			int type = definition.getNode(id).getNodeType();
			
			if(type != ProcessConstants.TIMEREVENT && type != ProcessConstants.EXCEPTIONEVENT){
				return id;
			}
		}
		
		return null;
	}
	
	public static String getTimerEvent(ProcessDefinition definition, String nodeId){
		
		Iterator<String> it = definition.getTransitionsFromNode(nodeId).iterator();
		
		while (it.hasNext()) {
			String id = (String) it.next();
			int type = definition.getNode(id).getNodeType();
			
			if(type == ProcessConstants.TIMEREVENT){
				return definition.getTransitionsFromNode(id).get(0);
			}
		}
		
		return null;
		
	}
	
	public static List<String> getExceptionEvents(ProcessDefinition definition, String nodeId){
		//TODO implement this
		
		return null;
	}
	
	public static String createProcessId(String accountName, String processName, String processVersion){
		return DigestUtils.md5Hex(String.format("%s%s%s", accountName, processName, processVersion));
	}
	
	public static String createTaskInstanceId(String instanceId, String taskId){
		return DigestUtils.md5Hex(String.format("%s%s", instanceId, taskId));
	}
	
	public static String createTaskId(String processId, String nodeName){
		return DigestUtils.md5Hex(String.format("%s%s", processId, nodeName));
	}
	
}
