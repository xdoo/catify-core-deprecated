package com.catify.core.process.routers;

public interface DecisionRouter {

	public String route(String payload) throws Exception;
	
}
