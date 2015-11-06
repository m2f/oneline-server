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

package com.bizosys.oneline.dao;

import java.io.PrintWriter;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bizosys.oneline.util.OnelineServerConstants;

public class ReadJsonArray extends ReadBase<String> {

	public String docName = null; 

	private final static Logger LOG = LogManager.getLogger(ReadJsonArray.class);
	private final static boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	
	private PrintWriter out = null;
	
	public ReadJsonArray(PrintWriter out) 
	{
		this.out = out;
	}
	
	@Override
	protected List<String> populate() throws SQLException {
		
		checkCondition();
		ResultSetMetaData md = rs.getMetaData() ;
		int totalCol = md.getColumnCount();
		String[] cols = createLabels(md, totalCol);
		int[] colTypes = getDataTypes(md, totalCol);
		
		List<String> records = null;
		boolean isStreaming = ( null != out );
		if ( isStreaming ) out.println("\"values\" : [");  
		else records = new ArrayList<String>(totalCol);

		StringBuilder aRecord = new StringBuilder(256);
		boolean isFirstRow = true;
		int count = 0;
		
		while (this.rs.next()) {
			if ( isFirstRow ) isFirstRow = false;
			else out.println("},\n");
			out.println("{ ");
			createRecord(totalCol, cols, colTypes, aRecord);
			if ( isStreaming ) {
				out.print(aRecord.toString());
			} else {
				records.add(aRecord.toString());
			}
			aRecord.delete(0, aRecord.capacity());
			if( DEBUG_ENABLED ) {
				if(count++ % OnelineServerConstants.DB_RCORDS_FETCH_LOG_INTERVAL == 0)
					LOG.debug("Fetched record count is " + count);
			}
		}
		if ( isStreaming )
		{
			if(!isFirstRow)
				out.println("}]");
			else
				out.println("]");
		}
		return records;
	}

	@Override
	protected String getFirstRow() throws SQLException {
		checkCondition();
		ResultSetMetaData md = rs.getMetaData() ;
		int totalCol = md.getColumnCount();
		String[] cols = createLabels(md, totalCol);
		int[] colTypes = getDataTypes(md, totalCol);
		
		StringBuilder aRecord = new StringBuilder(256);
		if (this.rs.next()) {
			createRecord(totalCol, cols, colTypes, aRecord);
		}
		return aRecord.toString();
	}

	
	private void checkCondition() throws SQLException {
		if ( null == this.rs) {
			LOG.warn("Rs is not initialized.");
			throw new SQLException("Rs is not initialized.");
		}
	}

	private String[] createLabels(ResultSetMetaData md, int totalCol) throws SQLException {
		String[] cols = new String[totalCol];
		for ( int i=0; i<totalCol; i++ ) {
			cols[i] = md.getColumnLabel(i+1);
		}
		return cols;
	}
	
	
	private int[] getDataTypes(ResultSetMetaData md, int totalCol) throws SQLException {
		int[] cols = new int[totalCol];
		for ( int i=0; i<totalCol; i++ ) {
			cols[i] = md.getColumnType(i+1);
		}
		return cols;
	}

	private void createRecord(int colsT, String[] cols, int [] types, StringBuilder recordsSb) throws SQLException {
		
		Object colObj = null; 
		String colStr = null;
		boolean isFirst = true;

		for ( int colI=0; colI<colsT; colI++ ) 
		{
			colObj = rs.getObject(colI+1);
			colStr = getColObjStr(colObj, types[colI]);
			
			if (isFirst) isFirst = false;
			else recordsSb.append(",\n");
			
			recordsSb.append('"').append(cols[colI]).append("\":").append(colStr);
		}
	}
	
	public String getColObjStr(Object colObj, int type)
	{
		if ( null == colObj ) return "null";
		switch (type) {

			case java.sql.Types.BIT:{
				return Boolean.parseBoolean(colObj.toString()) ? "1" : "0";
			}
			case java.sql.Types.BOOLEAN :
			case java.sql.Types.TINYINT:
			case java.sql.Types.SMALLINT:
			case java.sql.Types.INTEGER:
            case java.sql.Types.BIGINT:
			case java.sql.Types.FLOAT:
			case java.sql.Types.REAL:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.NUMERIC:
			case java.sql.Types.DECIMAL:
			case java.sql.Types.NULL:
			{
				return colObj.toString();
			}
			default:
			{
				StringBuilder sb = new StringBuilder();
				return sb.append("\"").append(colObj.toString()).append("\"").toString();
			}
		}
	}

}