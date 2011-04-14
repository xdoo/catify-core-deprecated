package com.catify.core.event.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Header;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.EventConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.event.TimerEventService;
import com.catify.core.event.impl.beans.TimerEvent;
import com.catify.core.process.ProcessHelper;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.query.SqlPredicate;

public class HazelcastTimerEventService implements TimerEventService {

	private IMap<String,TimerEvent> cache = Hazelcast.getMap(CacheConstants.TIMER_CACHE);
	
	@Override
	public void register(	@Header(EventConstants.EVENT_TIME) long eventTime,
							@Header(MessageConstants.INSTANCE_ID) String instanceId,
							@Header(MessageConstants.TASK_ID) String taskId) {
		long time  = new Date().getTime() + eventTime;
		this.cache.put(ProcessHelper.createTaskInstanceId(instanceId, taskId), new TimerEvent(time, instanceId, taskId));
	}

	@Override
	public void unregister(	@Header(MessageConstants.INSTANCE_ID) String instanceId,
							@Header(MessageConstants.TASK_ID) String taskId) {
	
		this.cache.remove(ProcessHelper.createTaskInstanceId(instanceId, taskId));
	}

	@Override
	public List<List<String>> fire(@Header(Exchange.TIMER_FIRED_TIME) Date dateTime) {
		List<List<String>> result = new ArrayList<List<String>>();
		
		Iterator<Object> it = this.cache.values(new SqlPredicate(String.format("time = %s", dateTime.getTime()))).iterator();
		while (it.hasNext()) {
			TimerEvent event = (TimerEvent) it.next();
			result.add(this.marshal(event));
		}
		return result;
	}
	
	private List<String> marshal(TimerEvent event){
		List<String> result = new ArrayList<String>();
		
		result.add(event.getInstanceId()); 	//index 0
		result.add(event.getTaskId());		//index 1
		
		return result;
	}

}
