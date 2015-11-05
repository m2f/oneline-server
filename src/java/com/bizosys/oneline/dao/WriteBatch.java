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

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

public class WriteBatch extends WriteBase 
{
    private final static Logger LOG = Logger.getLogger(WriteBatch.class);

    /**
     * To execute a batch of updates or inserts onto a single table.
     * @param query - For all records to execute with
     * @param records - List of column values for each record. List of Object[] objects
     * @return int[] - rows updated per record
     * @throws SQLException
     */
    public int[] executeBatch(String query, List<Object[]> records) throws SQLException
    {
    	this.startBatch(query);
    	for (Object[] record : records)
    	{
    		this.addToBatch(record);
    	}
    	return this.executeBatch();
    }
    
	private void startBatch(String query) throws SQLException
	{
	    try 
	    {
			this.beginTransaction();
	    	this.createConnection(true);
			this.prepareStmt = this.con.prepareStatement(query);
		} 
	    catch (SQLException ex) 
	    {
			LOG.fatal(ex);
			throw ex;
		}
	}

	private void addToBatch(Object[] columns) throws SQLException
	{
		if (this.prepareStmt == null)
		{
			throw new SQLException("Illegal call. startBatch has to be done before addToBatch");
		}

		int columnLength = columns.length;
		for ( int i=1; i  <= columnLength; i++ ) 
		{
			this.prepareStmt.setObject(i,columns[i-1]) ;
		}
		this.prepareStmt.addBatch();
	}

	private int[] executeBatch() throws SQLException
	{
		if (this.prepareStmt == null)
		{
			throw new SQLException("Illegal call. startBatch has to be done before addToBatch");
		}
	    try 
	    {
			int[] results = this.prepareStmt.executeBatch();
			this.commitTransaction();
			this.prepareStmt.clearBatch();
			return results;
		} 
	    catch (SQLException ex) 
	    {
			LOG.fatal(ex);
			this.rollbackTransaction();
			throw ex;
		} 
	    finally 
	    {
			this.releaseResources();
		}
	}
}
