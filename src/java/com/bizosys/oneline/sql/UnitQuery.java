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

import java.util.ArrayList;
import java.util.List;

//import org.apache.log4j.Logger;




import com.bizosys.oneline.model.AppConfig;
import com.google.gson.JsonElement;

public final class UnitQuery extends UnitStep {
	
	public AppConfig appConfig = null;
	public List<Object> params = null;
	public UnitExpr expr = null;
	public List<UnitExpr> andExprs = null;
	public List<UnitExpr> orExprs = null;
	
	public String where = null;
	public String sort = null;
	public long offset = -1;
	public long limit = -1;
	
	public String variables = null;
	
	public boolean shouldProcess = true;
	public boolean isRecursive = false;
	public JsonElement sequenceElem = null;

	public UnitQuery(AppConfig appConfig, List<Object> params, UnitExpr expr, List<UnitExpr> andExprs, List<UnitExpr> orExprs,
			String where, String sort, long offset, long limit, String variables, boolean isRecursive, JsonElement sequenceElem) {
		
		this.appConfig = appConfig;
		this.params = params;
		this.expr = expr;
		this.andExprs = andExprs;
		this.orExprs = orExprs;
		
		this.where = where;
		this.sort = sort;
		this.offset = offset;
		this.limit = limit;
		
		this.variables = variables;
		this.isRecursive = isRecursive;
		this.sequenceElem = sequenceElem;
		
	}
	
	@Override
	public UnitStep cloneIt() {
		List<Object> clonedParams = null;
		if ( null != params) {
			clonedParams = new ArrayList<Object>(params.size());
			clonedParams.addAll(params);
		}
		
		UnitExpr cloneExpr = null;
		if ( null != this.expr ) cloneExpr = this.expr.clone();

		List<UnitExpr> clonedAndExprs = null;
		if ( null != andExprs) {
			clonedAndExprs = new ArrayList<UnitExpr>(andExprs.size());
			clonedAndExprs.addAll(andExprs);
		}
		
		List<UnitExpr> clonedOrExprs = null;
		if ( null != orExprs) {
			clonedOrExprs = new ArrayList<UnitExpr>(orExprs.size());
			clonedOrExprs.addAll(orExprs);
		}
		
		return new UnitQuery(this.appConfig.clone(), clonedParams, cloneExpr, clonedAndExprs, clonedOrExprs, 
			where, sort, offset, limit, variables, isRecursive, sequenceElem);
	}
	
	@Override
	public String toString() {
		return "UniitQuery : " + appConfig.toString();
	}
}