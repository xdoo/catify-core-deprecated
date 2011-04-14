package com.catify.core.event;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.catify.core.process.ProcessHelper;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.query.SqlPredicate;

import junit.framework.TestCase;

public class TestHazelcastTimerEventService extends TestCase implements Serializable {

	public void testSearch(){
		
		long time = new Date().getTime();
		
		IMap<String, Timer> map = Hazelcast.getMap("timer");
		map.put(ProcessHelper.createTaskInstanceId("1", "4711"), new Timer(time, "1", "4711"));
		map.put(ProcessHelper.createTaskInstanceId("2", "4711"), new Timer(time, "2", "4711"));
		map.put(ProcessHelper.createTaskInstanceId("3", "4711"), new Timer(time, "3", "4711"));
		map.put(ProcessHelper.createTaskInstanceId("4", "4711"), new Timer(time + 5, "4", "4711"));
		
		Iterator<Object> it = map.values(new SqlPredicate(String.format("time = %s", time))).iterator();
		int x = 0;
		while (it.hasNext()) {
			Timer t = (Timer) it.next();
			System.out.println(t.getInstanceId());
			x++;
		}
		
	}
	
	public class Timer implements Serializable {

		private static final long serialVersionUID = 4233809355282392353L;
		
		private long time;
		private String instanceId;
		private String nodeId;
		
		public Timer(long time, String instanceId, String nodeId){
			this.time = time;
			this.instanceId = instanceId;
			this.nodeId = nodeId;
		}
		
		
		public long getTime() {
			return time;
		}
		public void setTime(long time) {
			this.time = time;
		}
		public String getInstanceId() {
			return instanceId;
		}
		public void setInstanceId(String instanceId) {
			this.instanceId = instanceId;
		}
		public String getNodeId() {
			return nodeId;
		}
		public void setNodeId(String nodeId) {
			this.nodeId = nodeId;
		}
		
		
		
	}
	
}
