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

import java.sql.Connection;

public class DbConfig {

    private int idleConnections = 5;
    private int maxConnections = 100;
    private int incrementBy = 5;
    private boolean testConnectionOnBorrow = false;
    private boolean testConnectionOnIdle = true;
    private boolean testConnectionOnReturn = false;
    private long healthCheckDurationMillis = 29*60*1000;
    private String testSql = "select 1";
    private boolean runTestSql = true;
    private int timeBetweenConnections = 50;
    private int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
    private boolean allowMultiQueries = false;
	private String poolName = "configpool";
    private boolean defaultPool = true;
    private boolean isGCS = false;
    private boolean preparedStmt = true;
    
    private String driverClass;
	private String connectionUrl;
	private String login;  
	private String password;

	public DbConfig() {
	}

	public DbConfig(String driverClass, String connectionUrl, String login, String password) {
		this.driverClass = driverClass;
		this.connectionUrl = connectionUrl;
		this.login = login;
		this.password = password;
	}

	public int getIdleConnections() {
		return idleConnections;
	}


	public void setIdleConnections(int idleConnections) {
		this.idleConnections = idleConnections;
	}


	public int getMaxConnections() {
		return maxConnections;
	}


	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}


	public int getIncrementBy() {
		return incrementBy;
	}


	public void setIncrementBy(int incrementBy) {
		this.incrementBy = incrementBy;
	}


	public boolean isTestConnectionOnBorrow() {
		return testConnectionOnBorrow;
	}


	public void setTestConnectionOnBorrow(boolean testConnectionOnBorrow) {
		this.testConnectionOnBorrow = testConnectionOnBorrow;
	}


	public boolean isTestConnectionOnIdle() {
		return testConnectionOnIdle;
	}


	public void setTestConnectionOnIdle(boolean testConnectionOnIdle) {
		this.testConnectionOnIdle = testConnectionOnIdle;
	}


	public boolean isTestConnectionOnReturn() {
		return testConnectionOnReturn;
	}


	public void setTestConnectionOnReturn(boolean testConnectionOnReturn) {
		this.testConnectionOnReturn = testConnectionOnReturn;
	}


	public long getHealthCheckDurationMillis() {
		return healthCheckDurationMillis;
	}


	public void setHealthCheckDurationMillis(long healthCheckDurationMillis) {
		this.healthCheckDurationMillis = healthCheckDurationMillis;
	}


	public String getTestSql() {
		return testSql;
	}


	public void setTestSql(String testSql) {
		this.testSql = testSql;
	}


	public boolean isRunTestSql() {
		return runTestSql;
	}


	public void setRunTestSql(boolean runTestSql) {
		this.runTestSql = runTestSql;
	}


	public int getTimeBetweenConnections() {
		return timeBetweenConnections;
	}


	public void setTimeBetweenConnections(int timeBetweenConnections) {
		this.timeBetweenConnections = timeBetweenConnections;
	}


	public int getIsolationLevel() {
		return isolationLevel;
	}


	public void setIsolationLevel(int isolationLevel) {
		this.isolationLevel = isolationLevel;
	}


	public boolean isAllowMultiQueries() {
		return allowMultiQueries;
	}


	public void setAllowMultiQueries(boolean allowMultiQueries) {
		this.allowMultiQueries = allowMultiQueries;
	}


	public String getPoolName() {
		return poolName;
	}


	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}


	public boolean isDefaultPool() {
		return defaultPool;
	}


	public void setDefaultPool(boolean defaultPool) {
		this.defaultPool = defaultPool;
	}


	public boolean isGCS() {
		return isGCS;
	}


	public void setGCS(boolean isGCS) {
		this.isGCS = isGCS;
	}


	public boolean isPreparedStmt() {
		return preparedStmt;
	}


	public void setPreparedStmt(boolean preparedStmt) {
		this.preparedStmt = preparedStmt;
	}


	public String getDriverClass() {
		return driverClass;
	}


	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}


	public String getConnectionUrl() {
		return connectionUrl;
	}


	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}


	public String getLogin() {
		return login;
	}


	public void setLogin(String login) {
		this.login = login;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	@Override
	public String toString()
	{
		return "DbConfigModel [idleConnections=" + idleConnections
				+ ", maxConnections=" + maxConnections + ", incrementBy="
				+ incrementBy + ", testConnectionOnBorrow="
				+ testConnectionOnBorrow + ", testConnectionOnIdle="
				+ testConnectionOnIdle + ", testConnectionOnReturn="
				+ testConnectionOnReturn + ", healthCheckDurationMillis="
				+ healthCheckDurationMillis + ", testSql=" + testSql
				+ ", runTestSql=" + runTestSql + ", timeBetweenConnections="
				+ timeBetweenConnections + ", isolationLevel=" + isolationLevel
				+ ", allowMultiQueries=" + allowMultiQueries + ", poolName="
				+ poolName + ", driverClass=" + driverClass
				+ ", connectionUrl=" + connectionUrl + ", login=" + login
				+ ", password=" + password 
				+ ", isGCS=" + isGCS + "]";
	}
}
