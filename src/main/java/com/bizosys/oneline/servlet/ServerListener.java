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

package com.bizosys.oneline.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bizosys.oneline.service.ServiceFactory;
import com.bizosys.oneline.util.Configuration;
import com.bizosys.oneline.util.OnelineServerConfiguration;

public class ServerListener
{
	private final static Logger LOG = LogManager.getLogger(ServerListener.class);
	
    public void startup(Configuration conf)
    {
    	LOG.info("> Starting Dataservice Services");
	      ServiceFactory.getInstance().serviceStart(conf);
    }
    /**
     * Initiate all services and then makes an execution.
     * 
     */
    public static void main(String[] args) throws Exception {
		ServerListener listener = new ServerListener();
	    Configuration conf = OnelineServerConfiguration.getInstance().getConfiguration();
		listener.startup(conf);
    }      
}