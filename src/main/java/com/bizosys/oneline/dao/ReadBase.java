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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bizosys.oneline.model.StoredProcOutParam;


public abstract class ReadBase<T> {

	protected Connection con = null;
	protected boolean isTxn = false;
	protected Statement stmt = null; 
	protected PreparedStatement prepareStmt = null;
	protected ResultSet rs = null;
	
	String poolName = null;
	
	protected CallableStatement callableStmt = null;
	protected boolean callableResponse = false;
	
	private final static Logger LOG = LogManager.getLogger(ReadBase.class);

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}
	
	
	
	/**
	 * Returns the appropriate results for the given sql statement.
	 * The data is stored in generic Object.class instance 
	 * @param sqlStmt
	 * @return List of records
	 * @throws SQLException
	 */
	public final List<T> execute(String sqlStmt) throws SQLException {

		try {
			if ( null == this.con) {
				this.con = (null == poolName) ? PoolFactory.getDefaultPool().getConnection() : 
					PoolFactory.getInstance().getPool(poolName, false).getConnection();
			}
			
			this.prepare(sqlStmt);
			return this.populate();
		} catch (SQLException ex) {
			LOG.fatal(ex);
			throw(ex);
		} finally {
			this.release();
		}
	}


	/**
	 * This is with prepare statement.
	 * @param sqlStmt
	 * @param clazz
	 * @return
	 * @throws SQLException
	 */
	public List<T> execute(String sqlStmt, Object[] columns) throws SQLException {
		try {
			if ( null == this.con) {
				this.con = (null == poolName) ? PoolFactory.getDefaultPool().getConnection() : 
					PoolFactory.getInstance().getPool(poolName, false).getConnection();
			}
			this.prepare(sqlStmt, columns);
			return this.populate();
		} catch (SQLException ex) {
			LOG.fatal(ex);
			throw(ex);
		} finally {
			this.release();
		}
	}
	
	/**
	 * This is with prepare statement.
	 * @param sqlStmt
	 * @param clazz
	 * @return
	 * @throws SQLException
	 */
	public List<T> execute(String sqlStmt, List<Object> columns) throws SQLException {
		try {
			if ( null == this.con) {
				this.con = (null == poolName) ? PoolFactory.getDefaultPool().getConnection() : 
					PoolFactory.getInstance().getPool(poolName, false).getConnection();
			}
			this.prepare(sqlStmt, columns);
			return this.populate();
		} catch (SQLException ex) {
			LOG.fatal(ex);
			throw(ex);
		} finally {
			this.release();
		}
	}	

	/**
	 * This is with prepare statement to get a record by unique key.
	 * @param sqlStmt
	 * @param clazz
	 * @return
	 * @throws SQLException
	 */
	public T selectByPrimaryKey(String sqlStmt, Object id) throws SQLException {
		try {
			if ( null == this.con) {
				this.con = (null == poolName) ? PoolFactory.getDefaultPool().getConnection() : 
					PoolFactory.getInstance().getPool(poolName, false).getConnection();
			}
			this.prepareForPrimaryKey(sqlStmt, id);
			return this.getFirstRow();
			
		} catch (SQLException ex) {
			LOG.fatal(ex);
			throw(ex);
		} finally {
			this.release();
		}
	}
	
	public T selectByUniqueKey(String sqlStmt, Object[] columns) throws SQLException {
		try {
			if ( null == this.con) {
				this.con = (null == poolName) ? PoolFactory.getDefaultPool().getConnection() : 
					PoolFactory.getInstance().getPool(poolName, false).getConnection();
			}
			this.prepare(sqlStmt, columns);
			return this.getFirstRow();
		} catch (SQLException ex) {
			LOG.fatal(ex);
			throw(ex);
		} finally {
			this.release();
		}
	}
	

	protected void prepare(String sqlStmt) throws SQLException {
		if ( LOG.isDebugEnabled()) {
			LOG.debug("Sql=" + sqlStmt);
		}
		this.stmt = this.con.createStatement() ;
		this.rs = this.stmt.executeQuery(sqlStmt) ;
	}
		
	protected void prepareForPrimaryKey(String sqlStmt, Object id) throws SQLException {
		this.prepareStmt = this.con.prepareStatement(sqlStmt);
		this.prepareStmt.setObject(1, id) ;

		if ( LOG.isDebugEnabled()) {
			LOG.debug("Sql statement\n " + sqlStmt + "\n id=" + id);
		}
		this.rs = this.prepareStmt.executeQuery() ;
	}

	protected void prepare(String sqlStmt, Object[] columns ) throws SQLException {
		this.prepareStmt = this.con.prepareStatement(sqlStmt);
		for ( int i=1; i  <= columns.length; i++ ) {
			this.prepareStmt.setObject(i,columns[i-1]) ;
		}
		this.rs = this.prepareStmt.executeQuery() ;
	}
	
	protected void prepare(String sqlStmt, List<Object> columns ) throws SQLException {
		this.prepareStmt = this.con.prepareStatement(sqlStmt);
		int colsT = columns.size();
		for ( int i=1; i  <= colsT; i++ ) {
			this.prepareStmt.setObject(i,columns.get(i-1)) ;
		}
		this.rs = this.prepareStmt.executeQuery() ;
	}	

	/**
	 * Close everything...
	 */
	protected void release() {
	    
		AutoCloseable[] resources = ( isTxn ) ?  
			new AutoCloseable[] {this.rs, this.prepareStmt, this.stmt} : 
			new AutoCloseable[] {this.rs, this.prepareStmt, this.stmt, this.con};
			
		for (AutoCloseable resource : resources ) {
			if (resource != null ) {
		        try {
		        	resource.close();
		            resource = null;
		        } catch (Exception ex) {
		        	LOG.warn("Error while closing " , ex);
		        }
		    }
		}
	}
	
	/**
	 * Stored procedure executions.
	 * @return
	 * @throws SQLException
	 */
	public List<T> execute(String spStmt, List<Object> inParams, List<StoredProcOutParam> outParams) throws SQLException {
		try {
			if ( null == this.con) {
				this.con = (null == poolName) ? PoolFactory.getDefaultPool().getConnection() : 
					PoolFactory.getInstance().getPool(poolName, false).getConnection();
			}
			LOG.debug("Statement is : " +spStmt);
			this.callableStmt = this.con.prepareCall(spStmt);
			
			int size = inParams.size();
			for ( int i=1; i  <= size; i++ ) {
				this.callableStmt.setObject(i,inParams.get(i-1)) ;
			}
			
			if( null != outParams ) {
				for (StoredProcOutParam outParam : outParams) {
					this.callableStmt.registerOutParameter(outParam.outPramIndex, outParam.outPramType);
				}
			}
			
			this.callableResponse = this.callableStmt.execute();
			return this.populate();
			
		} catch (SQLException ex) {
			ex.printStackTrace();
			LOG.fatal(ex);
			throw(ex);
		} finally {
			this.release();
		}
	}	

	protected abstract List<T> populate() throws SQLException;
	protected abstract T getFirstRow() throws SQLException;
}


