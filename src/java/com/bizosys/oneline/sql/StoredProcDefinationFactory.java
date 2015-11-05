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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.bizosys.oneline.model.StoredProcConfig;
import com.bizosys.oneline.model.StoredProcConfigTable;
import com.bizosys.oneline.model.StoredProcOutParam;
import com.bizosys.oneline.util.ErrorCodeExp;
import com.bizosys.oneline.util.LineReaderUtil;


public class StoredProcDefinationFactory {
	
	private final static Logger LOG = Logger.getLogger(StoredProcDefinationFactory.class);
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	
	public static Map<String, StoredProcConfig> storedProcM = new HashMap<String, StoredProcConfig>();
	static Pattern VAR_PATTERN = Pattern.compile("@[a-zA-Z0-9]*", Pattern.CASE_INSENSITIVE);

	public static synchronized boolean refreshQueries() throws SQLException, ErrorCodeExp 
	{

		storedProcM.clear();
		if ( DEBUG_ENABLED ) LOG.debug("Refreshing stored procedures...");
		try 
		{
			List<StoredProcConfig> queryL = StoredProcConfigTable.selectAll();
			List<String> inputVars =  new ArrayList<String>();
			List<StoredProcOutParam> outVars = new ArrayList<StoredProcOutParam>();

			List<String> tempOut = new ArrayList<String>();
			int index = -1; 
			
			for (StoredProcConfig query : queryL)
			{
				query.spCallSyntax = getVariables(query.spCallSyntax, inputVars);
				
				/**
				 * Parse the out variable and register.
				 */
				if( query.spOutVar.trim().length() > 0) {
					LineReaderUtil.fastSplit(tempOut, query.spOutVar, ',');
					for (String keyNval : tempOut) {
						index = keyNval.indexOf('=');
						String outParamName = keyNval.substring(0, index).trim();
						int outPramIndex = inputVars.indexOf(outParamName) + 1;
						int outPramType = Integer.parseInt(keyNval.substring(index + 1).trim());
						outVars.add(new StoredProcOutParam(outParamName, outPramIndex, outPramType));
					}
					tempOut.clear();
				}
				if( query.spErrVar.trim().length() > 0) {
					int outPramIndex = inputVars.indexOf(query.spErrVar) + 1;
					StoredProcOutParam errorParam = new StoredProcOutParam(query.spErrVar, outPramIndex, Types.VARCHAR);
					errorParam.setError(true);
					query.setErrorParam(errorParam);
					outVars.add(errorParam);
				}
				
				if(outVars.size() > 0) {
					query.setOutVars(outVars);
					for (StoredProcOutParam out : outVars) {
						inputVars.remove(out.outParamName);
					}
					outVars.clear();
				}
				
				if(inputVars.size() > 0) {
					query.setVars(inputVars);
					inputVars.clear();
				}
				
				storedProcM.put(query.spTitle.trim(), query);
			}
			
			if ( DEBUG_ENABLED ) LOG.debug("Stored Proc Refreshed, total fetched : " + storedProcM.size());
			return true;
		} 
		catch (SQLException e) 
		{
			LOG.error("Error in refreshing Stored proc.", e);
			throw e; 
		}
	}
	
	public static String getVariables( String query , List<String> vars ){
		if(query.indexOf('@') > 0){
			Matcher matcher = VAR_PATTERN.matcher(query);
			if( null != vars ){
				while(matcher.find()){
					vars.add(query.substring(matcher.start() + 1, matcher.end()));
				}
			}
			query = matcher.replaceAll("?");
		}
		return query;
	}
}
