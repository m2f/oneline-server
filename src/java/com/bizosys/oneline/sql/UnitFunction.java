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
import java.util.Map;

public final class UnitFunction extends UnitStep {
	
	public String funcId = null;
	public UnitExpr expr = null;
	public String variables = null;
	public List<UnitExpr> andExprs = null;
	public List<UnitExpr> orExprs = null;
	public Boolean isRecursive = false;


	public UnitFunction(String funcId, String variables, UnitExpr expr, List<UnitExpr> andExprs,
			List<UnitExpr> orExprs,Boolean isRecursive) {
		this.funcId = funcId;
		this.variables = variables;
		this.expr = expr;
		this.andExprs = andExprs;
		this.orExprs = orExprs;
		this.isRecursive = isRecursive;
	}

	public List<UnitStep> getSteps() {
		Map<String, List<UnitStep>> functions = FunctionDefinationFactory.functions;
		if ( null != this.funcId ) {
			if ( functions.containsKey( this.funcId )) {
				return functions.get(this.funcId);
			}
		}
		return null;
	}

	@Override
	public UnitStep cloneIt() {
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
		
		return new UnitFunction(funcId, this.variables, cloneExpr, clonedAndExprs, clonedOrExprs,isRecursive);
	}
}