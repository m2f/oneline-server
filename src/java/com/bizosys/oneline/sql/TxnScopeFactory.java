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
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.bizosys.oneline.dao.WriteBase;
import com.bizosys.oneline.util.ErrorCodeExp;
import com.bizosys.oneline.util.ErrorCodes;

public class TxnScopeFactory {
	
	private final static Logger LOG = Logger.getLogger(TxnScopeFactory.class);

	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	
	private static final String TRANSACTION_ERR_TITLE = "TRANSACTION_ERROR";
	
	public class TxnObject {
		public WriteBase writeBase;
		public int idleTimeInMillis = 100;
		
		public TxnObject(WriteBase writeBase, int idleTimeInMillis) {
			this.writeBase = writeBase;
			this.idleTimeInMillis = idleTimeInMillis;
		}
	}
	
	
	public static TxnScopeFactory instance;
	
	public static TxnScopeFactory getInstance() {
		if ( null != instance) return instance;
		synchronized (TxnScopeFactory.class.getName()) {
			if ( null != instance) return instance;
			instance = new TxnScopeFactory();
		}
		return instance;
	}	
	
	AtomicLong txnIds = new AtomicLong(1);
	Map<Long, TxnObject> allLiveTxns = new ConcurrentHashMap<Long, TxnObject>(); 

	public Long createClientTxn(String pool, int idleTimeInMillis) throws SQLException {
		Long nextId = txnIds.incrementAndGet();
		WriteBase wb = new WriteBase(pool);
		allLiveTxns.put(nextId, new TxnObject(wb,idleTimeInMillis) );
		wb.beginTransaction();
		final Timer txtRollbackTimer = new Timer();
		txtRollbackTimer.schedule(new TxnAutoRollBack(txtRollbackTimer, nextId), idleTimeInMillis);
		return nextId;
	}
	
	public boolean closeClientTxn(Long txnId, boolean commit) throws ErrorCodeExp,SQLException {
		long txnIdL = ( null == txnId) ? -1 : txnId.longValue();
		if (DEBUG_ENABLED) LOG.debug("Transaction closing");
		if ( ! allLiveTxns.containsKey(txnIdL))
			throw new ErrorCodeExp(TRANSACTION_ERR_TITLE, ErrorCodes.TXN_SESSION_EXPIRED, "Txn Id :" + txnIdL, ErrorCodes.QUERY_KEY);

		TxnObject txnObject = allLiveTxns.get(txnIdL);
		if ( commit ) txnObject.writeBase.commitTransaction();
		else txnObject.writeBase.rollbackTransaction();
		allLiveTxns.remove(txnIdL);		
		return true;
	}
	
	public void autoRollBackClientTxn(Long txnId) throws ErrorCodeExp,SQLException {
		long txnIdL = ( null == txnId) ? -1 : txnId.longValue();
		if (DEBUG_ENABLED) LOG.debug("Transaction closing");
		TxnObject txnObject = allLiveTxns.get(txnIdL);
		if ( allLiveTxns.containsKey(txnIdL)){
			txnObject.writeBase.rollbackTransaction();
			allLiveTxns.remove(txnIdL);	
		}
	}

	private TxnScope get(long txnId)  throws ErrorCodeExp {
		if ( allLiveTxns.containsKey(txnId)) 
			return new TxnScopeClient(allLiveTxns.get(txnId).writeBase);
		else throw new ErrorCodeExp(TRANSACTION_ERR_TITLE, ErrorCodes.TXN_SESSION_EXPIRED, "Txn expired - " + txnId, ErrorCodes.QUERY_KEY);
	}

	private TxnScope get(boolean isMultiStmt, String pool) {
		if ( isMultiStmt ) return new TxnScopeRequest(pool);
		else return new TxnScopeNone(pool);
	}
	
	public TxnScope get(Long txnId, boolean isMultiStmt, String pool ) throws ErrorCodeExp {
		if ( null == txnId) return get(isMultiStmt, pool );
		return get(txnId.longValue());
	}

}
