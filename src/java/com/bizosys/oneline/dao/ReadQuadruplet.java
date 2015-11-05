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

public class ReadQuadruplet extends ReadBase<ReadQuadruplet.Quadruplet> {

	public static class Quadruplet {
		public Object first;
		public Object second;
		public Object third;
		public Object fourth;
		public Quadruplet(Object first, Object second, Object third, Object fourth) {
			this.first = first;
			this.second = second;
			this.third = third;
			this.fourth = fourth;
		}
		
	}
	private final static Logger LOG = Logger.getLogger(ReadQuadruplet.class);
	
	protected List<Quadruplet> populate() throws SQLException {
		if ( null == this.rs) {
			LOG.warn("Rs is not initialized.");
			throw new SQLException("Rs is not initialized.");
		}
		
		List<Quadruplet> records = new ArrayList<Quadruplet>();
		while (this.rs.next()) {
			Quadruplet twin = new Quadruplet(rs.getObject(1), rs.getObject(2), rs.getObject(3), rs.getObject(4));
			records.add(twin);
		}
		return records;
	}

	@Override
	protected ReadQuadruplet.Quadruplet getFirstRow() throws SQLException {
		if ( null == this.rs) {
			LOG.warn("Rs is not initialized.");
			throw new SQLException("Rs is not initialized.");
		}
		
		if (this.rs.next()) {
			return 	new Quadruplet(rs.getObject(1), rs.getObject(2), rs.getObject(3), rs.getObject(4));
		}
		
		return null;
	}
}