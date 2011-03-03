package com.catify.core.rest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelTestSupport;

import com.catify.core.data.marshall.CsvMarshallProcessor;

public class TestCsvMarshaller extends CamelTestSupport {

	public void testCsvMarshalling(){
		
		String body = "44532221;Hans;Tester;München;80809;Schleißheimer Str.;215;33002357;24.12.73;267;Die deutsche Wirtschaft ist im vergangenen Jahr um 3,6 Prozent gewachsen. Dies teilte das Statistische Bundesamt in Wiesbaden nach ersten Berechnungen mit. Das Wachstum war damit so groß wie noch nie seit der Wiedervereinigung Deutschlands. Im Krisenjahr 2009 war das Bruttoinlandsprodukt (BIP) um 4,7 Prozent abgestürzt. Dies war der stärkste Rückgang seit dem Zweiten Weltkrieg. Im bisher besten Jahr im vereinten Deutschland war die Wirtschaft im Boomjahr 2006 um 3,4 Prozent gewachsen.";
		
		template.sendBody("direct:foo", body);
		String result = (String) consumer.receiveBody("seda:out", 5000);
		
		assertEquals("<set><column_0>44532221</column_0><column_1>Hans</column_1><column_2>Tester</column_2><column_3>München</column_3><column_4>80809</column_4><column_5>Schleißheimer Str.</column_5><column_6>215</column_6><column_7>33002357</column_7><column_8>24.12.73</column_8><column_9>267</column_9><column_10>Die deutsche Wirtschaft ist im vergangenen Jahr um 3,6 Prozent gewachsen. Dies teilte das Statistische Bundesamt in Wiesbaden nach ersten Berechnungen mit. Das Wachstum war damit so groß wie noch nie seit der Wiedervereinigung Deutschlands. Im Krisenjahr 2009 war das Bruttoinlandsprodukt (BIP) um 4,7 Prozent abgestürzt. Dies war der stärkste Rückgang seit dem Zweiten Weltkrieg. Im bisher besten Jahr im vereinten Deutschland war die Wirtschaft im Boomjahr 2006 um 3,4 Prozent gewachsen.</column_10></set>", result);
		
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				
				from("direct:foo")
				.process(new CsvMarshallProcessor())
				.log("${body}")
				.to("seda:out");
				
				
			}
		};
	}
	
}
