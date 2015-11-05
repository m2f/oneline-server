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

package com.bizosys.oneline.util;

import java.sql.SQLException;
import java.util.List;

import com.bizosys.oneline.dao.DbConfig;
import com.bizosys.oneline.dao.PoolFactory;
import com.bizosys.oneline.dao.ReadArray;

public class DbConfigUtil {
	
	private static ILogger LOG = LoggerFactory.getLogger(DbConfigUtil.class, OnelineServerConstants.IS_CONSOLE_LOG);
	private static final String SELECT_CONFIG_BY_IP_USER = "select poolName,connectionUrl,dbUser,dbUserPassword from db_config where machineIp=? AND dbUser=?";
	private static final String SELECT_CONFIG_BY_IP_POOL = "select poolName,connectionUrl,dbUser,dbUserPassword from db_config where machineIp=? AND poolName=?";
	private static final String SELECT_USER_PASS_BY_IP = "select dbUser,dbUserPassword from db_config where machineIp=?";
	
	public static final String[] setupPoolByUser(final String machineIp, final String user) throws SQLException {
		return setPools(SELECT_CONFIG_BY_IP_USER, new Object[]{machineIp, user});
	}
	
	public static final String[] setupPoolByPoolName(final String machineIp, final String poolName) throws SQLException {
		return setPools(SELECT_CONFIG_BY_IP_POOL, new Object[]{machineIp, poolName});
	}
	
	private static final String[] setPools(final String query, final Object[] params) throws SQLException {
		
		ReadArray reader = new ReadArray();
		reader.setPoolName(OnelineServerConstants.CONFIG_POOL);
		
		List<Object[]> vals = reader.execute(query, params);
		
		String[] pools = new String[vals.size()];
		int index = 0;
		
		for(Object[] cols : vals) {
			initializePool(cols);
			pools[index++] = cols[0].toString();
		}
		return pools;
	}

	public static final void initializePool(Object[] poolUrlUserPass) {
		
		DbConfig config = new DbConfig();
		config.isolationLevel = 4;
		config.incrementBy = 2;
		config.idleConnections = 2;
		config.defaultPool = false;
		config.poolName = poolUrlUserPass[0].toString();
		config.connectionUrl = poolUrlUserPass[1].toString() + OnelineServerConstants.CONNECTION_URL_OPTION;
		config.login = poolUrlUserPass[2].toString();  
		config.password = poolUrlUserPass[3].toString();
		config.driverClass = OnelineServerConstants.JDBC_DRIVER;

		if(LOG.isDebugEnabled()) LOG.debug("Initializing pool for [ " + config.poolName + " ]");
		
		try {
			PoolFactory factory = PoolFactory.getInstance();
			if(factory.contains(config.poolName)) {
				factory.stop(true, config.poolName);
			}
			factory.setup(config);
		} catch (Exception e) {
			LOG.warn("Error initializing pool for url - " + config.connectionUrl);
		}
	}
	
	public static DbConfig[] getUsers(String machineIp) throws SQLException {
		
		ReadArray reader = new ReadArray();
		reader.setPoolName(OnelineServerConstants.CONFIG_POOL);
		List<Object[]> vals = reader.execute(SELECT_USER_PASS_BY_IP, new Object[]{machineIp});
		DbConfig[] configs = new DbConfig[vals.size()];
		int index = 0;
		for (Object[] cols : vals) {
			DbConfig config = new DbConfig();
			config.login = cols[0].toString();
			config.password = cols[1].toString();
			configs[index++] = config;
		}
		
		return configs;
	}
	
}
