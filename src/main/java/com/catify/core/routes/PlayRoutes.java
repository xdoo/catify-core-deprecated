package com.catify.core.routes;

import org.apache.camel.builder.RouteBuilder;

import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.QueueConstants;
import com.catify.core.data.marshall.CsvMarshallProcessor;
import com.catify.core.process.processors.InitProcessProcessor;
import com.catify.core.process.processors.ProcessIdProcessor;

public class PlayRoutes extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		from("file:../files?delete=true")
//		from("file:///home/claus/Dropbox/buch/workspace/catify-core/target/file?delete=true")
		.setHeader(MessageConstants.ACCOUNT_NAME, constant("sbk"))
		.setHeader(MessageConstants.PROCESS_NAME, constant("process-01"))
		.setHeader(MessageConstants.PROCESS_VERSION, constant("1.0"))
		.setHeader(MessageConstants.TASK_ID, constant("start"))
		.process(new ProcessIdProcessor())
		.split(body().tokenize("\n")).streaming()
			.process(new CsvMarshallProcessor())
			.process(new InitProcessProcessor())
//			.wireTap("direct:savePayload")
			.to(String.format("activemq:queue:%s", QueueConstants.IN_QUEUE))
		.end();
		
		
		from(String.format("activemq:queue:%s", QueueConstants.OUT_QUEUE))
		.log(String.format("instance id --> ${header.%s}",MessageConstants.INSTANCE_ID));
		
	}

}
