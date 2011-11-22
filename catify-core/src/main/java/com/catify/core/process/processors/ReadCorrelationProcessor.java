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

import com.catify.core.constants.CacheConstants;
import com.catify.core.constants.MessageConstants;
import com.hazelcast.core.Hazelcast;

/**
 * check correlation cache for the given correlation id. if it's
 * not present, store a {@link com.catify.core.exceptions.CorrelationException} 
 * inside the exchange.
 * 
 * @author claus
 *
 */
public class ReadCorrelationProcessor extends CorrelationProcessor {

	public static final String CORRELATION_EXCEPTION_HEADER = "CorrelationException"; 
	
	@Override
	public void process(Exchange ex) throws Exception {

		super.generateCorrelationId(ex);

		// get instance id from hazelcast
		String instanceId = (String) Hazelcast.getMap(
				CacheConstants.CORRELATION_CACHE).get(
				ex.getOut().getHeader(HazelcastConstants.OBJECT_ID));
		
		if(instanceId == null) {
			ex.getOut().setHeader(CORRELATION_EXCEPTION_HEADER, "yes");
		} else {
			// put it into the message
			ex.getOut().setHeader(MessageConstants.INSTANCE_ID, instanceId);
		}
	}
}
