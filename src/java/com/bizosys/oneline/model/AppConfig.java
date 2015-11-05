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

public class AppConfig {

	public Long id;
	public String configtype;
	public String title;
	public String body;
	public String variables;
	public String outvar;
	public String[] outvarA; 
	public String status;
	
	/** Default constructor */
	public AppConfig() {
	}


	/** Constructor with primary keys (Insert with primary key)*/
	public AppConfig(Long id,String configtype,String title,String body, 
		String variables, String outvar, String status) {

		this.id = id;
		this.configtype = configtype;
		this.title = title;
		this.body = body;
		this.variables = variables;
		this.outvar = outvar;
		if( null != outvar ) {
			if(outvar.length() > 0) {
				this.outvarA = outvar.split(",");
				for (int i = 0; i < this.outvarA.length; i++) {
					this.outvarA[i] = this.outvarA[i].trim();
				}
			}
		}
		this.status = status;

	}

	/** Constructor with primary keys (Insert with primary key)*/
	public AppConfig(Long id,String configtype,String title,String body, 
		String variables, String[] outvar, String status) {

		this.id = id;
		this.configtype = configtype;
		this.title = title;
		this.body = body;
		this.variables = variables;
		this.outvarA = outvar;
		this.status = status;

	}

	/** Constructor with Non Primary keys (Insert with autoincrement)*/
	public AppConfig(String configtype,String title,String body, String variables, String outvar, String status) {

		this.configtype = configtype;
		this.title = title;
		this.body = body;
		this.variables = variables;
		this.outvar = outvar;
		if( null != outvar ) {
			if(outvar.length() > 0) {
				this.outvarA = outvar.split(",");
				for (int i = 0; i < this.outvarA.length; i++) {
					this.outvarA[i] = this.outvarA[i].trim();
				}
			}
		}
		this.status = status;
	}


	/** Params for (Insert with autoincrement)*/
	public Object[] getNewPrint() {
		return new Object[] {
			configtype, title, body, variables, outvar, status
		};
	}


	/** Params for (Insert with primary key)*/
	public Object[] getNewPrintWithPK() {
		return new Object[] {
			id, configtype, title, body, variables, outvar, status
		};
	}


	/** Params for (Update)*/
	public Object[] getExistingPrint() {
		return new Object[] {
			configtype, title, body, status, variables, outvar, id
		};
	}
	
	public AppConfig clone() {
		AppConfig config = new AppConfig(id,configtype,title,body,variables, outvar, status);
		if( null != this.vars ) config.setVars(this.vars);
		if( null != this.customVars ) config.setCustomVars(this.customVars);
		return config;
	}
	
	private List<String> vars = null;
	public final void setVars(final List<String> vars) {
		this.vars = new ArrayList<String>(vars);
	}
	
	public final List<String> getVars(){
		return vars;
	}
	
	private List<String> customVars = null;
	public final void setCustomVars(final List<String> customVars) {
		this.customVars = new ArrayList<String>(customVars);
	}
	
	public final List<String> getCustomVars(){
		return customVars;
	}
	
	@Override
	public String toString() {
		return "Title: " + this.title + " , Query : " + this.body;
	}
}
