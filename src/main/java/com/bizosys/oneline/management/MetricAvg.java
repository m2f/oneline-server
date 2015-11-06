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

public class MetricAvg 
{
	boolean isFirst = true;
	private int minVal = 0;
	private int maxVal = 0;
	private int sum = 0;
	private int count = 0;
	
	public void set(int val)
	{
		if ( isFirst ) {
			this.minVal = val;
			this.maxVal = val;
			this.isFirst = false;
		} else {
			if ( val < minVal ) minVal = val;
			if ( val > maxVal ) maxVal = val;
		}
		sum += val;
		count++;
	}
	
	public double getAvgVal()
	{
		return (isFirst) ? 0 : sum/count;
	}
	
	public int getMinVal()
	{
		return (isFirst) ? 0 : minVal;
	}
	
	public int getTotal()
	{
		return count;
	}

	public int getMaxVal()
	{
		return (isFirst) ? 0 : maxVal;
	}
	
	public void reset() {
		isFirst = true;
		this.sum = 0;
		this.count = 0;		
	}
}

