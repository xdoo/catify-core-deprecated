package com.catify.core.process.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import com.catify.core.process.xml.CorrelationRuleBuilder;

public class TestCorrelationRuleBuilder extends CamelTestSupport {

	@Test
	public void testCreationWithOneXPath(){
		CorrelationRuleBuilder builder = new CorrelationRuleBuilder();
		String definition = builder.buildCorrelationDefinition(this.createXPath(1));	
		assertTrue(definition.contains("concat(string($var1_instance/foo/0))"));
	}
	
	@Test
	public void testCreationWithMoreThanOneXPath(){
		CorrelationRuleBuilder builder = new CorrelationRuleBuilder();
		String definition = builder.buildCorrelationDefinition(this.createXPath(5));
		assertTrue(definition.contains("concat(string($var1_instance/foo/0), string($var1_instance/foo/1), string($var1_instance/foo/2), string($var1_instance/foo/3), string($var1_instance/foo/4))"));
	}
	
	private List<String> createXPath(int number){
		List<String> xpath = new ArrayList<String>();
		
		for (int i = 0; i < number; i++) {
			xpath.add(String.format("/foo/%s", i));
		}
		
		return xpath;
	}
	
}
