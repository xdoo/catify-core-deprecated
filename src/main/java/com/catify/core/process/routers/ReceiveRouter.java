package com.catify.core.process.routers;

import org.apache.camel.Message;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class ReceiveRouter {

	private String goRoute;
	private String waitRoute;
	public static final String WAIT = "catify.state.wait";

	public ReceiveRouter(String taskId){
		String.format("direct:wait-%s", taskId);
		this.goRoute	= String.format("direct:go-%s", taskId);
		this.waitRoute 	= String.format("direct:wait-%s", taskId);
	}
	
	public String route(Message message){
		
		String iid 	= (String) message.getHeader(MessageConstants.TASK_INSTANCE_ID);
		IMap<String, Integer> map = Hazelcast.getMap(CacheConstants.NODE_CACHE);
		
		if(message.getHeaders().containsKey(WAIT)){
			message.getHeaders().remove(WAIT);
			return null;
		}
		
		if(map.containsKey(iid)){
			int state = map.get(iid);
			
			if(state == ProcessConstants.STATE_WAITING){
				map.put(iid, ProcessConstants.STATE_DONE);
				return goRoute;
			}
		} else {
			map.put(iid, ProcessConstants.STATE_WAITING);
			return waitRoute;
		}
		
		return null;
	}
	
}
