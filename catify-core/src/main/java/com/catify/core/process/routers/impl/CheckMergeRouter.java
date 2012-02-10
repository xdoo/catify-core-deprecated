package com.catify.core.process.routers.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.Message;

import com.catify.core.constants.MessageConstants;
import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;

public class CheckMergeRouter {
	
	public String route(Message message, 
						@Header(MessageConstants.TASK_INSTANCE_ID) String taskInstanceId,
						@Header(MessageConstants.AWAITED_HITS) int awaitedHits,
						@Header(MessageConstants.TASK_ID) String taskId,
						@Header(MessageConstants.INSTANCE_ID) String instanceId,
						@Header(MessageConstants.PROCESS_NAME) String processName,
						@Header(MessageConstants.PRECEDING_NODES) List<String> precedingNodes,
						CamelContext context){
		
		String result = null;
		
		AtomicNumber number = Hazelcast.getAtomicNumber(taskInstanceId);
		
		//set number plus one
		long hits = number.incrementAndGet();
		
		//if all awaited lines have been finished
		//go to next node - otherwise stay in wait 
		//state...
		if(hits == awaitedHits){
			this.setState(precedingNodes, instanceId, processName, context);
			return String.format("direct:cleannode-%s", taskId);
		}
		
//		TODO --> 	resolve the problem, that tons of numbers will stay in cache. we have
//					to delete them without loosing the information, that all hits have been
//					made on the merge node...
		
		return result;
	}
	
	private void setState(List<String> precedingNodes, String instanceId, String processName, CamelContext context){
		
		Iterator<String> it = precedingNodes.iterator();
		
		while (it.hasNext()) {	
			
			//set all non idempotent nodes to 'done' state
			context.createProducerTemplate().sendBodyAndHeaders("direct:done", "", this.getHeaders(instanceId, it.next(), processName));			
		}
		
	}
	
	private Map<String,Object> getHeaders(String instanceId, String taskId, String processName){
		Map<String,Object> headers = new HashMap<String, Object>();
		
		headers.put(MessageConstants.INSTANCE_ID, instanceId);
		headers.put(MessageConstants.TASK_ID, taskId);
		headers.put(MessageConstants.PROCESS_NAME, processName);
		
		return headers;
	}
	
}
