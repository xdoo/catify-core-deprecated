package com.catify.core.process.routers;

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
	
	public String route(Message message){
		
		String iid 	= (String) message.getHeader(MessageConstants.TASK_INSTANCE_ID);
		
		if(LOG.isDebugEnabled()){
			LOG.debug(String.format("task instance id --> %s", iid));
		}
		
		if(message.getHeaders().containsKey(WAIT)){
			message.getHeaders().remove(WAIT);
			
			if(LOG.isDebugEnabled()){
				LOG.info("wait header removed...");
			}
			
			return null;
		}
		
		if(map.containsKey(iid)){
			int state = map.get(iid).getState();
			
			if(LOG.isDebugEnabled()){
				LOG.info(String.format("actual state --> %s", state));
			}
			
			if(state == ProcessConstants.STATE_WAITING){
				map.put(iid, new StateEvent(message.getHeader(MessageConstants.INSTANCE_ID, String.class), ProcessConstants.STATE_DONE));
				
				if(LOG.isDebugEnabled()){
					LOG.info(String.format("returning go route for '%s' --> %s", iid, goRoute));
				}
				
				return goRoute;
			}
		} else {
			map.put(iid, new StateEvent(message.getHeader(MessageConstants.INSTANCE_ID, String.class), ProcessConstants.STATE_WAITING));
			
			if(LOG.isDebugEnabled()){
				LOG.info(String.format("returning wait route for '%s' --> %s", iid, waitRoute));
			}
			
			return waitRoute;
		}
		
		return null;
	}
	
}
