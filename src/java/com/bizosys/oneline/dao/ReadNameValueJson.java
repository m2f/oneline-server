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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ReadNameValueJson extends ReadBase<String>  {

	private final static Logger LOG = Logger.getLogger(ReadNameValueJson.class);
	
	@Override
	protected List<String> populate() throws SQLException {
		
		checkCondition();
		ResultSetMetaData md = rs.getMetaData() ;
		int totalCol = md.getColumnCount();
		String[] cols = createLabels(md, totalCol);
		List<String> records = new ArrayList<String>(totalCol);

		StringBuilder aRecord = new StringBuilder(256);
		while (this.rs.next()) {
			createRecord(totalCol, cols, aRecord);
			records.add(aRecord.toString());
			aRecord.delete(0, aRecord.capacity());
		}
		return records;
	}

	@Override
	protected String getFirstRow() throws SQLException {
		checkCondition();
		ResultSetMetaData md = rs.getMetaData() ;
		int totalCol = md.getColumnCount();
		String[] cols = createLabels(md, totalCol);

		StringBuilder aRecord = new StringBuilder(256);
		if (this.rs.next()) {
			createRecord(totalCol, cols, aRecord);
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

	private void createRecord(int totalCol, String[] cols, StringBuilder recordsSb) throws SQLException {
		recordsSb.append('"').append(cols[0]).append("\":\"").append(cols[1]).append('"');
	}
		
}
