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

import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadpoolHealth implements IHealth {

	ThreadPoolExecutor pool = null;
	String name = null;

	public ThreadpoolHealth(String name, ThreadPoolExecutor pool) {
		this.pool = pool;
		this.name = name;
	}

	@Override
	public Vector<Metric> collect() 
	{
		Vector<Metric> collector = new Vector<Metric>();
		if (null != this.pool) 
		{
			collector.add(new Metric(name + "-max-count", this.pool.getMaximumPoolSize()));
			collector.add(new Metric(name + "-active-count", this.pool.getActiveCount()));
			collector.add(new Metric(name + "-pool-size", this.pool.getPoolSize()));
			collector.add(new Metric(name + "-task-count", this.pool.getTaskCount()));
			collector.add(new Metric(name + "-completed-task-count", this.pool.getCompletedTaskCount()));
		}
		return collector;
	}

}