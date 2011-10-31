package com.catify.core.adapter.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.HazelcastConstants;

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.QueueConstants;
import com.catify.core.model.pipeline.PipelineDefinition;

public class PipelineBuilder {
	
	private final static String SETCONTEXT 		= "setcontext";
	private final static String GETCONTEXT 		= "getcontext";
	private final static String TRANSFORM 		= "transform";
	private final static String VALIDATE 		= "validate";
	private final static String SAVEPAYLOAD		= "savepayload";
	private final static String LOADPAYLOAD		= "loadpayload";
	private final static String STARTPIPELINE 	= "startpipeline";
	private final static String REQUESTPIPELINE	= "requestpipeline";
	private final static String RECEIVEPIPELINE	= "receivepipeline";
	private final static String REPLYPIPELINE	= "replypipeline";
	
	private final static String ROUTING			= "pipelinerouting";
	
	public void createStartPipeline(CamelContext context, final PipelineDefinition definition) {
		
		try {
			
			final String routingSlip = this.createRoutingSlip(definition, definition.getProcessId(), context, false, true);
			
			context.addRoutes(new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {
					from(String.format("seda:%s-%s", STARTPIPELINE, definition.getProcessId()))
					.routeId(String.format("%s-%s", STARTPIPELINE, definition.getProcessId()))
					.setHeader(ROUTING, constant(routingSlip))
					.routingSlip(ROUTING)
					.to(String.format("hazelcast:%s%s", HazelcastConstants.SEDA_PREFIX, QueueConstants.IN_QUEUE));
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createRequestPipeline(CamelContext context, String taskId){
		
		try {
			
			
			
			context.addRoutes(new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {
					// TODO Auto-generated method stub
					
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void createReceivePipeline(CamelContext context, String taskId){

		try {
			context.addRoutes(new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {
					// TODO Auto-generated method stub
					
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void createReplyPipeline(CamelContext context, String taskId){
		
		try {
			context.addRoutes(new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {
					// TODO Auto-generated method stub
					
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}
	
	private String createRoutingSlip(PipelineDefinition definition, String id, CamelContext context, boolean loadPayload, boolean savePayload) throws Exception{
		
		StringBuilder routingslip = new StringBuilder();
		
		if(loadPayload){
			this.loadPayload(context, id);
			routingslip.append(String.format("direct:%s-%s", LOADPAYLOAD, id)).append(",");
		}
		
		if(definition.isValidate()){
			this.addValidation(context, id);
			routingslip.append(",").append(String.format("direct:%s-%s", VALIDATE, id));
		}
		
		if(savePayload){
			this.savePayload(context, id);
			routingslip.append(",").append(String.format("direct:%s-%s", SAVEPAYLOAD, id));
		}
		
		if(definition.isAddCorrelationContext()){
			this.getCorrelationContext(context, id);
			routingslip.append(",").append(String.format("direct:%s-%s", SETCONTEXT, id));
		}
		
		if(definition.isLoadCorrelationContext()){
			this.getCorrelationContext(context, id);
			routingslip.append(",").append(String.format("direct:%s-%s", GETCONTEXT, id));
		}
		
		return routingslip.toString();
	}
	
	/**
	 * 
	 * 
	 * @param context
	 * @param id
	 * @throws Exception
	 */
	private void addValidation(CamelContext context, final String id) throws Exception{
		
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from(String.format("direct:%s-%s", VALIDATE, id))
				.routeId(String.format("validate-%s", id))
				.to(String.format("msv:http://localhost:9080/rest/%s/%s", CacheConstants.VALIDATION_CACHE, id));
				
			}
		});	
	}
	
	/**
	 * 
	 * 
	 * @param context
	 * @param id
	 * @throws Exception
	 */
	private void setCorrelationContext(CamelContext context, final String id) throws Exception{
		
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from(String.format("direct:%s-%s", SETCONTEXT, id))
				.routeId(String.format("setcontext-%s", id))
				.to(String.format("xslt:http://localhost:9080/rest/%s/%s", CacheConstants.CORRELATION_CACHE, id))
				.setHeader(HazelcastConstants.OBJECT_ID, simple("${body}"))
				.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
				.setBody(simple(String.format("${header.%s}", MessageConstants.INSTANCE_ID)))
				.to(String.format("hazelcast:%s%s", 
						HazelcastConstants.MAP_PREFIX,
						CacheConstants.CONTEXT_CACHE
						));				
			}
		});	
	}
	
	/**
	 * 
	 * 
	 * @param context
	 * @param id
	 * @throws Exception
	 */
	private void getCorrelationContext(CamelContext context, final String id) throws Exception{
		
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from(String.format("direct:%s-%s", GETCONTEXT, id))
				.routeId(String.format("%s-%s", GETCONTEXT, id))
				.to(String.format("xslt:http://localhost:9080/rest/%s/%s", CacheConstants.CORRELATION_CACHE, id))
				.setHeader(HazelcastConstants.OBJECT_ID, simple("${body}"))
				.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.GET_OPERATION))
				.to(String.format("hazelcast:%s%s", 
						HazelcastConstants.MAP_PREFIX,
						CacheConstants.CONTEXT_CACHE))
				.setHeader(MessageConstants.INSTANCE_ID, simple("${body}"));
			}
		});
	}
	
	/**
	 * 
	 * 
	 * @param context
	 * @param id
	 * @throws Exception
	 */
	private void savePayload(CamelContext context, final String id) throws Exception{
		
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from(String.format("direct:%s-%s", SAVEPAYLOAD, id))
				.routeId(String.format("%s-%s", SAVEPAYLOAD, id))
				.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}", MessageConstants.INSTANCE_ID)))
				.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION))
				.to(String.format("hazelcast:%s%s", 
						HazelcastConstants.MAP_PREFIX,
						CacheConstants.PAYLOAD_CACHE
						));
				
			}
		});
	}
	
	/**
	 * 
	 * 
	 * @param context
	 * @param id
	 * @throws Exception
	 */
	private void loadPayload(CamelContext context, final String id) throws Exception{
		
		context.addRoutes(new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from(String.format("direct:%s-%s", LOADPAYLOAD, id))
				.routeId(String.format("%s-%s", LOADPAYLOAD, id))
				.setHeader(HazelcastConstants.OBJECT_ID, simple(String.format("${header.%s}", MessageConstants.INSTANCE_ID)))
				.setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.GET_OPERATION))
				.to(String.format("hazelcast:%s%s", 
						HazelcastConstants.MAP_PREFIX,
						CacheConstants.PAYLOAD_CACHE
						));
				
			}
		});
	}
	
}
