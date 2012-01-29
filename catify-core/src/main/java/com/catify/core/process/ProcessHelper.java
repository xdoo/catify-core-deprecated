package com.catify.core.process;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.catify.core.constants.ProcessConstants;
import com.catify.core.process.model.ProcessDefinition;

public class ProcessHelper {
	
	/**
	 * Separates the first level nodes like request, receive, sleep etc. 
	 * from second level nodes like timer event.
	 * 
	 * @param definition
	 * @param nodeId
	 * @return
	 */
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
	
	/**
	 * Filter a process definition for all timer events.
	 * 
	 * @param definition
	 * @param nodeId
	 * @return
	 */
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
	
	/**
	 * Generates a process id as md5Hex string.
	 * 
	 * @param accountName
	 * @param processName
	 * @param processVersion
	 * @return
	 */
	public static String createProcessId(String accountName, String processName, String processVersion){
		return DigestUtils.md5Hex(String.format("%s%s%s", accountName, processName, processVersion));
	}
	
	/**
	 * Generates a node instance id (task instance id) as md5Hex string.
	 * A node instance id is a unique id for every node inside single
	 * process instance - it is never used twice.
	 * 
	 * @param instanceId
	 * @param taskId
	 * @return
	 */
	public static String createTaskInstanceId(String instanceId, String taskId){
		return DigestUtils.md5Hex(String.format("%s%s", instanceId, taskId));
	}
	
	/**
	 * Generates a node id (task id) as md5Hex string. It is
	 * a unique identifier for a node within the complete
	 * system.
	 * 
	 * @param processId
	 * @param nodeName
	 * @return
	 */
	public static String createTaskId(String processId, String nodeName){
		return DigestUtils.md5Hex(String.format("%s%s", processId, nodeName));
	}
	
}
