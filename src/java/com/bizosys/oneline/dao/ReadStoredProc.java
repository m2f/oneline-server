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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.bizosys.oneline.model.StoredProcOutParam;

public class ReadStoredProc extends ReadBase<String> {

	public String docName = null; 
	private final static Logger LOG = Logger.getLogger(ReadStoredProc.class);
	private PrintWriter out = null;
	private String storedProcid = null;
	private List<StoredProcOutParam> outVars = null;
	private StoredProcOutParam errorParam = null;

	public ReadStoredProc(PrintWriter out, String storedProcid, 
			List<StoredProcOutParam> outVars, StoredProcOutParam errorParam) 
	{
		this.out = out;
		this.storedProcid = storedProcid;
		this.outVars = outVars;
		this.errorParam = errorParam;
	}

	@Override
	protected List<String> populate() throws SQLException {

		checkCondition();
		if( this.callableResponse ) {
			int round = 0;
			boolean hasMoreResultsets = false;
			
			out.print("{\"key\" : \"" +storedProcid);
			out.println("\",\n \"type\" : \"sp\",");
			
			do {
				ResultSet rs = this.callableStmt.getResultSet();
				ResultSetMetaData md = rs.getMetaData() ;
				int totalCol = md.getColumnCount();
				String[] cols = createLabels(md, totalCol);
				int[] colTypes = getDataTypes(md, totalCol);

				if(hasMoreResultsets){
					out.println("\"" + round++ + "\":[" ); 
				}else out.println("\"values\" : { \"" + round++ + "\":[" ); 
				
				StringBuilder aRecord = new StringBuilder(256);
				boolean isFirstRow = true;
				while (rs.next()) {
					if ( isFirstRow ) isFirstRow = false;
					else out.println("},\n");
					out.println("{ ");
					createRecord(rs, totalCol, cols, colTypes, aRecord);
					out.print(aRecord.toString());
					aRecord.delete(0, aRecord.capacity());
				}
				out.println("}]");
				
				hasMoreResultsets = this.callableStmt.getMoreResults();
				if(hasMoreResultsets) out.println(",");
			} while (hasMoreResultsets);
			
			if(round > 1){
				out.println("},\"response\":\"multiDataset\" }");
			}else {
				out.println("},\"response\":\"data\" }");
			}
		} else {
			out.println("\"response\":\"data\",\n \"values\" : []}");
		}
		

		if( null == errorParam ) {
			if( null != this.outVars) {
				for (StoredProcOutParam out : this.outVars) {
					String outParamValue = this.callableStmt.getObject(out.outPramIndex).toString();
					out.setOutPramValue(outParamValue);
				}
			}
		} else {
			Object errorParamValue = this.callableStmt.getObject(errorParam.outPramIndex);
			if(null == errorParamValue) {
				if( null != this.outVars) {
					for (StoredProcOutParam out : this.outVars) {
						if( out.outParamName.equals(errorParam.outParamName)) continue;
						String outParamValue = this.callableStmt.getObject(out.outPramIndex).toString();
						out.setOutPramValue(outParamValue);
					}
				}
			} else {
				throw new SQLException("Error executing sp returned error : " + errorParamValue );
			}
		}

		return null;
	}

	@Override
	protected String getFirstRow() throws SQLException {
		return null;
	}


	private void checkCondition() throws SQLException {
		if ( null == this.callableStmt) {
			LOG.warn("Callable statement not created.");
			throw new SQLException("Callable statement not created.");
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

	private void createRecord(ResultSet rs, int colsT, String[] cols, int [] types, StringBuilder recordsSb) throws SQLException {

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

		case java.sql.Types.BIT:
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
		case java.sql.Types.BOOLEAN :
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