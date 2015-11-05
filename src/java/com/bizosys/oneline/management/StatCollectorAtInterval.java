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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.bizosys.oneline.util.OnelineServerConstants;
import com.bizosys.oneline.util.ILogger;
import com.bizosys.oneline.util.LoggerFactory;

public class StatCollectorAtInterval 
{

	private static StatCollectorAtInterval singleton = null;
	public static StatCollectorAtInterval getInstance() 
	{
		if ( null != singleton) return singleton;
		synchronized (StatCollectorAtInterval.class.getName()) 
		{
			if ( null != singleton) return singleton;
			singleton = new StatCollectorAtInterval();
		}
		return singleton;
	}
	
	public static ILogger l = LoggerFactory.getLogger(StatCollectorAtInterval.class, OnelineServerConstants.IS_CONSOLE_LOG);
	public boolean INFO_ENABLED = l.isInfoEnabled();
	private List<IHealth> healthAgents = new ArrayList<IHealth>();
	
	private  StatCollectorAtInterval() 
	{
	}
	
	public void register(IHealth healthAgent)
	{
		l.info("Registering: " + healthAgent.getClass().getName());
		healthAgents.add(healthAgent);
	}

	
	//--------------Health Check Module-------------------
	public String healthCheck(char separator) {
		
		StringBuilder sb = new StringBuilder();
		Runtime r = Runtime.getRuntime();
		sb.append("jvm-maxm-mb:").append(r.maxMemory()/1048576).append(separator).
		append("jvm-totm-mb:").append(r.totalMemory()/1048576).append(separator).
		append("jvm-freem-mb:").append(r.freeMemory()/1048576).append(separator).
		append("jvm-threads-no:").append(Thread.activeCount()).append(separator);
		
		for (IHealth health : healthAgents) 
		{
			Vector<Metric> collector = health.collect();
			if ( null == collector) continue;

			for (Metric mm : collector) 
			{
				sb.append(separator).append(mm.name).append(':').append(mm.val);
			}
		}
		
		return sb.toString();
	}
}
