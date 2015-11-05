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

package com.bizosys.oneline.model;

import java.util.List;
import java.sql.SQLException;

import com.bizosys.oneline.dao.PoolFactory;
import com.bizosys.oneline.dao.ReadObject;
import com.bizosys.oneline.dao.ReadXml;
import com.bizosys.oneline.dao.WriteBase;
import com.bizosys.oneline.util.FileReaderUtil;

	
public class AppConfigTable {

	protected static final String CONFIGPOOL = "configpool";

	/** The VO Class */
	public static final Class<AppConfig> clazz = AppConfig.class;

	/** The SQL Select statement */
	public static String sqlSelect =
		"select configtype, title, body, variables, outvar, status	from app_config";

	/** The SQL Select statements of all records */
	public static List<AppConfig> selectAll() throws SQLException {
		
		ReadObject<AppConfig> ro = new ReadObject<AppConfig>(clazz);
		ro.setPoolName(CONFIGPOOL);
		
		return ro.execute(sqlSelect);
	}

	/** The SQL Select statements on indexed fields and primary keys */
	private static String sqlSelectByid = sqlSelect + " where id = ?";

	private static String sqlSelectByconfigtype = sqlSelect + " where configtype = ?";

	/** The SQL Insert statement with auto increment */
	private static String sqlInsert =
		"insert into app_config (configtype, title, body, variables, outvar, status	 ) " + 
 		"values (?, ?, ?, ?, ?, ? )";

	/** The SQL Insert statement with primary key */
	private static String sqlInsertPK =
		"insert into app_config (id, configtype, title, body, variables, outvar, status	 ) " + 
 		"values (?, ?, ?, ?, ?, ?, ? )";

	/** The SQL Update statement */
	private static String sqlUpdate =
		"update app_config SET configtype = ?, title=?, body=?, variables=?, outvar=?, status=? where id=?";


	/** The private constructor. All methods are static public */
	protected AppConfigTable() {
	}


	/** Sql select functions */
	public static AppConfig selectById( Object id) throws SQLException {
		ReadObject<AppConfig> ro = new ReadObject<AppConfig>(clazz);
		ro.setPoolName(CONFIGPOOL);
		Object record = ro.selectByPrimaryKey(sqlSelectByid,id);
		if ( null == record) return null;
		return (AppConfig) record;
	}

	public static String selectXmlById( Object id) throws SQLException {
		ReadXml<AppConfig> ro = new ReadXml<AppConfig>(clazz);
		ro.setPoolName(CONFIGPOOL);
		
		Object record = ro.selectByPrimaryKey(sqlSelectByid,id);
		if ( null == record) return null;
		return (String) record;
	}

	public static List<AppConfig> selectByConfigtype( Object configtype) throws SQLException {
		ReadObject<AppConfig> ro = new ReadObject<AppConfig>(clazz);
		ro.setPoolName(CONFIGPOOL);
		return ro.execute(sqlSelectByconfigtype, new Object[]{configtype});
	}

	public static List<String> selectXmlByConfigtype( Object configtype) throws SQLException {
		ReadXml<AppConfig> ro = new ReadXml<AppConfig>(clazz);
		ro.setPoolName(CONFIGPOOL);
		return ro.execute(sqlSelectByconfigtype, new Object[]{configtype});
	}


	/** Sql Insert with Auto increment function */
	public static void insert( AppConfig record, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) {
			sqlWriter = new WriteBase(CONFIGPOOL);
		}

		record.id = new Long(sqlWriter.insert(sqlInsert, record.getNewPrint()));
	}


	/** Sql Insert with PK function */
	public static void insertPK( AppConfig record, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) {
			sqlWriter = new WriteBase(CONFIGPOOL);
		}

		sqlWriter.execute(sqlInsertPK, record.getNewPrintWithPK());
	}


	/** Sql Update function */
	public static void update( AppConfig record, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) {
			sqlWriter = new WriteBase(CONFIGPOOL);
		}

		sqlWriter.execute(sqlUpdate, record.getExistingPrint());
	}
	
	public static void main(String[] args) throws SQLException {
		String configXml = FileReaderUtil.toString("conf/jdbc.conf");
		PoolFactory.getInstance().setup(configXml);
		new ReadObject<AppConfig>(AppConfig.class).execute(sqlSelect);
		System.out.println("complete");
	}

}