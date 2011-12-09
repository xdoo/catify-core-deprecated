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
package com.catify.persistence.beans;

import com.catify.core.event.impl.beans.TimerEvent;
import com.catify.persistence.beans.base.BaseKey;

public class TimerCache extends BaseKey {

	private static final long serialVersionUID = -1513824722140463130L;
	private TimerEvent beanValue;
	
	public TimerCache() {}
	
	public TimerCache(String key, TimerEvent beanValue) {
		this.setBeanKey(key);
		this.setBeanValue(beanValue);
	}

	public TimerEvent getBeanValue() {
		return beanValue;
	}

	public void setBeanValue(TimerEvent beanValue) {
		this.beanValue = beanValue;
	}
}
