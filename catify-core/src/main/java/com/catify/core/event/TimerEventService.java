package com.catify.core.event;

import java.util.Date;
import java.util.List;

public interface TimerEventService {

	public void register(	long eventTime, String instanceId, String taskId);
	public void unregister(String instanceId, String taskId);
	public List<List<String>> fire(Date dateTime);
	
}
