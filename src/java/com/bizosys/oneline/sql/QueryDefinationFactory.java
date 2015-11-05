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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.bizosys.oneline.model.AppConfig;
import com.bizosys.oneline.model.AppConfigTableExt;


public class QueryDefinationFactory {
	
	private final static Logger LOG = Logger.getLogger(QueryDefinationFactory.class);
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	
	public static Map<String, AppConfig> queryM = new HashMap<String, AppConfig>();
	static Pattern VAR_PATTERN = Pattern.compile("@[a-zA-Z0-9_$]*", Pattern.CASE_INSENSITIVE);
	static Pattern CUSTOM_VAR_PATTERN = Pattern.compile("&[a-zA-Z0-9_$]*", Pattern.CASE_INSENSITIVE);

	public static synchronized boolean refreshQueries()
	{

		queryM.clear();
		if ( DEBUG_ENABLED ) LOG.debug("Refreshing queries...");
		try 
		{
			List<AppConfig> queryL = AppConfigTableExt.selectAll();
			List<String> vars =  new ArrayList<String>();
			List<String> customVars =  new ArrayList<String>();

			for (AppConfig query : queryL)
			{
				query.body = getVariables(query.body, vars, customVars);
				
				if( vars.size() > 0 ) {
					query.setVars(vars);
					vars.clear();
				}
				
				if( customVars.size() > 0 ) {
					query.setCustomVars(customVars);
					customVars.clear();
				}
				
				queryM.put(query.title.trim(), query);
			}
			if ( DEBUG_ENABLED ) LOG.debug("Queries Refreshed, total fetched : " + queryM.size());
			return true;
		} 
		catch (SQLException e) 
		{
			LOG.error("Error in refreshing queries.", e);
			return false;
		}
	}
	
	public static String getVariables( String query , List<String> vars, List<String> customVars){
		if(query.indexOf('@') > 0){
			Matcher matcher = VAR_PATTERN.matcher(query);
			if( null != vars ){
				while(matcher.find()){
					vars.add(query.substring(matcher.start() + 1, matcher.end()));
				}
			}
			query = matcher.replaceAll("?");
		}
		
		if(query.indexOf('&') > 0) {
			Matcher matcher = CUSTOM_VAR_PATTERN.matcher(query);
			if( null != customVars ){
				StringBuffer sb = new StringBuffer(query.length());
				while(matcher.find()){
					customVars.add(query.substring(matcher.start() + 1, matcher.end()));
					matcher.appendReplacement(sb, matcher.group() + "&");
				}
				matcher.appendTail(sb);
				query = sb.toString();
			}
		}
		return query;
	}
}
