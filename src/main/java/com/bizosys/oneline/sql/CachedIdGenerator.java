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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bizosys.oneline.dao.IPool;
import com.bizosys.oneline.dao.PoolFactory;
import com.bizosys.oneline.util.ErrorCodes;
import com.bizosys.oneline.util.OnelineServerConstants;
import com.bizosys.oneline.util.StringUtils;

public class CachedIdGenerator {

	private final static Logger LOG = LogManager.getLogger(CachedIdGenerator.class);
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

	private static final String UPDATE_QUERY = "update idsequence set sequenceno = sequenceno + ? where sequencename = ?";
	private static final String SELECT_QUERY = "select sequenceno from idsequence where sequencename = ?";
	private static final String POOLNAME = "configpool"; 
	private Map<String, IdCounter> cache = new HashMap<String, IdCounter>(64);

	static CachedIdGenerator instance = null;
	public static CachedIdGenerator getInstance() {
		if ( null != instance ) return instance;
		synchronized (CachedIdGenerator.class.getName()) {
			if ( null != instance ) return instance;
			instance = new CachedIdGenerator();
		}
		return instance;
	}
	
	private CachedIdGenerator(){
	}
	
	public static final class IdCounter {

		public int currentCounter = 1;
		public int bufferCounter = OnelineServerConstants.DEFAULT_VALUE;

		@Override
		public String toString() {
			return "Current Value : " + currentCounter 
					+ ", Buffer Value : " + bufferCounter;
		}
	}

	public int generateId( String sequenceKey ) throws SQLException {

		/*
		 * Make a database call for first time and 
		 * when the buffer counter becomes zero.
		 * Else Directly serve it from cache.
		 */
		synchronized (sequenceKey) {
			IdCounter idcounter = cache.get(sequenceKey);
			if( null == idcounter || idcounter.bufferCounter == 0 ) {

				int currentId = 0;
				Integer cacheAmount = OnelineServerConstants.CACHED_AMOUNT.get(sequenceKey);
				if( null == cacheAmount ) cacheAmount = OnelineServerConstants.DEFAULT_VALUE;

				if ( DEBUG_ENABLED ) LOG.debug("Fetching from database ids for idsequence: " + sequenceKey);
				
				int nextid = getNextId(sequenceKey, cacheAmount, POOLNAME);
				currentId = nextid - cacheAmount;
				
				if( null == idcounter ){
					idcounter = new IdCounter();
					idcounter.currentCounter = currentId;
					idcounter.bufferCounter = cacheAmount - 1;
					cache.put(sequenceKey, idcounter);
				} else {
					idcounter.currentCounter = currentId;
					idcounter.bufferCounter = cacheAmount - 1;
				}
				return currentId;
			}

			/*
			 * Counter exists and also buffer ids are available, serve it from cache. 
			 */
			idcounter.currentCounter++;
			idcounter.bufferCounter--;
			if ( DEBUG_ENABLED ) LOG.debug("Serving from cache id for sequence : " + sequenceKey + " val : " + idcounter.currentCounter);
			return idcounter.currentCounter;
		}
	}

	public int generateIds( String sequenceKey , int amount) throws SQLException {

		/*
		 * Make a database call for first time and 
		 * when the buffer counter becomes zero.
		 * Else Directly serve it from cache.
		 */
		synchronized (sequenceKey) {
			IdCounter idcounter = cache.get(sequenceKey);
			if( null == idcounter || idcounter.bufferCounter <= amount ) {

				int currentId = 0;
				Integer cacheAmount = OnelineServerConstants.CACHED_AMOUNT.get(sequenceKey);
				if( null == cacheAmount ) cacheAmount = OnelineServerConstants.DEFAULT_VALUE;
				
				cacheAmount = (cacheAmount < amount) ? amount : cacheAmount;
				
				if ( DEBUG_ENABLED ) LOG.debug("Fetching from database ids for idsequence: " + sequenceKey);
				
				int nextid = getNextId(sequenceKey, cacheAmount, POOLNAME);
				currentId = nextid - cacheAmount;
				
				if( null == idcounter ){
					idcounter = new IdCounter();
					idcounter.currentCounter = currentId + amount;
					idcounter.bufferCounter = cacheAmount - amount;
					cache.put(sequenceKey, idcounter);
				} else {
					idcounter.currentCounter = currentId + amount;
					idcounter.bufferCounter = cacheAmount - amount;
				}
				return idcounter.currentCounter;
			}

			/*
			 * Counter exists and also buffer ids are available, serve it from cache. 
			 */
			idcounter.currentCounter = idcounter.currentCounter + amount;
			idcounter.bufferCounter = idcounter.bufferCounter - amount;
			if ( DEBUG_ENABLED ) LOG.debug("Serving from cache id for sequence : " + sequenceKey + " val : " + idcounter.currentCounter);
			return idcounter.currentCounter;
		}
	}

	private Integer getNextId( String sequenceKey , int cacheAmount, String poolName ) throws SQLException {

		int nextId = -1;
		IPool pool = ( StringUtils.isEmpty(poolName)) ?  
				PoolFactory.getDefaultPool() : PoolFactory.getInstance().getPool(poolName, false);

		Connection conn = null;
		PreparedStatement stmtU = null;
		PreparedStatement stmtS = null;
		ResultSet rs = null;

		try {
			conn = pool.getConnection();
			stmtU = conn.prepareStatement(UPDATE_QUERY);
			stmtU.setInt(1, cacheAmount ) ;
			stmtU.setString(2, sequenceKey ) ;

			stmtU.execute(); stmtU.close(); stmtU = null;

			stmtS = conn.prepareStatement(SELECT_QUERY);
			stmtS.setString(1, sequenceKey ) ;
			stmtS.execute(); 
			rs = stmtS.getResultSet();

			String error = null;
			if ( rs.next() ) {
				nextId = rs.getInt(1);
			} else {
				error = ErrorCodes.SEQUENCE_ID_NOTSETUP;
			}

			rs.close(); rs = null;
			stmtS.close(); stmtS = null;
			conn.close(); conn = null; //Returns the connection

			if ( null != error) {
				throw new SQLException( "Not able to generate id [" + sequenceKey + "] , for amount [" +  cacheAmount + "] under pool [" + poolName + "]");
			}

		} catch (SQLException ex) {
			LOG.fatal("Error during idgenerator." , ex);
			for (AutoCloseable closable : new AutoCloseable[]{rs,stmtU,stmtS,conn}) {
				if ( null != closable ) {
					try {
						closable.close();
					} catch (Exception cex) {
						LOG.warn( "Closing issue during id creation [" + sequenceKey + "] , for amount [" +  cacheAmount + "] under pool [" + poolName + "]");
					}
				}
			}
			throw ex;
		}

		if ( nextId == -1) {
			throw new SQLException( "Not able to generate id [" + sequenceKey + "] , for amount [" +  cacheAmount + "] under pool [" + poolName + "]");
		}

		return nextId;
	}

	public static void main(String[] args) throws Exception {
		/**
		 * Multiple id generate 
		 */
		IdCounter c = new IdCounter();
		c.currentCounter = 200;
		CachedIdGenerator.getInstance().cache.put("A", c);
		
		for(int i = 0 ; i < 100; i++){
			CachedIdGenerator.getInstance().generateIds("A", 3);
			System.out.println(c.toString());
		}
		
		/**
		 * Single id generate
		 */
//		long start = System.currentTimeMillis();
//		Thread t1 = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				for(int i = 1 ; i < 500; i++) {
//					try {
//						if(i == 50)Thread.sleep(200);
//						System.out.print("A--");
//						idgen.generateId("A");
//					} catch (Exception e) {
//						e.printStackTrace();
//						System.out.println("Error generating ids.");
//					}
//				}
//			}
//		});
//		t1.start();
//		t1.join();
//
//		Thread t2 = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				for(int i = 1 ; i < 500; i++) {
//					try {
//						if(i == 50)Thread.sleep(200);
//						System.out.print("B--");
//						idgen.generateId("B");
//					} catch (Exception e) {
//						e.printStackTrace();
//						System.out.println("Error generating ids.");
//					}
//				}
//			}
//		});
//		t2.start();
//		t2.join();
//
//		Thread t3 = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				for(int i = 1 ; i < 500; i++) {
//					try {
//						if(i == 50)Thread.sleep(200);
//						System.out.print("C--");
//						idgen.generateId("C");
//					} catch (Exception e) {
//						e.printStackTrace();
//						System.out.println("Error generating ids.");
//					}
//				}
//			}
//		});
//		t3.start();
//		t3.join();
//
//		long end = System.currentTimeMillis();
//		System.out.println(end - start);
	}
}
