package com.catify.core.process;

import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

import com.catify.core.testsupport.SpringTestBase;

/**
 * Tests if the fork node works correct.
 * 
 * @author claus
 * 
 */
public class TestForkNode extends SpringTestBase {

	@EndpointInject(uri = "mock:out")
	private MockEndpoint out;

	@EndpointInject(uri = "mock:out_line1")
	private MockEndpoint outLine1;

	@EndpointInject(uri = "mock:out_line2")
	private MockEndpoint outLine2;

	@EndpointInject(uri = "mock:out_line3")
	private MockEndpoint outLine3;
	
	@EndpointInject(uri = "mock://checkout")
	private MockEndpoint checkout;

	@Test
	public void testReceiveAllLines() throws Exception {

		this.deployProcess(-1, "fork01");
		this.createOutRoute("mock://out");

		Thread.sleep(3000);

		outLine1.setExpectedMessageCount(1);
		outLine2.setExpectedMessageCount(1);
		outLine3.setExpectedMessageCount(1);
		out.setExpectedMessageCount(1);

		// init process
		template.sendBody("seda:start", this.getXml());
		Thread.sleep(1000);

		// send to lines
		template.sendBody("seda:in_line1", this.getXml());
		template.sendBody("seda:in_line2", this.getXml());
		template.sendBody("seda:in_line3", this.getXml());

		assertMockEndpointsSatisfied(10, TimeUnit.SECONDS);

	}

	@Test
	public void testReceiveTwoLines() throws Exception {

		this.deployProcess(2, "fork01");
		this.createOutRoute("mock://out");

		Thread.sleep(3000);

		outLine1.setExpectedMessageCount(1);
		outLine2.setExpectedMessageCount(0);
		outLine3.setExpectedMessageCount(1);
		out.setExpectedMessageCount(1);

		// init process
		template.sendBody("seda:start", this.getXml());
		Thread.sleep(1000);

		// send to lines (1 and 3)
		template.sendBody("seda:in_line1", this.getXml());
		template.sendBody("seda:in_line3", this.getXml());

		// check, what happens if we hit the 2. one after finishing the others
		Thread.sleep(2000);
		template.sendBody("seda:in_line2", this.getXml());

		Thread.sleep(2000);

		assertMockEndpointsSatisfied(15, TimeUnit.SECONDS);

	}

	/**
	 * Tests, if a fork node can be deployed without a name - which should be
	 * possible regarding to the XML schema.
	 */
	@Test
	public void testDeployForkWithoutName() {
		this.deployProcess(2, null);
	}

	@Test public void testReInitProcess() throws Exception {
		this.deployProcess(1, "fork01");
		this.createOutRoute("seda://start");
		
		outLine3.setExpectedMessageCount(1);
		outLine1.setExpectedMessageCount(1);
		outLine2.setExpectedMessageCount(0);
		checkout.setExpectedMessageCount(2);
		
		// init process
		template.sendBody("seda://start", this.getXml());
		Thread.sleep(1000);
		
		System.out.println("-------------------------------------------------------------------> send in line 3");
		
		template.sendBody("seda:in_line3", this.getXml());
		Thread.sleep(1000);
		
		System.out.println("-------------------------------------------------------------------> send in line 1");
		
		template.sendBody("seda:in_line1", this.getXml());
		Thread.sleep(1000);
		
		assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
	}

	private String getProcess(int awaitedHits, String name) {

		if (name != null) {
			name = String.format("ns:name=\"%s\"", name);
		} else {
			name = "";
		}

		return " <process processVersion=\"1.0\" processName=\"process01\" accountName=\"CATIFY\" xmlns=\"http://www.catify.com/api/1.0\" xmlns:ns=\"http://www.catify.com/api/1.0\" >"
				+ "<start ns:name=\"start\">\n"
				+ "	<inPipeline>\n"
				+ "		<endpoint ns:uri=\"seda:start\"/>\n"
				+ "		<correlation>\n"
				+ "			<xpath>/foo/a</xpath>\n"
				+ "			<xpath>/foo/b</xpath>\n"
				+ "		</correlation>\n"
				+ "	</inPipeline>\n"
				+ "</start>\n"
				+ "<fork "
				+ name
				+ " ns:receivingLines=\""
				+ awaitedHits
				+ "\">\n"
				+ "	<line ns:name=\"line1\">\n"
				+ "		<receive ns:name=\"wait_line1\">\n"
				+ "			<timeEvent ns:time=\"30000\">\n"
				+ "				<end ns:name=\"end_time_out1\"/>\n"
				+ "			</timeEvent>\n"
				+ "			<inPipeline>\n"
				+ "				<endpoint ns:uri=\"seda:in_line1\"/>\n"
				+ "				<correlation>\n"
				+ "					<xpath>/foo/a</xpath>\n"
				+ "					<xpath>/foo/b</xpath>\n"
				+ "				</correlation>\n"
				+ "			</inPipeline>\n"
				+ "		</receive>\n"
				+ "		<request ns:name=\"write_line1\">\n"
				+ "			<outPipeline>\n"
				+ "				<endpoint ns:uri=\"mock:out_line1\"/>\n"
				+ "			</outPipeline>\n"
				+ "		</request>\n"
				+ "	</line>\n"
				+ "	<line ns:name=\"line2\">\n"
				+ "		<receive ns:name=\"wait_line2\">\n"
				+ "			<timeEvent ns:time=\"30000\">\n"
				+ "				<end ns:name=\"end_time_out2\"/>\n"
				+ "			</timeEvent>\n"
				+ "			<inPipeline>\n"
				+ "				<endpoint ns:uri=\"seda:in_line2\"/>\n"
				+ "				<correlation>\n"
				+ "					<xpath>/foo/a</xpath>\n"
				+ "					<xpath>/foo/b</xpath>\n"
				+ "				</correlation>\n"
				+ "			</inPipeline>\n"
				+ "		</receive>\n"
				+ "		<request ns:name=\"write_line2\">\n"
				+ "			<outPipeline>\n"
				+ "				<endpoint ns:uri=\"mock:out_line2\"/>\n"
				+ "			</outPipeline>\n"
				+ "		</request>\n"
				+ "	</line>\n"
				+ "	<line ns:name=\"line3\">\n"
				+ "		<receive ns:name=\"wait_line3\">\n"
				+ "			<timeEvent ns:time=\"30000\">\n"
				+ "				<end ns:name=\"end_time_out2\"/>\n"
				+ "			</timeEvent>\n"
				+ "			<inPipeline>\n"
				+ "				<endpoint ns:uri=\"seda:in_line3\"/>\n"
				+ "				<correlation>\n"
				+ "					<xpath>/foo/a</xpath>\n"
				+ "					<xpath>/foo/b</xpath>\n"
				+ "				</correlation>\n"
				+ "			</inPipeline>\n"
				+ "		</receive>\n"
				+ "		<request ns:name=\"write_line3\">\n"
				+ "			<outPipeline>\n"
				+ "				<endpoint ns:uri=\"mock:out_line3\"/>\n"
				+ "			</outPipeline>\n"
				+ "		</request>\n"
				+ "	</line>\n"
				+ "	</fork>\n"
				+ "	<request ns:name=\"check_out\">\n"
				+ "		<outPipeline>\n"
				+ "			<endpoint ns:uri=\"mock://checkout\"/>\n"
				+ "		</outPipeline>\n"
				+ "	</request>\n"
				+ "	<request ns:name=\"final_out\">\n"
				+ "		<outPipeline>\n"
				+ "			<endpoint ns:uri=\"seda:finalOut\"/>\n"
				+ "		</outPipeline>\n"
				+ "	</request>\n"
				+ "	<end ns:name=\"end\"/>" + "</process>";
	}

	private void createOutRoute(final String sink) throws Exception {
		RouteBuilder routeBuilder = new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				from("seda://finalOut")
				.to("log:PROCESS_END?showAll=true")
				.to(sink);	
			}
		};
		
		this.context().addRoutes(routeBuilder);
	}

	private void deployProcess(int awaitedHits, String name) {
		super.deployProcess(this.getProcess(awaitedHits, name));
	}

}
