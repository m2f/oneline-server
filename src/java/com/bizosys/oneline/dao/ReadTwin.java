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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ReadTwin extends ReadBase<ReadTwin.Twin> {

	public static class Twin {
		public Object first;
		public Object second;
		public Twin(Object first, Object second) {
			this.first = first;
			this.second = second;
		}
		
	}
	private final static Logger LOG = Logger.getLogger(ReadTwin.class);
	
	protected List<Twin> populate() throws SQLException {
		if ( null == this.rs) {
			LOG.warn("Rs is not initialized.");
			throw new SQLException("Rs is not initialized.");
		}
		
		List<Twin> records = new ArrayList<Twin>();
		while (this.rs.next()) {
			Twin twin = new Twin(rs.getObject(1), rs.getObject(2));
			records.add(twin);
		}
		return records;
	}

	@Override
	protected ReadTwin.Twin getFirstRow() throws SQLException {
		if ( null == this.rs) {
			LOG.warn("Rs is not initialized.");
			throw new SQLException("Rs is not initialized.");
		}
		
		if (this.rs.next()) {
			return 	new Twin(rs.getObject(1), rs.getObject(2));
		}
		
		return null;
	}
}