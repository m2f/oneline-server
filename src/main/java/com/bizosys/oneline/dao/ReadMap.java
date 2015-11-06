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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This reads all the fetched rows and stores as List<Map<String, String>> object.
 * This will help for data transmission from one system to another system. Custom 
 * API calls, Web service calls. However, a huge amount of fetched rows will create
 * many objects, slowing down the server. That needs to be restricted.    
 * @author abinash
 *
 */
public class ReadMap extends ReadBase<Map<String, String>> {

	private final static Logger LOG = LogManager.getLogger(ReadMap.class);

	@Override
	protected List<Map<String, String>> populate() throws SQLException {
		checkCondition();
		ResultSetMetaData md = rs.getMetaData() ;
		int totalCol = md.getColumnCount();
		String[] cols = createLabels(md, totalCol);
		List<Map<String, String>> records = new ArrayList<Map<String, String>>();

		while (this.rs.next()) {
			Map<String, String> aRecord = createRecord(totalCol, cols);
			records.add(aRecord);
		}
		return records;
	}

	@Override
	protected Map<String, String> getFirstRow() throws SQLException {
		checkCondition();
		ResultSetMetaData md = rs.getMetaData() ;
		int totalCol = md.getColumnCount();
		String[] cols = createLabels(md, totalCol);

		Map<String, String> aRecord = null;
		if (this.rs.next()) {
			aRecord = createRecord(totalCol, cols);
		}
		return aRecord;
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

	private Map<String, String> createRecord(int totalCol, String[] cols) throws SQLException {
		Map<String, String> aRecord = new HashMap<String, String>(totalCol);
		for ( int i=0; i<totalCol; i++ ) {
			Object value = rs.getObject(i+1);
			if ( null != value ) {
				aRecord.put(cols[i], value.toString());
			}
		}
		return aRecord;
	}
	
}
