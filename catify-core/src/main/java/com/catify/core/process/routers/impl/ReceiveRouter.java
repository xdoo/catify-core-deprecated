package com.catify.core.process.routers.impl;

import org.apache.camel.Header;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;
import com.catify.core.event.impl.beans.StateEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class ReceiveRouter {

	static final Logger LOG = LoggerFactory.getLogger(ReceiveRouter.class);
	
	private String goRoute;
	private String waitRoute;
	public static final String WAIT = "catify.state.wait";
	
	private IMap<String, StateEvent> map;

	public ReceiveRouter(String taskId){
		String.format("direct:wait-%s", taskId);
		this.goRoute	= String.format("direct:go-%s", taskId);
		this.waitRoute 	= String.format("direct:wait-%s", taskId);
		this.map		= Hazelcast.getMap(CacheConstants.NODE_CACHE);
	}
	
	public String route(Message message,
						@Header(MessageConstants.TASK_ID) String taskId,
						@Header(MessageConstants.INSTANCE_ID) String instanceId){
		
		
		if(LOG.isDebugEnabled()){
			LOG.debug(String.format("task id --> %s    instance id --> %s", taskId, instanceId));
		}
		
		String key = String.format("%s-%s", instanceId, taskId);
		
		if(message.getHeaders().containsKey(WAIT)){
			message.getHeaders().remove(WAIT);
			
			if(LOG.isDebugEnabled()){
				LOG.info("wait header removed...");
			}
			
			return null;
		}
		
		//if the map contains the key, the node has been initialized
		if(map.containsKey(key)){
			int state = map.get(key).getState();
			
			
			if(LOG.isDebugEnabled()){
				LOG.info(String.format("actual state --> %s", state));
			}
			
			LOG.info(String.format("actual state --> %s", state));
			
			//check if the node is waiting for a message
			if(state == ProcessConstants.STATE_WAITING){
				map.put(key, new StateEvent(message.getHeader(MessageConstants.INSTANCE_ID, String.class), ProcessConstants.STATE_DONE, key));
				
				if(LOG.isDebugEnabled()){
					LOG.info(String.format("returning go route for '%s' / '%s' --> %s", instanceId, taskId, goRoute));
				}
				
				
				return goRoute;
			}
			
			//check if we have to go to the waiting mode
			if(state == ProcessConstants.STATE_WORKING){
				
				if(LOG.isDebugEnabled()){
					LOG.info(String.format("returning wait route for '%s' / '%s' --> %s", instanceId, taskId, waitRoute));
				}
				
				return waitRoute;
				
			}
			
		}
		
		return null;
	}
	
}
