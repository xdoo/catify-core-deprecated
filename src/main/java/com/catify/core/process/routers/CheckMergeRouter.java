package com.catify.core.process.routers;

import org.apache.camel.Message;

import com.catify.core.constants.MessageConstants;
import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;

public class CheckMergeRouter {
	
	public String route(Message message){
		
		String result = null;
		
		AtomicNumber number = Hazelcast.getAtomicNumber(message.getHeader(MessageConstants.TASK_INSTANCE_ID, String.class));
		
		//set number plus one
		long hits = number.incrementAndGet();
		
		//if all awaited lines have been finished
		//go to next node - otherwise stay in wait 
		//state...
		if(hits == message.getHeader(MessageConstants.AWAITED_HITS, Integer.class)){
			return String.format("direct:cleannode-%s", message.getHeader(MessageConstants.TASK_ID, String.class));
		}
		
		return result;
	}

}
