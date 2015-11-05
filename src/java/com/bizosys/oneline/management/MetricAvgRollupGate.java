/**
* Copyright 2010 Bizosys Technologies Limited
*
* Licensed to the Bizosys Technologies Limited (Bizosys) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The Bizosys licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.bizosys.oneline.management;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.bizosys.oneline.util.OnelineServerConstants;
import com.bizosys.oneline.util.ILogger;
import com.bizosys.oneline.util.LoggerFactory;

public class MetricAvgRollupGate implements IHealth {

	private static MetricAvgRollupGate singleton = null;

	public static ILogger l = LoggerFactory.getLogger(MetricAvgRollupGate.class, OnelineServerConstants.IS_CONSOLE_LOG);

	public static MetricAvgRollupGate getInstance() 
	{
		if (null != singleton)
			return singleton;
		synchronized (MetricAvgRollupGate.class.getName()) 
		{
			if (null != singleton)
				return singleton;
			singleton = new MetricAvgRollupGate();
		}
		return singleton;
	}

	final ThreadLocal<Clock> clock = new ThreadLocal<Clock>() 
	{
		@Override
		protected Clock initialValue() 
		{
			return new Clock();
		}
	};
	
	Set<MetricAvgRollup> reqs = new HashSet<MetricAvgRollup>();
	public void register(MetricAvgRollup aReq) {
		reqs.add(aReq);
	}

	private MetricAvgRollupGate() 
	{
		for (MetricAvgRollup mr: reqs) {
			mr.totalRequests = 0;
			mr.activeSessions = 0;
		}
	}

	public void onEnter(MetricAvgRollup mr) {
		mr.totalRequests++;
		mr.activeSessions++;
		clock.get().time = System.currentTimeMillis();
	}

	public void onExit(MetricAvgRollup mr, boolean isSucess) 
	{
		mr.activeSessions--;
		long endTime = System.currentTimeMillis();
		long diff = (endTime - clock.get().time);
		mr.mmaRequestTime.set((int) diff);
		
		if ( !isSucess ) mr.failedRequests++;
		
		
	}
	
	private static final class Clock {
		public long time = 0;
	}

	@Override
	public Vector<Metric> collect() 
	{
		Vector<Metric> collector = new Vector<Metric>();
		for (MetricAvgRollup aReq : reqs) {
			
			collector.add(new Metric(aReq.measures[MetricAvgRollup.REQUESTS_PROCESSED], aReq.getRequestsProcessed() ));
			collector.add(new Metric(aReq.measures[MetricAvgRollup.REQUESTS_ACTIVE], aReq.activeSessions));
			collector.add(new Metric(aReq.measures[MetricAvgRollup.REQUESTS_FAILED], aReq.failedRequests));
			collector.add(new Metric(aReq.measures[MetricAvgRollup.REQUESTS_TOTAL], aReq.mmaRequestTime.getTotal()));
			collector.add(new Metric(aReq.measures[MetricAvgRollup.MIN_RESPONE_MILLIS], aReq.mmaRequestTime.getMinVal()));
			collector.add(new Metric(aReq.measures[MetricAvgRollup.AVG_RESPONE_MILLIS], aReq.mmaRequestTime.getAvgVal()));
			collector.add(new Metric(aReq.measures[MetricAvgRollup.MAX_RESPONE_MILLIS], aReq.mmaRequestTime.getMaxVal()));

			
			aReq.reset();
		}
		return collector;
	}
}