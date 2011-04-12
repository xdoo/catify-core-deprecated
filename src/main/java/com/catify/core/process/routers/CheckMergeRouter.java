package com.catify.core.process.routers;

import org.apache.camel.Header;
import org.apache.camel.Message;

import com.catify.core.constants.MessageConstants;
import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;

public class CheckMergeRouter {
	
	public String route(Message message, 
						@Header(MessageConstants.TASK_INSTANCE_ID) String taskInstanceId,
						@Header(MessageConstants.AWAITED_HITS) int awaitedHits,
						@Header(MessageConstants.TASK_ID) String taskId){
		
		String result = null;
		
		AtomicNumber number = Hazelcast.getAtomicNumber(taskInstanceId);
		
		//set number plus one
		long hits = number.incrementAndGet();
		
		//if all awaited lines have been finished
		//go to next node - otherwise stay in wait 
		//state...
		if(hits == awaitedHits){
			return String.format("direct:cleannode-%s", taskId);
		}
		
//		TODO --> 	resolve the problem, that tons of numbers will stay in cache. we have
//					to delete them without loosing the information, that all hits have been
//					made on the merge node...
		
		return result;
	}

}
