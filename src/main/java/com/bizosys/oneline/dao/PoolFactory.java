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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bizosys.oneline.sql.JdbcConfigLoader;

public class PoolFactory 
{
	private static PoolFactory instance = null;
	private Map<String, IPool> poolMap;
	private IPool defaultPool;

	private final static Logger LOG = LogManager.getLogger(PoolFactory.class);

	public static PoolFactory getInstance()
	{
		if ( null != instance ) return instance;
		synchronized (PoolFactory.class.getName())
		{
			if ( null != instance ) return instance;
			instance = new PoolFactory();
		}
		return instance;
	}

	private PoolFactory() 
	{
		this.poolMap = new HashMap<String, IPool>();
	}

	public static IPool getDefaultPool()
	{
		return PoolFactory.getInstance().defaultPool;
	}

	public IPool getPool(String poolName,boolean isGCS)
	{
		if ( this.poolMap.containsKey(poolName) ) {
			return this.poolMap.get(poolName);
		} else {
			LOG.warn("Pool name not found :" + poolName + " . Switching to default pool");
			return PoolFactory.getInstance().defaultPool;
		}
	}

	public void returnConnection(Connection conn)
	{
		if (conn instanceof PoolConnection) this.returnConnection((PoolConnection) conn);
	}

	public void returnConnection(PoolConnection poolConn)
	{
		if ( null == poolConn ) return;
		try 
		{

			if (this.poolMap.containsKey(poolConn.poolName))
			{
				if (LOG.isDebugEnabled()) LOG.debug("Returning connection " + poolConn.hashCode() + " to pool " + poolConn.poolName);
				this.poolMap.get(poolConn.poolName).returnConnection(poolConn);
			} else {
				LOG.warn("Joombie connection to pool " + poolConn.poolName);
			}
		} 
		catch (SQLException e) 
		{
			LOG.error("Unable to return Pool Connection for pool type: " + poolConn.poolName, e);
		}
	}

	public boolean setup(String configXml)
	{
		try
		{
			List<DbConfig> dbcL = new JdbcConfigLoader().getConfiguration(configXml);

			if (dbcL != null && !dbcL.isEmpty())
			{
				for (DbConfig config : dbcL)
				{
					this.startPool(config);
				}
				return true;
			}
		}
		catch (Exception e)
		{
			LOG.error("Error in starting database service with config: " + configXml, e);
		}
		return false;
	}

	public boolean setup(DbConfig config)
	{
		try
		{
			this.startPool(config);
		}
		catch (Exception e)
		{
			LOG.error("Error in starting database service with config: " + config, e);
			return false;
		}
		return true;
	}

	public boolean stop()
	{
		if (this.poolMap == null || this.poolMap.isEmpty()) return true;
		for (IPool pool : this.poolMap.values())
		{
			pool.stop();
		}
		this.poolMap.clear();
		this.defaultPool = null;
		return true;
	}

	public final boolean stop(final boolean releaseConnection, final String poolName)
	{
		if( LOG.isDebugEnabled() ) 
			LOG.debug("PoolFactory stopping pool for [ " + poolName 
					+ " ] and releaseConnection is [ " + releaseConnection + " ]");

		if (this.poolMap == null || this.poolMap.isEmpty()) return true;
		IPool pool = this.poolMap.get(poolName); 
		if (null == pool) return false;

		pool.stop(releaseConnection);
		this.poolMap.remove(poolName);

		return true;
	}

	public final boolean contains(final String poolName)
	{
		if (this.poolMap == null || this.poolMap.isEmpty()) return false;
		return this.poolMap.containsKey(poolName); 
	}

	private synchronized void startPool(DbConfig config)
	{
		String poolName = config.getPoolName();
		LOG.info("Initializing DB Pool - " + poolName);
		if(this.poolMap.containsKey(poolName)) {
			LOG.info("Pool is already started Ignoring - " + poolName);
			return;
		}

		IPool pool = new Pool(poolName);
		this.poolMap.put(poolName, pool);
		pool.start(config);
		if (this.defaultPool == null && config.isDefaultPool()) this.defaultPool = pool;
		LOG.info("Created Pools are - " + this.toString());
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(String poolname: this.poolMap.keySet())
		{
			IPool pool = this.poolMap.get(poolname);
			sb.append(poolname).append("=> Pool:").append(pool.hashCode()).append("=").append(pool.toString()).append(", ");
		}
		return sb.toString();
	}

}