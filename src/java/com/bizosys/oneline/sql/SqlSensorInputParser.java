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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bizosys.oneline.model.AppConfig;
import com.bizosys.oneline.model.StoredProcConfig;
import com.bizosys.oneline.model.StoredProcOutParam;
import com.bizosys.oneline.util.ErrorCodeExp;
import com.bizosys.oneline.util.ErrorCodes;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SqlSensorInputParser {

	private final static Logger LOG = Logger.getLogger(SqlSensorInputParser.class);

	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	
	public static boolean fromJson(String queriesJson, List<UnitStep> queryUnits) throws ErrorCodeExp
	{
		Gson gson = new Gson();

		JsonElement jsonElem = gson.fromJson(queriesJson, JsonElement.class);
		JsonObject jsonObj = jsonElem.getAsJsonObject();
		Iterator<JsonElement> queriesIterator = jsonObj.get("queries").getAsJsonArray().iterator();
				
		while(queriesIterator.hasNext())
		{
			JsonObject queryObj = (JsonObject) queriesIterator.next();
						
			/**
			 * queryid
			 */
			if ( queryObj.has("queryid")) {
				String queryId = queryObj.get("queryid").getAsString().trim();
				addQuery(queryUnits, queryObj, queryId);
			} else if ( queryObj.has("functionid")) { 
				String functionId = queryObj.get("functionid").getAsString().trim();
				addFunction(queryUnits, queryObj, functionId);
			}else if ( queryObj.has("spid")) { 
				String spId = queryObj.get("spid").getAsString().trim();
				addSp(queryUnits, queryObj, spId);
			}
		}
		
		return true;
	}
	
	private static void addFunction(List<UnitStep> queryUnits, 
		JsonObject queryObj, String funcId) throws ErrorCodeExp {
		
		List<UnitStep> queryObject = FunctionDefinationFactory.functions.get(funcId);
		
		try {
			if (queryObject == null) {
				FunctionDefinationFactory.refreshFunctions();
				queryObject = FunctionDefinationFactory.functions.get(funcId);
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
 
		if (queryObject == null) {
			throw new ErrorCodeExp(funcId, ErrorCodes.FUNCTION_NOT_FOUND, 
								"Function not found :" + funcId, ErrorCodes.FUNCTION_KEY);
		}
		
		Boolean isRecursive = (queryObj.has("isRecursive") ) ?
								queryObj.get("isRecursive").getAsBoolean() : false;
			
		/**
		 * Single Expression
		 */
		UnitExpr expr = extractExpr(queryObj);
		
		/**
		 * Boolean Expression
		 */
		List<UnitExpr> andExprs = booleanExprs(queryObj, true);
		List<UnitExpr> orExprs = booleanExprs(queryObj, false);

		String variables = (queryObj.has("variables") ) ? "{variables:" + queryObj.get("variables").getAsJsonArray().toString() + "}": null;

		UnitFunction uf = new UnitFunction(funcId, variables, expr, andExprs, orExprs, isRecursive) ;
		uf.stepId  = funcId;
		uf.isFunc = true;
		
		queryUnits.add( uf );
	}	

	private static void addSp(List<UnitStep> queryUnits, 
			JsonObject queryObj, String spId) throws ErrorCodeExp {
		
		StoredProcConfig spObject = StoredProcDefinationFactory.storedProcM.get(spId);
		
		try {
			if (spObject == null) {
				StoredProcDefinationFactory.refreshQueries();
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
			 
		if (spObject == null) {
			throw new ErrorCodeExp(spId, ErrorCodes.STORED_PROC_NOT_FOUND, 
								"Sp not found :" + spId, ErrorCodes.SP_KEY);
		}
				

		String variables = (queryObj.has("variables") ) ? "{variables:" + queryObj.get("variables").getAsJsonArray().toString() + "}": null;
		
		List<Object> inParams = new ArrayList<Object>();
		List<StoredProcOutParam> queryOutVars = spObject.getOutVars();
		List<String> inputVars = spObject.getVars();
		

		UnitSp up = new UnitSp(spObject, variables,inParams,queryOutVars,inputVars) ;
		up.stepId  = spId;
		up.isFunc = false;
		queryUnits.add(up);
	}
	
	private static void addQuery(List<UnitStep> queryUnits,
			JsonObject queryObj, String queryId) throws ErrorCodeExp {
		
		AppConfig queryObject = QueryDefinationFactory.queryM.get(queryId);
		
		if (queryObject == null) {
			QueryDefinationFactory.refreshQueries();
			queryObject = QueryDefinationFactory.queryM.get(queryId);
		}
 
		if (queryObject == null) {
			throw new ErrorCodeExp ( queryId,  ErrorCodes.QUERY_NOT_FOUND, "Query Id " + queryId + " is not configured.", ErrorCodes.QUERY_KEY);
			
		}
		
		queryObject = queryObject.clone();
		
		/**
		 * Parameters
		 */
		List<Object> paramL = new ArrayList<Object>();
		if (queryObj.has("params") ) {
			JsonArray paramsArray =  queryObj.get("params").getAsJsonArray();
			int totalParam = paramsArray.size();
			for( int i = 0; i < totalParam; i++)
			{
				String paramVal = paramsArray.get(i).getAsString();
				if ( paramVal.length() > 0) {
					if ( paramVal.equals("__null") || paramVal.equals("null") )
						paramVal = null;
				}
				paramL.add(paramVal);
			}
		}
		
		/**
		 * where, sort, offset, limit
		 */
		String where = (queryObj.has("where") ) ? queryObj.get("where").getAsString() : null;
		String sort = (queryObj.has("sort") ) ? queryObj.get("sort").getAsString() : null;
		int offset = (queryObj.has("offset") ) ? queryObj.get("offset").getAsInt() : -1;
		int limit = (queryObj.has("limit") ) ? queryObj.get("limit").getAsInt() : -1;
		
		/**
		 * If there are any sequence ids to be generated then generate the ids 
		 * and add the generated sequenceids to the variables string.
		 */
		String variables = (queryObj.has("variables") ) ? 
					"{variables:" + queryObj.get("variables").getAsJsonArray().toString() + "}": null;
		
		if( DEBUG_ENABLED && variables != null ) LOG.debug("String variables are : " +variables);
		
		Boolean isRecursive = (queryObj.has("isRecursive") ) ? queryObj.get("isRecursive").getAsBoolean(): false;
		
		/**
		 * Single Expression
		 */
		UnitExpr expr = extractExpr(queryObj);
		
		/**
		 * Boolean Expression
		 */
		List<UnitExpr> andExprs = booleanExprs(queryObj, true);
		List<UnitExpr> orExprs = booleanExprs(queryObj, false);
		
		JsonElement sequenceElem = null;
		if(queryObj.has("sequences")) sequenceElem = queryObj.get("sequences");
		
		UnitQuery uq = new UnitQuery( queryObject, paramL, expr, andExprs, orExprs, where, 
						sort, offset, limit,variables,isRecursive, sequenceElem) ;
		uq.isFunc = false;
		uq.stepId = queryId;
		queryUnits.add(uq);
	}

	public static Map<String, String> generateSequences(JsonElement sequenceElem) throws SQLException 
	{
		JsonArray sequenceArray =  sequenceElem.getAsJsonArray();
		int totalSequence = sequenceArray.size();
		Map<String, String> ids = new HashMap<String, String>(totalSequence);
		for( int i = 0; i < totalSequence; i++)
		{
			JsonObject sequenceKeyName = sequenceArray.get(i).getAsJsonObject();
			String sequenceKey = sequenceKeyName.get("sequenceKey").getAsString();
			String sequenceName = sequenceKeyName.get("sequenceName").getAsString();
			int id = CachedIdGenerator.getInstance().generateId(sequenceKey);
			ids.put(sequenceName , Integer.toString(id));
		}
		return ids;
	}

	private static List<UnitExpr> booleanExprs(JsonObject queryObj, boolean flipAndOr) {
		
		String tag = ( flipAndOr ) ?  "andexprs" : "orexprs";
	
		if ( ! queryObj.has(tag)) return null;
		
		List<UnitExpr> andExprs = new ArrayList<>();
		Iterator expression = queryObj.get(tag).getAsJsonArray().iterator();
		
		while(expression.hasNext()){
			JsonObject exprObj = (JsonObject)expression.next();
			UnitExpr cluse = extractExpr(exprObj);
			if  ( null != cluse) {
				andExprs.add(cluse);
			}
		}	
		return andExprs;
	}
	
	
	private static UnitExpr extractExpr(JsonObject queryObj) {
		UnitExpr expr = null;
		if ( queryObj.has("expr")) {
			
			if ( queryObj.get("expr").isJsonNull() ) return expr;
			if ( !queryObj.get("expr").isJsonObject() ) return expr;
			
			JsonObject exprObj = queryObj.get("expr").getAsJsonObject();
			
			expr = new UnitExpr(exprObj.get("lhs").getAsString(),
					exprObj.get("rhs").getAsString(),
					exprObj.get("opr").getAsString());
			
			if ( expr.rhs.length() > 0) {
				if (expr.rhs.charAt(0) == '_') {
					if ( expr.rhs.equals("__null")) expr.rhs = null;
				}
			}
		}
		return expr;
	}

}
