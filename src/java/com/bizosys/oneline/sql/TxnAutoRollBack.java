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

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class TxnAutoRollBack extends TimerTask{

	private final static Logger LOG = Logger.getLogger(TxnAutoRollBack.class);
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	
	private long txnId = 0L;
	private Timer timer = null;
	
	public TxnAutoRollBack(Timer timer, long txnId){
		this.timer = timer;
		this.txnId = txnId;
	}

	@Override
	public void run() {
		
		try {
			
			if(DEBUG_ENABLED) LOG.debug("Auto rolling back he transaction : "+this.txnId);
			TxnScopeFactory.getInstance().autoRollBackClientTxn(this.txnId);
			
		}
		catch(Exception e){
			LOG.fatal("Error in auto rolling back the transaction with transation id : " +this.txnId);
		}
		finally
		{
			if ( null != this.timer ) this.timer.cancel();
		}
	}
}
