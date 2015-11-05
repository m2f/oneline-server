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
package com.bizosys.oneline.authorization;

import java.sql.SQLException;
import java.util.List;

import com.bizosys.oneline.dao.ReadObject;


public class AuthorizationTable {

	protected static final String CONFIGPOOL = "configpool";
	
	/** The VO Class */
	public static final Class<Authorization> clazz = Authorization.class;

	/** The SQL Select statement */
	public static String sqlSelect =
		"select rolename,objecttype,objectname,permission from sauthorization";

	/** The SQL Select statements of all records */
	public static List<Authorization> selectAll() throws SQLException {
		ReadObject<Authorization> ro = new ReadObject<Authorization>(clazz);
		ro.setPoolName(CONFIGPOOL);
		return ro.execute(sqlSelect);
	}

}
