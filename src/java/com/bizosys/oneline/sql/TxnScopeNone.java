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
import java.util.List;

import org.apache.log4j.Logger;

import com.bizosys.oneline.dao.ReadBase;
import com.bizosys.oneline.dao.WriteBase;

public class TxnScopeNone implements TxnScope {
	String  pool;
	private final static Logger LOG = Logger.getLogger(TxnScopeNone.class);

	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	
	
	public TxnScopeNone(String pool) {
		this.pool = pool;
	}
	
	@Override
	public int execute(String query) throws SQLException {
		if(DEBUG_ENABLED) LOG.debug("TxnScopeNone >> execute string" );
		return new WriteBase(pool).execute(query);
	}
	
	@Override
	public int execute(String query, List<Object> params) throws SQLException {
		if(DEBUG_ENABLED) LOG.debug("TxnScopeNone >> execute string" );
		return new WriteBase(pool).execute(query, params);
	}

	@Override
	public void setReadOnTxnScope(ReadBase<?> reader) {
	}

	@Override
	public void commitTransaction() {
	}

	@Override
	public void rollbackTransaction() {
	}

	@Override
	public void beginTransaction() throws SQLException {
	}

	@Override
	public void setPoolName(String poolName) {
		this.pool = poolName;
		
	}

	@Override
	public void openTempTransaction() throws SQLException{
	}

	@Override
	public void closeTempTransaction() throws SQLException {
	}

	@Override
	public void commitTransaction(boolean isOpenTempTransaction) throws SQLException {
	}

}
