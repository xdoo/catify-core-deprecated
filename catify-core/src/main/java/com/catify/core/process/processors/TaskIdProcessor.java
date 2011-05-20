package com.catify.core.process.processors;

import org.apache.camel.Exchange;

import com.catify.core.constants.MessageConstants;
import com.catify.core.process.ProcessHelper;

public class TaskIdProcessor extends BaseProcessor {

	@Override
	public void process(Exchange ex) throws Exception {
		
		String pid = ex.getIn().getHeader(MessageConstants.PROCESS_ID, String.class);
		String name = ex.getIn().getHeader(MessageConstants.TASK_NAME, String.class);
		
		String taskId = ProcessHelper.createTaskId(pid, name);
		
		super.copyBodyAndHeaders(ex);
		
		ex.getOut().setHeader(MessageConstants.TASK_ID, taskId);
	}

}
