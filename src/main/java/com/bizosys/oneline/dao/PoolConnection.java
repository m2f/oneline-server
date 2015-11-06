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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
import java.sql.Struct;
import java.sql.Clob;
import java.util.Properties;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLXML;
*/

public class PoolConnection implements Connection {
	
	private Connection baseCon = null;
	private boolean isDirty = false;
	public String poolName;

	public PoolConnection(Connection con, String poolType) {
		this.baseCon = con;
		this.poolName = poolType;
		if ( this.baseCon == null ) throw new IllegalStateException("NO_CONNECTION");
	}
	
	public void clearWarnings() throws SQLException {
		this.baseCon.clearWarnings();
	}

	public void close() throws SQLException {
		this.isDirty = false;
		PoolFactory.getInstance().returnConnection(this);
	}
	
	public boolean isDirty() {
		return this.isDirty; 
	}

	public void destroy() throws SQLException {
		if ( isDirty ) throw new SQLException ("BUSY_CONNECTION");
		if ( this.baseCon == null ) throw new SQLException ("NULL_BASE_CONNECTION"); 
		if ( ! this.baseCon.isClosed()) this.baseCon.close();
		this.baseCon = null;
	}
	
	public void destroySilently() {
		if ( null == this.baseCon ) return;

		try { if ( this.baseCon.isClosed() ) return; } catch (SQLException e1) {}
		
		try {
			this.baseCon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void commit() throws SQLException {
		this.baseCon.commit();
	}

	public Statement createStatement() throws SQLException {
		this.isDirty = true;
		return this.baseCon.createStatement();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {

		this.isDirty = true;
		return this.baseCon.createStatement(resultSetType,resultSetConcurrency);
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {

		this.isDirty = true;
		return this.baseCon.createStatement(resultSetType,resultSetConcurrency,resultSetHoldability);
	}

	public boolean getAutoCommit() throws SQLException {
		return this.baseCon.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		return this.baseCon.getCatalog();
	}

	public int getHoldability() throws SQLException {
		return this.baseCon.getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return this.baseCon.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		return this.baseCon.getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return this.baseCon.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		return this.baseCon.getWarnings();
	}

	public boolean isClosed() throws SQLException {
		return this.baseCon.isClosed();
	}

	public boolean isReadOnly() throws SQLException {
		return this.baseCon.isReadOnly();
	}

	public String nativeSQL(String sql) throws SQLException {
		return this.baseCon.nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return this.baseCon.prepareCall(sql);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {

		this.isDirty = true;
		return this.baseCon.prepareCall(sql,resultSetType,resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {

		this.isDirty = true;
		return this.baseCon.prepareCall(sql,resultSetType,resultSetConcurrency,resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {

		this.isDirty = true;
		return this.baseCon.prepareStatement(sql);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {

		this.isDirty = true;
		return this.baseCon.prepareStatement(sql,autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {

		this.isDirty = true;
		return this.baseCon.prepareStatement(sql,columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {

		this.isDirty = true;
		return this.baseCon.prepareStatement(sql,columnNames);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {

		this.isDirty = true;
		return this.baseCon.prepareStatement(sql,resultSetType,resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {

		this.isDirty = true;
		return this.baseCon.prepareStatement(sql,resultSetType,resultSetConcurrency,resultSetHoldability);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		this.baseCon.releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException {
		this.baseCon.rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		this.baseCon.rollback(savepoint);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		this.baseCon.setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException {
		this.baseCon.setCatalog(catalog);
	}

	public void setHoldability(int holdability) throws SQLException {
		this.baseCon.setHoldability(holdability);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		this.baseCon.setReadOnly(readOnly);
	}

	public Savepoint setSavepoint() throws SQLException {
		return this.baseCon.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return this.baseCon.setSavepoint(name);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		this.baseCon.setTransactionIsolation(level);
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		this.baseCon.setTypeMap(map);
	}

	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Blob createBlob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Clob createClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public NClob createNClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLXML createSQLXML() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getClientInfo() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getClientInfo(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isValid(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void abort(Executor arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getSchema() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSchema(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}


	/*
	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		return this.baseCon.createArrayOf(arg0, arg1);
	}

	public Blob createBlob() throws SQLException {
		return this.baseCon.createBlob();
	}

	public Clob createClob() throws SQLException {
		return this.baseCon.createClob();
	}

	public NClob createNClob() throws SQLException {
		return this.baseCon.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return this.baseCon.createSQLXML();
	}

	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		return this.baseCon.createStruct(arg0,arg1);
	}

	public Properties getClientInfo() throws SQLException {
		return this.baseCon.getClientInfo();
	}

	public String getClientInfo(String arg0) throws SQLException {
		return this.baseCon.getClientInfo(arg0);
	}

	public boolean isValid(int arg0) throws SQLException {
		return this.baseCon.isValid(arg0);
	}

	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		this.baseCon.setClientInfo(arg0);
	}

	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
		this.baseCon.setClientInfo(arg0, arg1);
	}

	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return this.baseCon.isWrapperFor(arg0);
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return this.baseCon.unwrap(arg0);
	}
	*/
}
