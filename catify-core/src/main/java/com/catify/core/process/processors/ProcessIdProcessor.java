package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.apache.commons.codec.digest.DigestUtils;

import com.catify.core.constants.MessageConstants;

public class ProcessIdProcessor extends BaseProcessor {

	public void process(Exchange exchange) throws Exception {
		
		String accountName = exchange.getIn().getHeader(MessageConstants.ACCOUNT_NAME, String.class);
		String processName = exchange.getIn().getHeader(MessageConstants.PROCESS_NAME, String.class);
		String processVersion = exchange.getIn().getHeader(MessageConstants.PROCESS_VERSION, String.class);
		
		String processId = DigestUtils.md5Hex(String.format("%s%s%s", accountName, processName, processVersion));
		
		super.copyBodyAndHeaders(exchange);
		
		exchange.getOut().setHeader(MessageConstants.PROCESS_ID, processId);

	}

}