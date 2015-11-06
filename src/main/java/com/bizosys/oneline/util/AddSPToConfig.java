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

package com.bizosys.oneline.util;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bizosys.oneline.dao.PoolFactory;
import com.bizosys.oneline.dao.WriteBase;
import com.bizosys.oneline.model.StoredProcConfig;
import com.bizosys.oneline.model.StoredProcConfigTable;

public class AddSPToConfig {

	private static final String SPACE = " ";
	private static final String VARIABLE_PREFIX = "v_";
	private static final String END_KEY = "BEGIN";
	private static final String START_KEY = "CREATE PROCEDURE";
	private static final Map<String, Integer> DATA_TYPES = new HashMap<String, Integer>();
	static {
		DATA_TYPES.put("INT", Types.INTEGER);
		DATA_TYPES.put("DATETIME", Types.DATE);
		DATA_TYPES.put("LONGTEXT", Types.VARCHAR);
		DATA_TYPES.put("NATIONAL VARCHAR", Types.NVARCHAR);
		DATA_TYPES.put("NVARCHAR", Types.NVARCHAR);
		DATA_TYPES.put("VARCHAR", Types.VARCHAR);
		DATA_TYPES.put("CHAR", Types.CHAR);
		DATA_TYPES.put("DECIMAL", Types.DECIMAL);
		DATA_TYPES.put("BOOLEAN", Types.BOOLEAN);
		DATA_TYPES.put("BIGINT", Types.BIGINT);
		DATA_TYPES.put("SMALLINT", Types.SMALLINT);
		DATA_TYPES.put("FLOAT", Types.FLOAT);
		
		DATA_TYPES.put("TINYINT UNSIGNED", Types.TINYINT);
	}
	
	public static void main(String[] args) throws Exception {
		
		if(args.length < 2) {
			System.err.println("Usage : " + AddSPToConfig.class.getName() + "<<sp_path>> <<sp_poolname>>");
			System.exit(1);
		}
		
		String path = args[0];
		String spPoolname = args[1];
		
		List<String> allSpsPath = new ArrayList<String>();
		getAllFiles(path, allSpsPath);
		
		List<StoredProcConfig> allSPs = new ArrayList<StoredProcConfig>();
		for (String spPath : allSpsPath) {
			StoredProcConfig spConfig = getSPConfigColValues(spPath, spPoolname);
			if( null == spConfig ) continue;
			allSPs.add(spConfig);
			System.out.println(spPath.substring(spPath.lastIndexOf('/') + 1) + "|" + spConfig.toString());
		}
		
		//addSpConfigToDb(allSPs);
		System.out.println("Successfully added all sp configs");
	}
	
	static class SPParam {
		public String variableName = null;
		public int variableType = 0;
		public boolean isOutParam = false;
		public SPParam(String variableName, int variableType, boolean isOutParam){
			this.variableName = variableName;
			this.variableType = variableType;
			this.isOutParam = isOutParam;
		}
		
		@Override
		public String toString() {
			return "VariableName : " + this.variableName 
					+ ", VariableType : " + variableType 
					+ ", isOutParam : " + this.isOutParam;
		}
	}
	
	public static StoredProcConfig getSPConfigColValues(String pathName, String spPoolname) throws Exception {
		
		//System.out.println("Processing SP : " + pathName);
		if(!pathName.endsWith(".sql")) return null;
		
		String sp = FileReaderUtil.toString(pathName);
		String spBody = sp;
				
		int startIndex = sp.indexOf(START_KEY) + START_KEY.length();
		int endIndex = sp.indexOf(END_KEY);
		sp = sp.substring(startIndex, endIndex).replaceAll("\\s+", SPACE);
		
		startIndex = sp.indexOf('(');
		String procedureName = sp.substring(0,startIndex).trim();
		String spTitle = procedureName;

		endIndex = sp.lastIndexOf(')');
		String procedureParams = sp.substring(startIndex + 1, endIndex).trim();
		List<String> procedureParamsList = new ArrayList<String>();
		LineReaderUtil.fastSplit(procedureParamsList, procedureParams, ',');

		List<SPParam> spparamL = new ArrayList<SPParam>();
		for (String param : procedureParamsList) {
		
			param = param.trim();
			int varStart = param.indexOf(VARIABLE_PREFIX);
			if(varStart < 0) continue;
			int varEnd = param.indexOf(SPACE,varStart);
			boolean isOutParam = false;
			if(varStart > 1) {
				 String out = param.substring(0, varStart).trim();
				 isOutParam = out.equalsIgnoreCase("INOUT") || out.equalsIgnoreCase("OUT");
			}
			String variableName = param.substring(varStart, varEnd).trim();
			String variableType = param.substring(varEnd).trim();
			
			int bracketIndex = variableType.indexOf('(');
			if(bracketIndex > 0) variableType = variableType.substring(0,bracketIndex);

			Integer javaSqlType = DATA_TYPES.get(variableType);
			if( null == javaSqlType ) throw new Exception("Variable type not configured for " + variableType);
			
			spparamL.add(new SPParam(variableName, javaSqlType, isOutParam));
		}

		StringBuilder callSyntax = new StringBuilder();
		callSyntax.append("Call ").append(procedureName).append("(");
		
		boolean isFirst = true;
		boolean isFirstOutput = true;
		
		StringBuilder outputVariablesSb = new StringBuilder();
		for(SPParam param : spparamL) {
			
			if( isFirst ) isFirst = false;
			else callSyntax.append(", ");
			
			callSyntax.append("@").append(param.variableName);
			if(param.isOutParam) {
				if(isFirstOutput) isFirstOutput = false;
				else outputVariablesSb.append(","); 
				outputVariablesSb.append(param.variableName)
				.append("=").append(param.variableType);
			}
		}
		
		callSyntax.append(")");
		
		return new StoredProcConfig(spTitle, spBody, spPoolname, 
				callSyntax.toString(), outputVariablesSb.toString(),"", "Y");
	}
	
	private static boolean getAllFiles(String pathname, List<String> allSpsPath) {
		File filePath = new File(pathname);
		if(filePath.isDirectory()) {
			File[] paths = filePath.listFiles();
			for (File path : paths) {
				getAllFiles(path.toString(), allSpsPath);
			}
			return true;
		} else {
			allSpsPath.add(pathname);
			return false;
		}
	}
	
	private static void addSpConfigToDb(List<StoredProcConfig> allSps) throws IOException, SQLException {
		Configuration conf = OnelineServerConfiguration
							.getInstance().getConfiguration();
		String dbConfFilename = conf.get("db.conf", "db.conf");
		File dbConfFile = new File(dbConfFilename);
		if ( !dbConfFile.exists()) throw new IOException("Could not find JDBC config at : " + dbConfFile.getAbsolutePath());
		PoolFactory.getInstance().setup(FileReaderUtil.toString(dbConfFilename));
		WriteBase sqlWriter = new WriteBase("configdb");
		try {
			sqlWriter.beginTransaction();
			for (StoredProcConfig storedProcConfig : allSps) {
				StoredProcConfigTable.insert(storedProcConfig, sqlWriter);
			}
			sqlWriter.commitTransaction();
		} catch (SQLException e) {
			System.err.println("Error loading sp config");
			sqlWriter.rollbackTransaction();
			throw e;
		}
	}
}
