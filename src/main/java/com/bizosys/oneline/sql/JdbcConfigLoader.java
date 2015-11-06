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

import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bizosys.oneline.dao.DbConfig;

public class JdbcConfigLoader extends DefaultHandler {

	List<DbConfig> configs = null;
	DbConfig running = null;
	StringBuilder sb = new StringBuilder();
	
	public List<DbConfig> getConfiguration(final String xmlString) throws ParseException{
		if ( null != configs) return configs;
		configs = new ArrayList<DbConfig>();
		
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try {

			saxParser = saxFactory.newSAXParser();
			saxParser.parse(new InputSource(new StringReader(xmlString)), this);
			
			return configs;
			
		} catch (Exception e) {
			ConfiguationLog.l.fatal("Schema: " + "\n" + xmlString, e );
			throw new ParseException(e.getMessage(), 0);
		} 
	}

	public void startElement(String uri, String localName, String qName,Attributes attributes) throws SAXException {

		sb.setLength(0);
		if (qName.equalsIgnoreCase("pool")) {
			try {
				running = new DbConfig();
			} catch (Exception e) {
				throw new SAXException(e);
			}
		}
		
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (qName.equalsIgnoreCase("pool")) {
			configs.add(this.running);
			running = null;
			
		} else if (qName.equalsIgnoreCase("poolName")) {
			running.setPoolName(sb.toString());
		} else if (qName.equalsIgnoreCase("driverClass")) {
			running.setDriverClass(sb.toString());
		} else if (qName.equalsIgnoreCase("connectionUrl")) {
			running.setConnectionUrl(sb.toString());
		} else if (qName.equalsIgnoreCase("login")) {
			running.setLogin(sb.toString());
		} else if (qName.equalsIgnoreCase("password")) {
			running.setPassword(sb.toString());
		} else if (qName.equalsIgnoreCase("idleConnections")) {
			running.setIdleConnections(Integer.parseInt(sb.toString()));
		} else if (qName.equalsIgnoreCase("maxConnections")) {
			running.setMaxConnections(Integer.parseInt(sb.toString()));
		} else if (qName.equalsIgnoreCase("incrementBy")) {
			running.setIncrementBy(Integer.parseInt(sb.toString()));
		} else if (qName.equalsIgnoreCase("testConnectionOnBorrow")) {
			running.setTestConnectionOnBorrow(Boolean.parseBoolean(sb.toString()));
		} else if (qName.equalsIgnoreCase("testConnectionOnIdle")) {
			running.setTestConnectionOnIdle(Boolean.parseBoolean(sb.toString()));
		} else if (qName.equalsIgnoreCase("testConnectionOnReturn")) {
			running.setTestConnectionOnReturn(Boolean.parseBoolean(sb.toString()));
		} else if (qName.equalsIgnoreCase("healthCheckDurationMillis")) {
			running.setHealthCheckDurationMillis(Integer.parseInt(sb.toString()));
		} else if (qName.equalsIgnoreCase("timeBetweenConnections")) {
			running.setTimeBetweenConnections(Integer.parseInt(sb.toString()));
		} else if (qName.equalsIgnoreCase("isolationLevel")) {
			running.setIsolationLevel(Integer.parseInt(sb.toString()));
		} else if (qName.equalsIgnoreCase("allowMultiQueries")) {
			running.setAllowMultiQueries(Boolean.parseBoolean(sb.toString()));
		} else if (qName.equalsIgnoreCase("testSql")) {
			running.setTestSql(sb.toString());
		} else if (qName.equalsIgnoreCase("runTestSql")) {
			running.setRunTestSql(Boolean.parseBoolean(sb.toString()));
		} else if (qName.equalsIgnoreCase("defaultPool")) {
			running.setDefaultPool(Boolean.parseBoolean(sb.toString()));
		} else if (qName.equalsIgnoreCase("preparedStmt")) {
			running.setPreparedStmt(Boolean.parseBoolean(sb.toString()));
		}
		
	}	
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		sb.append(new String(ch, start, length));
	}
}
