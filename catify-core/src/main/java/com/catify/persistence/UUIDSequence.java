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
package com.catify.persistence;

import java.util.UUID;
import java.util.Vector;
 
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sessions.Session;
 
public class UUIDSequence extends Sequence implements SessionCustomizer {
 
	public UUIDSequence() {
		super();
	}
 
	public UUIDSequence(String name) {
		super(name);
	}
 
	@Override
	public Object getGeneratedValue(Accessor accessor,
			AbstractSession writeSession, String seqName) {
		return UUID.randomUUID().toString().toUpperCase();
	}
 
	@Override
	public Vector getGeneratedVector(Accessor accessor,
			AbstractSession writeSession, String seqName, int size) {
		return null;
	}
 
	@Override
	public void onConnect() {
	}
 
	@Override
	public void onDisconnect() {
	}
 
	@Override
	public boolean shouldAcquireValueAfterInsert() {
		return false;
	}
 
	// test without this operation, too TODO
	public boolean shouldOverrideExistingValue(String seqName,
			Object existingValue) {
		return ((String) existingValue).isEmpty();
	}
 
	@Override
	public boolean shouldUseTransaction() {
		return false;
	}
 
	@Override
	public boolean shouldUsePreallocation() {
		return false;
	}
 
	public void customize(Session session) throws Exception {
		UUIDSequence sequence = new UUIDSequence("system-uuid");
 
		session.getLogin().addSequence(sequence);
	}
 
}
