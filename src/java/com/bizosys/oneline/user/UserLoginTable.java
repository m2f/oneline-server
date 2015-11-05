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

package com.bizosys.oneline.user;

import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;

import com.bizosys.oneline.dao.DbConfig;
import com.bizosys.oneline.dao.PoolFactory;
import com.bizosys.oneline.dao.ReadObject;
import com.bizosys.oneline.dao.ReadXml;
import com.bizosys.oneline.dao.WriteBase;
import com.bizosys.oneline.sql.JdbcConfigLoader;
import com.bizosys.oneline.util.FileReaderUtil;


public class UserLoginTable {

	/** The VO Class */
	public static final Class<UserLogin> clazz = UserLogin.class;

	/** The SQL Select statement */
	public static String sqlSelect =
		"select id, tenantid, active, loginid, password, profile,touchtime	 from user_login";

	/** The SQL Select statements of all records */
	public static List<UserLogin> selectAll() throws SQLException {
		return new ReadObject<UserLogin>(clazz).execute(sqlSelect);
	}

	/** The SQL Select statements on indexed fields and primary keys */
	private static String sqlSelectByid = sqlSelect + " where id = ?";

	private static String sqlSelectByloginid = sqlSelect + " where loginid = ?";

	/** The SQL Insert statement with auto increment */
	private static String sqlInsert =
		"insert into user_login (tenantid, active, loginid, password, profile	 ) " + 
 		"values (?, ?, ?, ?, ? )";

	/** The SQL Insert statement with primary key */
	private static String sqlInsertPK =
		"insert into user_login (id, tenantid, active, loginid, password, profile	 ) " + 
 		"values (?, ?, ?, ?, ?, ? )";

	/** The SQL Update statement */
	private static String sqlUpdate =
		"update user_login SET tenantid = ?, active = ?, loginid = ?, password = ?, profile = ? " + 
		"where id = ?	";



	/** The private constructor. All methods are static public */
	protected UserLoginTable() {
	}


	/** Sql select functions */
	public static UserLogin selectById( Object id) throws SQLException {
		Object record = new ReadObject<UserLogin>(clazz).selectByPrimaryKey(sqlSelectByid,id);
		if ( null == record) return null;
		return (UserLogin) record;
	}

	public static String selectXmlById( Object id) throws SQLException {
		Object record = new ReadXml<UserLogin>(clazz).selectByPrimaryKey(sqlSelectByid,id);
		if ( null == record) return null;
		return (String) record;
	}

	public static UserLogin selectByLoginid( Object loginid) throws SQLException {
		Object record = new ReadObject<UserLogin>(clazz).selectByPrimaryKey(sqlSelectByloginid,loginid);
		if ( null == record) return null;
		return (UserLogin) record;
	}

	public static String selectXmlByLoginid( Object loginid) throws SQLException {
		Object record = new ReadXml<UserLogin>(clazz).selectByPrimaryKey(sqlSelectByloginid,loginid);
		if ( null == record) return null;
		return (String) record;
	}


	/** Sql Insert with Auto increment function */
	public static void insert( UserLogin record, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) {
			sqlWriter = new WriteBase();
		}

		record.id = sqlWriter.insert(sqlInsert, record.getNewPrint());
	}


	/** Sql Insert with PK function */
	public static void insertPK( UserLogin record, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) {
			sqlWriter = new WriteBase();
		}

		sqlWriter.execute(sqlInsertPK, record.getNewPrintWithPK());
	}


	/** Sql Update function */
	public static void update( UserLogin record, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) {
			sqlWriter = new WriteBase();
		}

		sqlWriter.execute(sqlUpdate, record.getExistingPrint());
	}
	
	public static void main ( String[] args) throws Exception {
		String configXml = FileReaderUtil.toString("conf/jdbc.conf");
		PoolFactory.getInstance().setup(configXml);
		
		UserLogin ul = UserLoginTable.selectByLoginid("root@bizosys.com");
		System.out.println( ul.tenantid.toString() );
		System.out.println( ul.touchtime.toString() );
	}

}


