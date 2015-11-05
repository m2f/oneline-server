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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OnelineServerConstants {

	public final static Configuration conf = OnelineServerConfiguration.getInstance().getConfiguration();
	
	public final static boolean IS_CONSOLE_LOG = conf.getBoolean("dataservice.console.log", false);
	
	public final static String AUTHENTICATOR_CLASS = 
		conf.get("authenticator.class.name","com.bizosys.oneline.authorization.BlackholeAuthenticator");

	public final static String AUTHORIZATION_CLASS = 
			conf.get("authoriztion.class.name","com.bizosys.oneline.authorization.BlackholeAuthorizer");

	public final static boolean SQL_CHECK_PERMISSIONS = 
			conf.getBoolean("sql.check.permission", false);

	public final static int DEFAULT_VALUE = conf.getInt("table.ids.amount.default.value", 100);
	
	public final static String TABLE_IDS_AMOUNT = conf.get("table.ids.amount");
	public final static Map<String, Integer> CACHED_AMOUNT = new HashMap<String, Integer>();
	static {
		boolean isEmpty = (TABLE_IDS_AMOUNT == null) ? true : 
						  (TABLE_IDS_AMOUNT.length() == 0) ? true : false;
		if(!isEmpty){
			List<String> tableIdsAmount = new ArrayList<String>();
			LineReaderUtil.fastSplit(tableIdsAmount, TABLE_IDS_AMOUNT, ',');
			int index = 0;
			int amount = DEFAULT_VALUE;
			for (String tableIdAmount : tableIdsAmount) {
				index = tableIdAmount.indexOf('=');
				amount = Integer.parseInt( tableIdAmount.substring(index + 1) );
				CACHED_AMOUNT.put(tableIdAmount.substring(0, index), amount);
			}
		}
	}

	public final static String ENCRYPTION_KEY = "pr!TH@amR@V@TH!od!NS0N";
	public static final String adminPwdHash = Hash.createHex(OnelineServerConstants.ENCRYPTION_KEY, "1et'$dance");
	
	public static final String JDBC_PROTOCOL = "jdbc:mysql://";
	public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	public static final String CONNECTION_URL_OPTION = "?useUnicode=true&amp;useFastDateParsing=false&amp;characterEncoding=UTF-8";
	

	public static final String PROJECT_USER = "project-user";
	public static final String DATSERVICE_POOL_SUFFIX = "_project_pool";

	public static final String DRONE_USER = "drone-user";
	public static final String DRONE_POOL_SUFFIX = "_repl_pool";
	
	public static final String CONFIG_POOL = "configpool";
	
	public final static String CURRENT_MACHINE_IP = conf.get("current.machine.ip","localhost");

	public final static int DB_RCORDS_FETCH_LOG_INTERVAL = conf.getInt("db.records.fetch.log.interval", 5000);

}
