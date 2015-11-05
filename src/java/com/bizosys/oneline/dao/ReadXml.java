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

import java.io.PrintWriter;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ReadXml<T> extends ReadBase<String> {

	public String docName = null; 

	private final static Logger LOG = Logger.getLogger(ReadXml.class);
	
	private PrintWriter out = null;
	private Boolean writeAttributes = Boolean.FALSE;
	private Class<T> classToFill = null;
	
	public ReadXml(Class<T> classToFill)
	{
		super();
		this.classToFill = classToFill;
	}
	
	public ReadXml(PrintWriter out, Class<T> classToFill) 
	{
		this(classToFill);
		this.out = out;
	}

	public ReadXml(Boolean writeAttributes, Class<T> classToFill) 
	{
		this(classToFill);
		this.writeAttributes = writeAttributes;
	}

	public ReadXml(PrintWriter out, Boolean writeAttributes, Class<T> classToFill) 
	{
		this(classToFill);
		this.out = out;
		this.writeAttributes = writeAttributes;
	}
	
	@Override
	protected List<String> populate() throws SQLException {
		if ( this.rs == null ) {
			throw new SQLException("Rs is not initialized.");
		}

		ResultSetMetaData md = rs.getMetaData() ;
		int totalCol = md.getColumnCount();
		String[] cols = new String[totalCol];
		int[] types = new int[totalCol];
		
		for ( int i=0; i<totalCol; i++ ) 
		{
			cols[i] = md.getColumnLabel(i+1);
			types[i] = md.getColumnType(i+1);
		}
		
		List<String> records = null;
		StringBuilder strBuf = new StringBuilder();
		String className = null;
		if ( null != docName ) className = docName;
		else className = classToFill.getName();

		if (this.out == null) {
			records = new ArrayList<String>();
		}

		while (this.rs.next()) {
			if (this.writeAttributes) {
				this.recordAsAttributes(totalCol, cols, strBuf, className);
			} else {
				this.recordAsTags(totalCol, cols, types, strBuf, className);
			}

			if ( LOG.isDebugEnabled()) LOG.debug(strBuf.toString());
			
			if (this.out == null) {
				records.add(strBuf.toString());
			} else {
				this.out.println(strBuf.toString());
			}
				
			strBuf.delete(0, strBuf.length());
		}
		return records;		
	}

	@Override
	protected String getFirstRow() throws SQLException {
		if ( this.rs == null ) {
			throw new SQLException("Rs is not initialized.");
		}

		ResultSetMetaData md = rs.getMetaData() ;
		int totalCol = md.getColumnCount();
		String[] cols = new String[totalCol];
		int[] types = new int[totalCol];
		for ( int i=0; i<totalCol; i++ ) 
		{
			cols[i] = md.getColumnLabel(i+1);
			types[i] = md.getColumnType(i+1);
		}
		
		StringBuilder strBuf = new StringBuilder();
		String className = null;
		if ( null != docName ) className = docName;
		else className = classToFill.getName();

		if (! this.rs.next()) return null; 
		
		if (this.writeAttributes) {
			this.recordAsAttributes(totalCol, cols, strBuf, className);
		} else {
			this.recordAsTags(totalCol, cols, types, strBuf, className);
		}

		if ( LOG.isDebugEnabled()) LOG.debug(strBuf.toString());
		String xmlRec = strBuf.toString();
		if (this.out != null) this.out.println(xmlRec);
		return xmlRec;
	}	

	private void recordAsTags(int totalCol, String[] cols, int[] types,
		StringBuilder strBuf, String className) throws SQLException {
		
		strBuf.append('<').append(className).append(">\n");

		for ( int i=0; i<totalCol; i++ )  {
			Object obj = rs.getObject(i+1);
			if ( null == obj) continue;
			
			strBuf.append('<').append(cols[i]).append('>');
			switch (types[i]) {
			case java.sql.Types.VARCHAR:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.NCHAR:
			case java.sql.Types.CHAR:
				strBuf.append("<![CDATA[").append(rs.getObject(i+1).toString()).append("]]>");
				break;
			default:
				strBuf.append(rs.getObject(i+1).toString());
				break;
			}
			strBuf.append("</").append(cols[i]).append(">\n");
		}

		strBuf.append("</").append(className).append(">\n");
	}
	
	private void recordAsAttributes(int totalCol, String[] cols,  
		StringBuilder strBuf, String className) throws SQLException	{

		strBuf.append('<').append(className).append(" \n");
		for ( int i=0; i<totalCol; i++ ) {
			Object obj = rs.getObject(i+1);
			if ( null == obj) continue;
			
			strBuf.append(cols[i]).append('=');
			strBuf.append(rs.getObject(i+1).toString()).append(' ');
		}

		strBuf.append("/>\n");
	}
}
