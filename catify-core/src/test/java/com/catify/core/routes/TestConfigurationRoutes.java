package com.catify.core.routes;

import org.junit.Test;

import com.catify.core.process.ProcessHelper;
import com.catify.core.testsupport.SpringTestBase;

public class TestConfigurationRoutes extends SpringTestBase {

	@Test
	public void testXsltDeploymentWithTask() throws InterruptedException{
		
		String pid = ProcessHelper.createProcessId("CATIFY", "process" , "1.0");
		String tid = ProcessHelper.createTaskId(pid, "foo");
		
		template.sendBody("restlet:http://localhost:9080/catify/deploy_transformation/CATIFY/process/1.0/foo?restletMethod=post", this.getTransformation());
		
		Thread.sleep(3000);
		
		String xslt = template.requestBody("restlet:http://localhost:9080/catify/get_transformation/"+tid+"?restletMethod=get", "foo", String.class);
		assertNotNull(xslt);
		assertTrue(xslt.contains("<xsl:template match=\"*\">"));
	}
	
	@Test
	public void testXsltDeploymentWithProcess() throws InterruptedException{
		
		String pid = ProcessHelper.createProcessId("CATIFY", "process" , "1.0");
		
		template.sendBody("restlet:http://localhost:9080/catify/deploy_transformation/CATIFY/process/1.0?restletMethod=post", this.getTransformation());
		
		Thread.sleep(3000);
		
		String xslt = template.requestBody("restlet:http://localhost:9080/catify/get_transformation/"+pid+"?restletMethod=get", "foo", String.class);
		assertNotNull(xslt);
		assertTrue(xslt.contains("<xsl:template match=\"*\">"));
		
	}
	
}
