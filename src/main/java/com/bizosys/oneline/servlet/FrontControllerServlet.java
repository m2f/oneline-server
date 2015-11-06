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

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.bizosys.oneline.service.ServiceFactory;
import com.bizosys.oneline.sql.SqlSensor;
import com.bizosys.oneline.user.UserProfileSensor;
import com.bizosys.oneline.util.Configuration;
import com.bizosys.oneline.util.StringUtils;
import com.bizosys.oneline.web.sensor.Sensor;

public class FrontControllerServlet extends AbstractServlet {
	
	private static final long serialVersionUID = 1L;

	public FrontControllerServlet() {
		System.out.println("FrontControllerServlet constructor");
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException 
	{
		
		super.init(config);
		
		LOG.info("FrontControllerServlet booting services initializing.");

		Sensor user = null;
		Sensor sql = null;
		String services = ServiceFactory.getInstance().getAppConfig().get("services.to.start");
		if( null == services ) services = "";
		LOG.info("Starting services : " + services);
		if ( services.contains("sql")) {

			user = new UserProfileSensor();
			this.setupSensor(user, user.getName());

			sql = new SqlSensor();
			this.setupSensor(sql, sql.getName());
			
		}
		
		Configuration conf = ServiceFactory.getInstance().getAppConfig();
		String restServicesLine = conf.get("rest.services");
		LOG.info("Rest Services : " + restServicesLine);
		
		ClassLoader classLoader = FrontControllerServlet.class.getClassLoader();
		
		if ( ! StringUtils.isEmpty(restServicesLine)) {
			List<String> restClazzes = StringUtils.fastSplit(restServicesLine, ',');
			if ( null != restClazzes) {
				try {
					for (String restClazz : restClazzes) {
						LOG.info("Initiating : " + restClazz);
						Class<?> aClass = classLoader.loadClass(restClazz);
						Sensor aSensor = (Sensor) aClass.newInstance(); 
						this.setupSensor(aSensor, aSensor.getName());
					}
				} catch (RuntimeException e) {
			        e.printStackTrace(System.err);
			        System.exit(1);
				} catch (Exception e) {
			        e.printStackTrace(System.err);
			        System.exit(1);
			    }
			}
		}
		
		LOG.info("FrontControllerServlet Initialized.");
	}
}
