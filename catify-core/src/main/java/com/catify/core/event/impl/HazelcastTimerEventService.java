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
							@Header(MessageConstants.TASK_ID) String taskId,
							@Header(MessageConstants.ACCOUNT_NAME) String account,
							@Header(MessageConstants.PROCESS_NAME) String process,
							@Header(MessageConstants.PROCESS_VERSION) String version,
							@Header(MessageConstants.TASK_NAME) String nodename) {
		long time  = new Date().getTime() + eventTime;
		TimerEvent event = new TimerEvent(time, instanceId, taskId, account, process, version, nodename);
		//TODO delete
		System.out.println(String.format("register timer event for --> %s", event.toString()));
		this.cache.put(ProcessHelper.createTaskInstanceId(instanceId, taskId), event);
	}

	@Override
	public void unregister(	@Header(MessageConstants.INSTANCE_ID) String instanceId,
							@Header(MessageConstants.TASK_ID) String taskId) {
	
		this.cache.remove(ProcessHelper.createTaskInstanceId(instanceId, taskId));
	}

	@Override
	public List<List<String>> fire(@Header(Exchange.TIMER_FIRED_TIME) Date dateTime) {
		List<List<String>> result = new ArrayList<List<String>>();
		
		System.out.println("FIRE ------------------------------------> ");
		
		Iterator<TimerEvent> it = this.cache.values(new SqlPredicate(String.format("time <= %s", dateTime.getTime()))).iterator();
		while (it.hasNext()) {
			TimerEvent event = it.next();
			
			//TODO delete
			System.out.println(event.toString());
			result.add(this.marshal(event));
			this.cache.remove(ProcessHelper.createTaskInstanceId(event.getInstanceId(), event.getTaskId()));
		}
		return result;
	}
	
	private List<String> marshal(TimerEvent event){
		List<String> result = new ArrayList<String>();
		
		result.add(event.getInstanceId()); 	//index 0
		result.add(event.getTaskId());		//index 1
		result.add(event.getAccount());		//index 2
		result.add(event.getProcess());		//index 3
		result.add(event.getVersion());		//index 4
		result.add(event.getNodename());	//index 5
		
		return result;
	}

}
