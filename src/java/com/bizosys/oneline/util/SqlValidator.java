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

import com.bizosys.oneline.sql.QueryDefinationFactory;
import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.SQLParser;

public class SqlValidator {
	
	static SQLParser parser = new SQLParser();
	public static String validateSql( String sql ) {
		String message = "OK";
		try {
			String parsedQuery = QueryDefinationFactory.getVariables(sql, null, null);
			if( parsedQuery.indexOf("__where") > 0 ) 
				parsedQuery = parsedQuery.replace("__where", "1=1");
			if( parsedQuery.indexOf("__sort") > 0 ){
				parsedQuery = parsedQuery.replace("order by __sort", "");
				parsedQuery = parsedQuery.replace("ORDER BY __sort", "");
			}
			if( parsedQuery.indexOf("__offset") > 0 ){
				parsedQuery = parsedQuery.replace("offset __offset", "");
				parsedQuery = parsedQuery.replace("OFFSET __offset", "");
			}
			if( parsedQuery.indexOf("__limit") > 0 ){
				parsedQuery = parsedQuery.replace("limit __limit", "");
				parsedQuery = parsedQuery.replace("LIMIT __limit", "");
				
			}
			
			parser.parseStatement(parsedQuery);
			
		} catch (StandardException e) {
			message = "Syntax error : " + e.getMessage().split("\n")[0];
		}
		return message;
	}
	
  public static void main(String[] args) {
		String sql = "select id,name,class from test where id = @id $mywhere";
		String message = SqlValidator.validateSql(sql);
		System.out.println( message );
	}
}
