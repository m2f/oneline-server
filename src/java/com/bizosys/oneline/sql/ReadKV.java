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

package com.bizosys.oneline.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bizosys.oneline.dao.ReadBase;
import com.bizosys.oneline.util.StringUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ReadKV extends ReadBase<Object> {
	
	public Multimap<String, String> foundRecords = ArrayListMultimap.create();
	public List<String> sortedIds = new ArrayList<String>();
	
	protected List<Object> populate() throws SQLException {
		if ( null == this.rs) {
			throw new SQLException("Rs is not initialized.");
		}
		
		String valStr = null;
		int total = rs.getMetaData().getColumnCount();
		while (this.rs.next()) {
			String id = rs.getObject(1).toString();
			sortedIds.add(id);
			if ( null == id) continue;
			
			for ( int i=0; i<total -1; i++) {
				Object val =  rs.getObject(i+2);
				valStr = ( null == val) ? StringUtils.Empty : val.toString();
				foundRecords.put(id, valStr);
			}
		}
		return null;
	}

	@Override
	protected Object getFirstRow() throws SQLException {
		return null;
	}
}