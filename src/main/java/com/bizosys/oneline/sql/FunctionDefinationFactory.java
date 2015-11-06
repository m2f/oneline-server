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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bizosys.oneline.dao.ReadMap;
import com.bizosys.oneline.util.ErrorCodeExp;
import com.bizosys.oneline.util.ErrorCodes;

public class FunctionDefinationFactory {

	public static Map<String, List<UnitStep>> functions = new ConcurrentHashMap<String, List<UnitStep>>();
	protected static final String CONFIGPOOL = "configpool";
		
	public static boolean refreshFunctions() throws SQLException, ErrorCodeExp {
		
		
		ReadMap readMap = new ReadMap();
		readMap.setPoolName(CONFIGPOOL);
		
		List<Map<String, String>> output = readMap.execute("select funcId, funcBody from sqlfunctions");
		
		StringBuilder sb = new StringBuilder(64);
		
		for (Map<String, String> functionDefn : output) {

			String funcId = functionDefn.get("funcId");
			String funcBody = functionDefn.get("funcBody");
			
			List<UnitStep> querySteps = new ArrayList<UnitStep>();
			try {
				SqlSensorInputParser.fromJson(funcBody, querySteps);
			} catch (ErrorCodeExp e) {
				sb.append("Error in function Id   : ").append(funcId)
				  .append(", ").append(e.errorMsg);
			}
			
			functions.put(funcId, querySteps);
		}
		String errorFunction = sb.toString();
		if(errorFunction.length() > 0){
			throw new ErrorCodeExp("Error Parsing functions", ErrorCodes.FUNCTION_NOT_FOUND, errorFunction, ErrorCodes.FUNCTION_KEY);
		}
		return true;
	}
	

}
