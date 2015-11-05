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

public class DbConfig 
{
	
	public static boolean DEFAULT_PREPARE_STMT_SUPPORT = true;
	
    public int idleConnections = 5;
    public int maxConnections = 100;
    public int incrementBy = 5;
    public boolean testConnectionOnBorrow = false;
    public boolean testConnectionOnIdle = true;
    public boolean testConnectionOnReturn = false;
    public long healthCheckDurationMillis = 29*60*1000;
    public String testSql = "select 1";
    public boolean runTestSql = true;
    public int timeBetweenConnections = 50;
    public int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
    public boolean allowMultiQueries = false;
	public String poolName;
    public boolean defaultPool = true;
    public boolean isGCS = false;
    public boolean preparedStmt = true;
    
	public String driverClass;
	public String connectionUrl;
	public String login;  
	public String password;

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

	/*
	public void encrypt() throws Exception 
	{
		this.password = PasswordEncryptor.encrypt(this.password);
	}
	
	public void decrypt() throws Exception 
	{
		this.password = PasswordEncryptor.decrypt(this.password);
	}

	*/
	
	
}
