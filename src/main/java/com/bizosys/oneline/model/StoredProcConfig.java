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

package com.bizosys.oneline.model;

import java.util.ArrayList;
import java.util.List;
public class StoredProcConfig {

	public Long id;
	public String spTitle;
	public String spBody;
	public String spPoolname;
	public String spCallSyntax;
	public String spOutVar;
	public String spErrVar;
	public String status;

	/** Default constructor */
	public StoredProcConfig() {
	}


	/** Constructor with primary keys (Insert with primary key)*/
	public StoredProcConfig(Long id,String spTitle,String spBody,String spPoolname,
		String spCallSyntax,String spOutVar,String spErrVar,
		String status) {

		this.id = id;
		this.spTitle = spTitle;
		this.spBody = spBody;
		this.spPoolname = spPoolname;
		this.spCallSyntax = spCallSyntax;
		this.spOutVar = spOutVar;
		this.spErrVar = spErrVar;
		this.status = status;

	}


	/** Constructor with Non Primary keys (Insert with autoincrement)*/
	public StoredProcConfig(String spTitle,String spBody,String spPoolname,String spCallSyntax,
		String spOutVar,String spErrVar,String status) {

		this.spTitle = spTitle;
		this.spBody = spBody;
		this.spPoolname = spPoolname;
		this.spCallSyntax = spCallSyntax;
		this.spOutVar = spOutVar;
		this.spErrVar = spErrVar;
		this.status = status;

	}


	/** Params for (Insert with autoincrement)*/
	public Object[] getNewPrint() {
		return new Object[] {
			spTitle, spBody, spPoolname, spCallSyntax, spOutVar, spErrVar, 
			status
		};
	}


	/** Params for (Insert with primary key)*/
	public Object[] getNewPrintWithPK() {
		return new Object[] {
			id, spTitle, spBody, spPoolname, spCallSyntax, spOutVar, 
			spErrVar, status
		};
	}


	/** Params for (Update)*/
	public Object[] getExistingPrint() {
		return new Object[] {
			spTitle, spBody, spPoolname, spCallSyntax, spOutVar, spErrVar, 
			status, id
		};
	}

	private List<String> vars = null;
	public final void setVars(final List<String> vars) {
		this.vars = new ArrayList<String>(vars);
	}
	
	public final List<String> getVars(){
		return vars;
	}

	private List<StoredProcOutParam> outVars = null;
	public final void setOutVars(final List<StoredProcOutParam> outVars) {
		this.outVars = new ArrayList<StoredProcOutParam>(outVars);
	}
	
	public final List<StoredProcOutParam> getOutVars(){
		return outVars;
	}

	private StoredProcOutParam errorParam = null;
	public final void setErrorParam(final StoredProcOutParam errorParam) {
		this.errorParam = errorParam;
	}
	
	public final StoredProcOutParam getErrorParam(){
		return errorParam;
	}

	public StoredProcConfig clone() {
		StoredProcConfig config = new StoredProcConfig(this.id,this.spTitle, this.spBody, 
														this.spPoolname, this.spCallSyntax, 
														this.spOutVar, this.spErrVar,this.status);
		if( null != this.vars ) config.setVars(this.vars);
		
		return config;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(spTitle).append("|");
		sb.append(spCallSyntax).append("|");
		sb.append(spOutVar);
		return sb.toString();
	}
}
