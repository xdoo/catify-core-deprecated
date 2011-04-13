package com.catify.core.process.processors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineDeploymentProcessor extends BaseProcessor {

	static final Logger LOG = LoggerFactory.getLogger(PipelineDeploymentProcessor.class);
	
	public void process(Exchange ex) {
		
		InputStream is = new ByteArrayInputStream(ex.getIn().getBody(String.class).getBytes());
		RoutesDefinition routes;
		try {
			routes = ex.getContext().loadRoutesDefinition(is);
			ex.getContext().addRouteDefinitions(routes.getRoutes());
			
			LOG.info("added pipeline to context.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.copyBodyAndHeaders(ex);
		
	}

}
