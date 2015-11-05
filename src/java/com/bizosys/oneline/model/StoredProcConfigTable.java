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

import java.sql.SQLException;
import java.util.List;

import com.bizosys.oneline.dao.ReadObject;
import com.bizosys.oneline.dao.ReadXml;
import com.bizosys.oneline.dao.WriteBase;

public class StoredProcConfigTable {

	/** The VO Class */
	public static final Class<StoredProcConfig> clazz = StoredProcConfig.class;
	protected static final String CONFIGPOOL = "configpool";

	/** The SQL Select statement */
	public static String sqlSelect =
		"select id, sp_title as spTitle, sp_call_syntax as spCallSyntax, sp_out_var as spOutVar, " + 
		"sp_err_var as spErrVar, status	from stored_proc_config";

	/** The SQL Select statements of all records */
	public static List<StoredProcConfig> selectAll() throws SQLException {
		ReadObject<StoredProcConfig> ro = new ReadObject<StoredProcConfig>(clazz);
		ro.setPoolName(CONFIGPOOL);
		return ro.execute(sqlSelect);
	}

	/** The SQL Select statements on indexed fields and primary keys */
	protected static String sqlSelectByid = sqlSelect + " where id = ?";

	protected static String sqlSelectBysp_title = sqlSelect + " where sp_title = ?";

	/** The SQL Insert statement with auto increment */
	protected static String sqlInsert =
		"insert into stored_proc_config (sp_title, sp_body, sp_poolname, sp_call_syntax, sp_out_var, sp_err_var, " + 
		"status	 ) " + 
 		"values (?, ?, ?, ?, ?, ?, ? )";

	/** The SQL Insert statement with primary key */
	protected static String sqlInsertPK =
		"insert into stored_proc_config (id, sp_title, sp_body, sp_poolname, sp_call_syntax, sp_out_var, " + 
		"sp_err_var, status	 ) " + 
 		"values (?, ?, ?, ?, ?, ?, ?, ? )";

	/** The SQL Update statement */
	protected static String sqlUpdate =
		"update stored_proc_config SET sp_title = ?, sp_body = ?, sp_poolname = ?, sp_call_syntax = ?, sp_out_var = ?, sp_err_var = ?, " + 
		"status = ? " + 
		"where id = ?	";



	/** The protected constructor. All methods are static public */
	protected StoredProcConfigTable() {
	}


	/** Sql select functions */
	public static StoredProcConfig selectById( Object id) throws SQLException {
		Object record = new ReadObject<StoredProcConfig>(clazz).selectByPrimaryKey(sqlSelectByid,id);
		if ( null == record) return null;
		return (StoredProcConfig) record;
	}

	public static String selectXmlById( Object id) throws SQLException {
		Object record = new ReadXml<StoredProcConfig>(clazz).selectByPrimaryKey(sqlSelectByid,id);
		if ( null == record) return null;
		return (String) record;
	}

	public static StoredProcConfig selectBySpTitle( Object sp_title) throws SQLException {
		Object record = new ReadObject<StoredProcConfig>(clazz).selectByPrimaryKey(sqlSelectBysp_title,sp_title);
		if ( null == record) return null;
		return (StoredProcConfig) record;
	}

	public static String selectXmlBySpTitle( Object sp_title) throws SQLException {
		Object record = new ReadXml<StoredProcConfig>(clazz).selectByPrimaryKey(sqlSelectBysp_title,sp_title);
		if ( null == record) return null;
		return (String) record;
	}


	/** Sql Insert with Auto increment function */
	public static void insert( StoredProcConfig record, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) {
			sqlWriter = new WriteBase();
		}

		record.id = new Long(sqlWriter.insert(sqlInsert, record.getNewPrint()));
	}


	/** Sql Insert with PK function */
	public static void insertPK( StoredProcConfig record, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) {
			sqlWriter = new WriteBase();
		}

		sqlWriter.execute(sqlInsertPK, record.getNewPrintWithPK());
	}


	/** Sql Update function */
	public static void update( StoredProcConfig record, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) {
			sqlWriter = new WriteBase();
		}

		sqlWriter.execute(sqlUpdate, record.getExistingPrint());
	}

}
