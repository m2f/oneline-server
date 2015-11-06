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

package com.bizosys.oneline.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bizosys.oneline.util.Configuration;


public interface IService {
	static Logger LOG = LogManager.getLogger(IService.class.getName());

	static int LVL_SUBOPTIMAL = -1;
	static int LVL_OPTIMAL = 0;
	static int LVL_DANGER = 1;
	
	boolean serviceStart(Configuration conf);
	boolean delayedStart();
	boolean serviceStop();
	boolean serviceSuspend();
	boolean serviceResume();
	boolean serviceRefresh(); 
	
	/**
	 * This sets the service on a resource available zone
	 * If resource depletes,
	 * 	> Non critical services will suspend.
	 *  > If something can be postponed, letz do it.  
	 * @param lvl
	 */
	void setWorkingLevel(int lvl);  
	int getWorkingLevel();
}
