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
import org.apache.camel.component.hazelcast.HazelcastConstants;
import org.apache.commons.codec.digest.DigestUtils;

public abstract class CorrelationProcessor extends BaseProcessor {

	public abstract void process(Exchange ex) throws Exception;
	
	protected void generateCorrelationId(Exchange ex){
		
//		creates a md5 hash as correlation id for the given string
		String correlationId = DigestUtils.md5Hex(ex.getIn().getBody(String.class));
		
		super.copyBodyAndHeaders(ex);
		
		ex.getOut().setHeader(HazelcastConstants.OBJECT_ID, correlationId);
	}

}
