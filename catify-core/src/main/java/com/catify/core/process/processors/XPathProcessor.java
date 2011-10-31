/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.catify.core.process.processors;

import org.apache.camel.Exchange;
import org.apache.camel.builder.xml.XPathBuilder;

public class XPathProcessor extends BaseProcessor {

	public static final String XPATH = "variableXpath";

	@Override
	public void process(Exchange ex) throws Exception {
		
		String xpath = ex.getIn().getHeader(XPATH, String.class);
		String body = ex.getIn().getBody(String.class);
		
		String result = XPathBuilder.xpath(xpath).evaluate(ex.getContext(), body, String.class);	
		
		ex.getOut().setBody(result);
		super.copyHeaders(ex);
		
	}

}
