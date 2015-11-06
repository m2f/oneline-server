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

import java.util.HashMap;
import java.util.Map;

public class UnitVariables {

	public Map<String, String> variablesM = null;
	public String jsonStr = null;
	
	public UnitVariables() {
		variablesM = new HashMap<String, String>();
	}
	
	public UnitVariables(String jsonStr ) {
		this.jsonStr = jsonStr;
		if ( null == jsonStr ) {
			if ( null == variablesM ) variablesM = new HashMap<String, String>();
			return;
		} else {
			if ( null == variablesM ) variablesM = new HashMap<String, String>();
			KVPairsSerde.deserJson(jsonStr, variablesM);
		}
		sanitize();
	}

	public void merge(String inputVariableJsonStr) {
		if ( null == variablesM) variablesM = new HashMap<String, String>();
		KVPairsSerde.deserJson(inputVariableJsonStr, variablesM);
		sanitize();			
	}
	
	public void merge(UnitVariables inputVariables) {
		if ( null == inputVariables) return;
		if ( null == variablesM )variablesM = new HashMap<String, String>();
		variablesM.putAll(inputVariables.variablesM);
	}

	public void merge(Map<String, String> inputVariables) {
		if ( null == variablesM ) variablesM = new HashMap<String, String>();
		variablesM.putAll(inputVariables);
	}

	private void sanitize() {
		if ( null != variablesM) {
			for (String  key : variablesM.keySet()) {
				String val = variablesM.get(key);
				if ( val != null ) {
					if ( val.length() > 1) {
						if ( val.charAt(0) == '_') {
							if ( "__null".equals(val)) variablesM.put(key, null);
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString() {
		if ( null == variablesM) return "No Variables";
		else return variablesM.toString();
	}
		

}
