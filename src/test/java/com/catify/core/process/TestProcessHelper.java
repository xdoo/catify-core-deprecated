package com.catify.core.process;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.CamelTestSupport;

import com.catify.core.process.model.ProcessDefinition;
import com.catify.core.process.xml.XmlProcessBuilder;
import com.catify.core.process.xml.model.Process;

public class TestProcessHelper extends CamelTestSupport {

	
	public void testGetNormalNode() {
		String nodeId = ProcessHelper.getNormalNode(getProcessDefinition(), "7fdce0391f138ba3913c4c2cd6d8795b");
		
		assertNotNull(nodeId);
		assertEquals("5e98c5e23510afc3d405f0ec2017abd4", nodeId);
	}

	public void testGetTimerEvent() {
		String eventId = ProcessHelper.getTimerEvent(getProcessDefinition(), "7fdce0391f138ba3913c4c2cd6d8795b");
		
		System.out.println(getProcessDefinition().nodesToString());
		
		assertNotNull(eventId);
		assertEquals("316e7035b7b2a5e5a3d0095cd4136902", eventId);
	}
	
	private ProcessDefinition getProcessDefinition(){
		Process process = template.requestBody("direct:xml", this.xml(), Process.class);
		return new XmlProcessBuilder().build(process);
	}
	
	@Override
	protected RouteBuilder createRouteBuilder(){
		
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				DataFormat jaxb = new JaxbDataFormat("com.catify.core.process.xml.model");
				
				from("direct:xml")
				.unmarshal(jaxb);
				
				
			}
		};
	}
	
	private String xml(){
		return 	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<process processVersion=\"1.0\" processName=\"process03\" accountName=\"tester\"  xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >\n" +
				"	<start ns:name=\"start\"/>\n" +
				"	<request ns:name=\"bam_step_01\"/>\n" +
				"	<receive ns:name=\"wait_for_payload\">\n" +
				"		<timeEvent ns:time=\"1000\">\n" +
				"			<request ns:name=\"throw_time_out_exception\"/>\n" +
				"			<end ns:name=\"end_time_out\"/>\n" +
				"		</timeEvent>\n" +
				"	</receive>\n" +
				"	<request ns:name=\"bam_step_02\"/>\n" +
				"	<end ns:name=\"end\"/>\n" +
				"</process>";
	}

}
