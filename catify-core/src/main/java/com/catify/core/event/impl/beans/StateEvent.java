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
package com.catify.core.event.impl.beans;

import java.io.Serializable;

import org.apache.camel.Handler;
import org.apache.camel.Header;

import com.catify.core.constants.MessageConstants;
import com.catify.core.constants.ProcessConstants;

public class StateEvent implements Serializable {

	private static final long serialVersionUID = -5918833981928700394L;
	
	// we need this id for persistence
	private String id;
	private String instanceId;
	private int state;

	public StateEvent(){}
	
	public StateEvent(String instanceId, int state){
		this.instanceId = instanceId;
		this.state = state;
	}
	
	@Handler
	public StateEvent proxy(	@Header(MessageConstants.INSTANCE_ID) String instanceId,
								@Header(ProcessConstants.STATE) int state){
		
		this.instanceId = instanceId;
		this.state 		= state;
		
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
}
