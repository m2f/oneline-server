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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.bizosys.oneline.management.MetricAvgRollup;
import com.bizosys.oneline.management.MetricAvgRollupGate;

public class Pool implements IPool{

	private DbConfig config = null;
    private Stack<PoolConnection> availablePool = new Stack<PoolConnection>();
    private Stack<PoolConnection> destroyPool =  new Stack<PoolConnection>();
    
    private final Timer AGENT_TIMER = new Timer(true);
    private HealthCheckAgent hcAgent = null;
    
    private AtomicInteger activeConnection = new AtomicInteger(0);
    private AtomicInteger createdConnection = new AtomicInteger(0);
    private AtomicInteger destroyedConnection = new AtomicInteger(0);

    private String poolType;
    private final static Logger LOG = Logger.getLogger(Pool.class);

    MetricAvgRollupGate monitorReq = MetricAvgRollupGate.getInstance();
    MetricAvgRollup mr = null;
    
    public Pool(String poolType) 
    {
    	this.poolType = poolType;
    	mr = new MetricAvgRollup(this.poolType.toUpperCase());
    	MetricAvgRollupGate.getInstance().register(mr);
	}
    
	private synchronized PoolConnection createConnection()
	{
		if (this.config == null) return null;
		
		try {
			Class.forName(this.config.driverClass).newInstance();
		} catch (Exception ex) {
			LOG.fatal( "Could not load the driver class, " + this.config.driverClass, ex);
			return null;
		}
		
		if (LOG.isDebugEnabled()) LOG.debug("Creating a new connection for " + this.poolType);
		boolean isSucess = true;
		try
		{
			monitorReq.onEnter(mr);
			
			PoolConnection poolCon = null;
			Connection con = null;
			if ( this.config.allowMultiQueries ) 
			{
				Properties props = new Properties();
				props.put("allowMultiQueries", "true");
				props.put("user", this.config.login);
				props.put("password", this.config.password);
				con = DriverManager.getConnection(this.config.connectionUrl, props);
			} 
			else 
			{
				con = DriverManager.getConnection(this.config.connectionUrl, 
						this.config.login, this.config.password);
			}
			
			try{
				//presto throws exception here
				con.setTransactionIsolation(this.config.isolationLevel);				
			} catch (Exception ex){
				LOG.warn(ex.getLocalizedMessage());
			}

			poolCon = new PoolConnection (con,this.poolType );
			this.activeConnection.incrementAndGet();
			this.createdConnection.incrementAndGet();
			return poolCon;
		} 
		catch (SQLException ex) 
		{
			isSucess = false;
			LOG.fatal( ("Error in accessing database, " + 
				"\tjdbcurl:" + this.config.connectionUrl + 
				"\tlogin:" + this.config.login + 
				"\tpasswd:" + this.config.password), 
				ex);
			return null;
		} 
		catch (Exception ex) 
		{
			isSucess = false;
			LOG.fatal("Error in creating connection", ex);
			return null;
		} finally {
			monitorReq.onExit(mr, isSucess);			
		}
	}
	
	private void increment(int count) 
	{
		if (LOG.isDebugEnabled()) LOG.debug("Incrementing Connections by " + count);
		for (int i=0; i < count; i++)
		{
			try 
			{
				PoolConnection con = this.createConnection();
				if ( null != con ) con.close(); //Returns to the stack
				Thread.sleep(this.config.timeBetweenConnections); 
			} 
			catch (Exception ex) 
			{
				LOG.fatal("Error in creating connection", ex);
			}
		}
    }
    
	/**
	 * This is explicitly called when people call Connection.close().
	 * Make sure no other place it is called.
	 * @param returnedConnection
	 * @throws IllegalStateException
	 */
	public void returnConnection(Connection returnedConnection) throws SQLException
	{
		PoolConnection poolConnection = null;
		if (  returnedConnection instanceof PoolConnection) {
			poolConnection = (PoolConnection) returnedConnection;
		}
		
		if ( null != poolConnection )
		{
			try{
				//presto throws exception here
				poolConnection.setTransactionIsolation(this.config.isolationLevel);
				poolConnection.setAutoCommit(true);				
			} catch (Exception ex){
				LOG.warn(ex.getLocalizedMessage());
			}

			this.poolReturn(poolConnection);
		}
	}
	

	/**
	 * This gives a connection from pool. If nothing is in the pool,
	 * it creates one and gives back. 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection con = this.getNTestConnection(); //First try
		if ( null != con ) return con;
		
		LOG.debug("Trying second time..");
		con = this.getNTestConnection(); //Second try
		if ( null != con ) return con;
		
		LOG.debug("Trying third time..");
		con = this.getNTestConnection(); //Third try
		if ( null != con ) return con;

		LOG.fatal("Database is not available");
		throw new SQLException ("NO_CONNECTION");
	}
	
	private Connection getNTestConnection() throws  SQLException {

		/** Step # 1  Get a connection */
		PoolConnection poolCon = null;
    	if (! this.availablePool.empty() ){
    		poolCon = this.poolGet();
    	}
    	if ( null == poolCon ) {
    		poolCon = this.createConnection();
    	}
    	
		/** Step # 2  Test for the null, closed and dirty connection*/
    	boolean goodConnection = true;
    	if ( poolCon == null ) { 
    		LOG.debug("Pool connection gave a null conncetion");
    		goodConnection = false;
    		throw new SQLException("Could not make connection to database " + this.config.connectionUrl);
    	}
    	if ( poolCon.isClosed() ) {
    		LOG.debug("Pool connection is already closed");
    		goodConnection = false;
    	}
    	if ( poolCon.isDirty() ) {
    		LOG.debug("Pool connection is dirty.");
    		goodConnection = false;
    	}

		/** Step # 3  Give a null connection and destroy the bad connections.*/
    	if ( goodConnection ) 
    	{
    		return poolCon;
    	} 
    	else 
    	{
			try 
			{
				this.destroyPoolConnection(poolCon);
			} 
			catch (Exception ex) 
			{
				LOG.error("Potential connection leakage. check the root cause ..", ex);
			}
			return null;
    	}
	}
    
    public int getActiveConnection() 
    {
    	return this.activeConnection.get();
    }
    
    public int getAvailableConnection() 
    {
    	return this.availablePool.size();
    }

    private synchronized void poolReturn(PoolConnection poolConnection) 
    {
    	if ( ! this.availablePool.contains(poolConnection)) {
    		this.availablePool.push(poolConnection);
			this.activeConnection.decrementAndGet();
    	}
    	if ( LOG.isDebugEnabled()) LOG.debug("Connections Created/Using/InPool/Destroyed/Hashcode = " + 
    			this.createdConnection.get() + "/" + this.getActiveConnection() + "/" + this.getAvailableConnection()  + "/" + this.destroyedConnection.get() + "/" + this.hashCode());
	}

	private synchronized PoolConnection poolGet() 
	{
		this.activeConnection.incrementAndGet();
		return this.availablePool.pop();
    }

    public synchronized void start(DbConfig dbConfig) 
    {
    	if ( null == dbConfig) 
    	{
    		String errMsg = "Null db configuration file provided."; 
    		LOG.fatal(errMsg + ":" + dbConfig);
    		throw new RuntimeException(errMsg);
    	} 
    	this.config = dbConfig;
    	LOG.debug("Starting the database service for " + this.config.poolName);

    	try 
    	{
			Class.forName(this.config.driverClass).newInstance();
			LOG.debug("Driver instantiated with " + this.config.driverClass);
	        this.availablePool.ensureCapacity( this.config.idleConnections );
	        this.healthCheck();
		} 
    	catch (Exception ex) 
		{
			LOG.fatal("Pool creation issues.", ex);
		} 

    	this.hcAgent = new HealthCheckAgent();
    	
		LOG.debug("Health check Timer pause/duration in ms = " + 
				this.config.timeBetweenConnections + "/" + this.config.healthCheckDurationMillis);
    	
    	AGENT_TIMER.schedule(this.hcAgent, this.config.timeBetweenConnections, this.config.healthCheckDurationMillis);
    }
    
    public synchronized void stop() 
    {
    	LOG.debug("Stoping the database service");
    	if ( null != this.hcAgent ) 
    	{
    		this.hcAgent.cancel();
    		this.hcAgent = null;
    	}
    }

    public synchronized void stop(boolean releaseConnection) 
    {
    	LOG.debug("Stoping the database service");
    	if ( null != this.hcAgent ) 
    	{
    		this.hcAgent.cancel();
    		this.hcAgent = null;
    	}
    	
    	if(releaseConnection && this.availablePool.size() > 0){
    		while( this.availablePool.size() > 0){
    			this.poolGet().destroySilently();
    		}
    	}
    }

    
    //--------------Health Check Module-------------------
    public synchronized void healthCheck() {
    	if ( LOG.isDebugEnabled()) 
    	{
    		LOG.debug("Connections Available/Total = " + this.getAvailableConnection() + "/" + this.getActiveConnection());
    	}
		
    	if ( ! this.healthAddConnections() ) 
    	{
    		this.healthRemoveConnections();
    	}
    	this.healthRefreshConnections();
    }
    
    private boolean healthAddConnections() 
    {
    	boolean canIncrease = this.activeConnection.get() < this.config.maxConnections;
    	int availablePoolT = this.availablePool.size();
        
    	boolean isLess = availablePoolT < this.config.idleConnections;
    	boolean toAdd = isLess &&  canIncrease;	
    	
    	if ( toAdd ) this.increment(this.config.incrementBy);
    	return toAdd;
    }
    
    private void healthRemoveConnections() {

		//Old destroy pool remove everything.
		int destroySize = this.destroyPool.size();
		for ( int i=0; i< destroySize ; i++ ) {
			PoolConnection con = this.destroyPool.pop();
			if (con == null ) break;
			this.destroyPoolConnection(con);
		}

		//Current pool take care.
		while ( ( this.availablePool.size() - this.config.idleConnections ) > 0 ) {
			
			PoolConnection con = null;
			try {
				con = (PoolConnection) this.getConnection();
			} catch (SQLException ex) {
				LOG.fatal("Error in destroying connection", ex);
				continue;
			}
			this.destroyPool.push(con);
		}
		
    }
    
    private void healthRefreshConnections() 
    {
    	if (!this.config.testConnectionOnIdle) return;

    	int availablePoolT = this.getAvailableConnection();
    	
		if (this.config.runTestSql) {
			this.runTestSqlOnConnections(availablePoolT);
		}
    }

	private void runTestSqlOnConnections(int availablePoolT)
	{
		if ( LOG.isDebugEnabled()) LOG.debug(
				"Testing idle connections from pool : " + this.config.poolName + 
				" , Total connections " + availablePoolT + "  , using sql > " + this.config.testSql);
		
		int goodConnections = 0;
		
		for ( int i=0; i < availablePoolT; i++ ) 
		{
			PoolConnection connection = null;
			Statement stmt = null;
			ResultSet rs = null;
			try 
			{
				connection = (PoolConnection) this.getConnection();
				if (connection != null)
				{
					stmt = connection.createStatement() ;
					rs = stmt.executeQuery(this.config.testSql);
					rs.close(); rs = null; 
					stmt.close(); stmt = null;
					connection.close(); connection = null;
					goodConnections++;
				}
			} 
			catch (SQLException ex) 
			{
				LOG.warn("Exception in testing connections.", ex);
				if ( null != rs ) try { rs.close(); } catch (Exception rex) {}  
				if ( null != stmt ) try { stmt.close(); } catch (Exception sex) {}  
				if ( null != connection) connection.destroySilently();
			}
		}
		if ( LOG.isDebugEnabled()) LOG.debug(
			"Pool : " + this.config.poolName + " , Connection Status Good/Bad = " +  
			goodConnections + "/" + (availablePoolT - goodConnections));
		
	}
    
    private void destroyPoolConnection(Connection con) 
    {
		try 
		{
			if (  con instanceof PoolConnection) {
				PoolConnection poolCon = (PoolConnection) con;
				try { Thread.sleep(this.config.timeBetweenConnections); } 
				catch (InterruptedException ex) { LOG.fatal("Error in sleeping for destroy action",ex); }
				poolCon.destroy();
			} else {
				con.close();
			}
			con = null;
		} catch (SQLException ex) {
			this.destroyedConnection.incrementAndGet();
			LOG.fatal("Error in cleaning up connection", ex);
		}
    }
    
    private class HealthCheckAgent extends TimerTask {
        public void run() {
            try {
            	healthCheck();
            } catch(Exception e) {
    			LOG.fatal("Error in running health check", e);
            }
        }
    }    
    
    @Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Pool Connections : [");
		for(PoolConnection poolConnection: this.availablePool)
		{
			sb.append(poolConnection.hashCode()).append(", ");
		}
		sb.append(']');
		return sb.toString();
	}
}