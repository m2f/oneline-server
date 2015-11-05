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

public class TxnScopeRequest implements TxnScope {

	private final static Logger LOG = Logger.getLogger(TxnScopeRequest.class);
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	WriteBase txnScope = null;
	String pool;
	
	public TxnScopeRequest(String pool) {
		this.pool = pool;
	}

	@Override
	public int execute(String query) throws SQLException {
		getWriter();
		if(DEBUG_ENABLED) LOG.debug("Executing queries in TxnScopeRequest execute(query) " +  this.txnScope.hashCode());
		return this.txnScope.execute(query);
	}
	
	@Override
	public int execute(String query, List<Object> params) throws SQLException {
		getWriter();
		if(DEBUG_ENABLED) LOG.debug("Executing queries in TxnScopeRequest execute(query,params) " +  this.txnScope.hashCode());
		return this.txnScope.execute(query, params);
	}
	
	private WriteBase getWriter() {
		if (null != txnScope) return txnScope;
		txnScope = new WriteBase(pool);
		return txnScope;
	}

	@Override
	public void commitTransaction() throws SQLException {
		txnScope.commitTransaction();
	}

	@Override
	public void commitTransaction(boolean isOpenTempTransaction) throws SQLException {
		txnScope.commitTransaction(isOpenTempTransaction);
	}

	@Override
	public void rollbackTransaction() throws SQLException {
		txnScope.rollbackTransaction();
	}

	@Override
	public void beginTransaction() throws SQLException {
		getWriter();
		txnScope.beginTransaction();
	}

	@Override
	public void setReadOnTxnScope(ReadBase<?> reader) {
		if ( null != txnScope) txnScope.setReadOnTxnScope(reader);
	}

	@Override
	public void setPoolName(String poolName) {
		this.pool = poolName;
	}

	@Override
	public void openTempTransaction() throws SQLException {
		getWriter().openTempTransaction();
		if(DEBUG_ENABLED) LOG.debug("Opened temp transaction " + this.txnScope.hashCode());
	}

	@Override
	public void closeTempTransaction() throws SQLException {
		getWriter().closeTempTransaction();
		if(DEBUG_ENABLED) LOG.debug("Closed temp transaction " + this.txnScope.hashCode());
	}

}
