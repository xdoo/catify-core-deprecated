package com.catify.core.event;

import java.util.List;

import org.apache.camel.Message;

public interface TimerEventService {

	public void register(Message message);
	public void unregister(Message message);
	public List<List<String>> fire(Message message);
	
}
