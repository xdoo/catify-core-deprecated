package com.catify.core.process.processors;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.model.RoutesDefinition;

public class PipelineDeploymentProcessor extends BaseProcessor {

	public void process(Exchange ex) throws Exception {
		
		InputStream is = new ByteArrayInputStream(ex.getIn().getBody(String.class).getBytes());
		RoutesDefinition routes = ex.getContext().loadRoutesDefinition(is);
		ex.getContext().addRouteDefinitions(routes.getRoutes());
		
		super.copyBodyAndHeaders(ex);
		
	}

}
