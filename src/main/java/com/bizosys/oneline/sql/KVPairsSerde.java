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
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bizosys.oneline.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class KVPairsSerde {

	private static final char COMMA = ',';
	private static final char EQUAL = '=';

	
	private final static Logger LOG = LogManager.getLogger(KVPairsSerde.class);
	
	public static final void deser(final String inputStr, final Map<String, String> container) {
		
		if ( StringUtils.isEmpty(inputStr)) return;
		
		int index1 = 0;
		int index2 = 0;

		boolean hasMore = true;
		while (hasMore) {
			index1 = inputStr.indexOf(EQUAL, index2);
			if ( -1 == index1) return;
			final String key = inputStr.substring(index2, index1);
			index2 = inputStr.indexOf(COMMA, index1+1);
			if (index2 == -1) {
				index2 = inputStr.length();
				hasMore = false;
			}
			final String value = inputStr.substring(index1+1, index2);
			index2++;
			container.put(key.trim(), value);
		}
	}
	
	public static final void deserJson(final String jsonStr, final Map<String, String> container) {
		
		if ( StringUtils.isEmpty(jsonStr)) return;
		
		Gson gson = new Gson();
		JsonElement jsonElem = gson.fromJson(jsonStr, JsonElement.class);
		JsonObject jsonObj = jsonElem.getAsJsonObject();
		
		if ( !jsonObj.has("variables")) return;
		
		Iterator<JsonElement> variablesIterator = jsonObj.get("variables").getAsJsonArray().iterator();
				
		while(variablesIterator.hasNext())
		{
			JsonObject variableObj = (JsonObject) variablesIterator.next();
						
			if ( variableObj.has("key") && variableObj.has("value")) {
				String varKey = variableObj.get("key").getAsString();
				JsonElement obj = variableObj.get("value");
				String varVal = (obj.isJsonNull()) ? null : obj.getAsString();
				container.put(varKey, varVal);
			}
		}
		if( LOG.isDebugEnabled() ) LOG.debug("Parsed variables are : " + container.toString());
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String inputStr = "{\"variables\":[{\"key\":\"PWAccountID\",\"value\":\"157\"},{\"key\":\"CompanyName\",\"value\":\"''\"},{\"key\":\"Address1\",\"value\":\"''\"},{\"key\":\"Address2\",\"value\":null},{\"key\":\"City\",\"value\":null},{\"key\":\"StateID\",\"value\":\"4\"},{\"key\":\"CountryID\",\"value\":\"14\"},{\"key\":\"ZipCode\",\"value\":\"''\"},{\"key\":\"Phone\",\"value\":\"''\"},{\"key\":\"WebURL\",\"value\":null},{\"key\":\"CompanyDescription\",\"value\":null},{\"key\":\"PrimaryUserID\",\"value\":null},{\"key\":\"YearsInBusiness\",\"value\":null},{\"key\":\"AccountStatus\",\"value\":\"5\"},{\"key\":\"SYSRoleID\",\"value\":\"4\"},{\"key\":\"TimeZoneID\",\"value\":\"33\"},{\"key\":\"Subscribed\",\"value\":\"0\"},{\"key\":\"APPID\",\"value\":\"1\"},{\"key\":\"AccountExpiryDate\",\"value\":null},{\"key\":\"PurchaseMode\",\"value\":\"2\"}]}";
		Map<String, String> container = new HashMap<String, String>();
		KVPairsSerde.deserJson(inputStr, container);
		System.out.println(container);
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < 1; i++) {
//			System.out.println(container.toString());
//			container.clear();
//		}
//		long end = System.currentTimeMillis();
//		long millisTime = end - start;
//		System.out.println("Parsed IN " + millisTime + " milliseconds.");
	}

}
